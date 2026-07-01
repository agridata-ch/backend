package ch.agridata.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import ch.agridata.aws.api.GuardDutyScanResultEnum;
import ch.agridata.product.persistence.DataProductDocumentEntity;
import ch.agridata.product.persistence.DataProductDocumentRepository;
import ch.agridata.product.persistence.DocumentScanStatusEnum;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.narayana.jta.TransactionRunnerOptions;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DataProductDocumentScanServiceTest {

  private static final UUID DOCUMENT_ID = UUID.randomUUID();
  private static final Instant NOW = Instant.parse("2026-07-09T12:00:00Z");
  private static final Duration PENDING_TIMEOUT = Duration.ofMinutes(15);

  @Mock
  DataProductDocumentStorageService dataProductDocumentStorageService;
  @Mock
  DataProductDocumentRepository dataProductDocumentRepository;
  @Mock
  TransactionRunnerOptions transactionRunner;

  // Fixed clock instead of a Clock mock: LocalDateTime.now(clock) needs instant() AND getZone(),
  // and a fixed clock keeps the cutoff assertion deterministic.
  private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

  private DataProductDocumentScanService service;

  @BeforeEach
  void setUp() {
    service = new DataProductDocumentScanService(
        dataProductDocumentStorageService, dataProductDocumentRepository, clock);
    service.pendingScanTimeout = PENDING_TIMEOUT;
  }

  @AfterEach
  void clearInterruptFlag() {
    // Some tests intentionally pre-interrupt the thread; never leak that into other tests.
    //noinspection ResultOfMethodCallIgnored
    Thread.interrupted();
  }

  /**
   * Makes the mocked transaction runner execute the passed callable/runnable inline.
   */
  private void stubTransactionRunnerToInvokeTask() {
    when(transactionRunner.call(any())).thenAnswer(invocation -> {
      Callable<?> callable = invocation.getArgument(0);
      return callable.call();
    });
  }

  private void stubTransactionRunnerToRunRunnable() {
    doAnswer(invocation -> {
      Runnable runnable = invocation.getArgument(0);
      runnable.run();
      return null;
    }).when(transactionRunner).run(any());
  }

  private DataProductDocumentEntity pendingDocument() {
    return DataProductDocumentEntity.builder()
        .id(DOCUMENT_ID)
        .scanStatus(DocumentScanStatusEnum.PENDING_SCAN)
        .build();
  }

  @Nested
  class PollUntilScanned {

    @ParameterizedTest
    @CsvSource({
        "NO_THREATS_FOUND, AVAILABLE",
        "THREATS_FOUND,    REJECTED",
        "UNSUPPORTED,      SCAN_FAILED",
        "ACCESS_DENIED,    SCAN_FAILED",
        "FAILED,           SCAN_FAILED",
        "UNKNOWN,          SCAN_FAILED",
    })
    void givenScanResultTagPresentOnFirstPoll_whenPoll_thenMapsResultToScanStatus(
        GuardDutyScanResultEnum guardDutyResult, DocumentScanStatusEnum expectedStatus) {
      var entity = pendingDocument();
      when(dataProductDocumentStorageService.readScanResult(DOCUMENT_ID)).thenReturn(guardDutyResult);
      when(dataProductDocumentRepository.findByIdForUpdate(DOCUMENT_ID)).thenReturn(Optional.of(entity));

      try (MockedStatic<QuarkusTransaction> quarkusTransaction = mockStatic(QuarkusTransaction.class)) {
        quarkusTransaction.when(QuarkusTransaction::requiringNew).thenReturn(transactionRunner);
        stubTransactionRunnerToRunRunnable();

        service.pollUntilScanned(DOCUMENT_ID);

        assertThat(entity.getScanStatus()).isEqualTo(expectedStatus);
      }
    }

    @ParameterizedTest
    @EnumSource(value = DocumentScanStatusEnum.class, names = "PENDING_SCAN", mode = EnumSource.Mode.EXCLUDE)
    void givenDocumentAlreadyInTerminalStatus_whenPoll_thenStatusIsNotClobbered(
        DocumentScanStatusEnum terminalStatus) {
      // Simulates the recovery watchdog having resolved the document first: the poll's
      // update must become a no-op instead of overwriting the terminal status.
      var entity = DataProductDocumentEntity.builder()
          .id(DOCUMENT_ID)
          .scanStatus(terminalStatus)
          .build();
      when(dataProductDocumentStorageService.readScanResult(DOCUMENT_ID))
          .thenReturn(GuardDutyScanResultEnum.NO_THREATS_FOUND);
      when(dataProductDocumentRepository.findByIdForUpdate(DOCUMENT_ID)).thenReturn(Optional.of(entity));

      try (MockedStatic<QuarkusTransaction> quarkusTransaction = mockStatic(QuarkusTransaction.class)) {
        quarkusTransaction.when(QuarkusTransaction::requiringNew).thenReturn(transactionRunner);
        stubTransactionRunnerToRunRunnable();

        service.pollUntilScanned(DOCUMENT_ID);

        assertThat(entity.getScanStatus()).isEqualTo(terminalStatus);
      }
    }

    @Test
    void givenDocumentDeletedMeanwhile_whenPoll_thenUpdateIsANoOp() {
      when(dataProductDocumentStorageService.readScanResult(DOCUMENT_ID))
          .thenReturn(GuardDutyScanResultEnum.NO_THREATS_FOUND);
      when(dataProductDocumentRepository.findByIdForUpdate(DOCUMENT_ID)).thenReturn(Optional.empty());

      try (MockedStatic<QuarkusTransaction> quarkusTransaction = mockStatic(QuarkusTransaction.class)) {
        quarkusTransaction.when(QuarkusTransaction::requiringNew).thenReturn(transactionRunner);
        stubTransactionRunnerToRunRunnable();

        assertThatCode(() -> service.pollUntilScanned(DOCUMENT_ID)).doesNotThrowAnyException();
      }
    }

    @Test
    void givenNoResultYetAndThreadInterrupted_whenPoll_thenReturnsWithoutUpdateAndRestoresInterruptFlag() {
      // Pre-interrupting the thread makes Thread.sleep throw immediately, letting us cover the
      // interrupt path without waiting out the 5s poll interval.
      when(dataProductDocumentStorageService.readScanResult(DOCUMENT_ID)).thenReturn(null);

      Thread.currentThread().interrupt();
      service.pollUntilScanned(DOCUMENT_ID);

      assertThat(Thread.currentThread().isInterrupted())
          .as("interrupt flag must be restored")
          .isTrue();
      verifyNoInteractions(dataProductDocumentRepository);
    }

    @Test
    void givenTransientReadFailure_whenPoll_thenExceptionIsSwallowedAndTreatedAsNotYetScanned() {
      // Same pre-interrupt trick: the swallowed read failure leads to the sleep branch,
      // which we exit immediately instead of waiting.
      when(dataProductDocumentStorageService.readScanResult(DOCUMENT_ID))
          .thenThrow(new RuntimeException("GetObjectTagging failed"));

      Thread.currentThread().interrupt();
      assertThatCode(() -> service.pollUntilScanned(DOCUMENT_ID)).doesNotThrowAnyException();

      verifyNoInteractions(dataProductDocumentRepository);
    }
  }

  @Nested
  class RecoverStalePendingScans {

    @Test
    void givenNoStaleDocuments_whenRecover_thenQueriesWithCorrectCutoffAndDoesNothingElse() {
      var expectedCutoff = LocalDateTime.now(clock).minus(PENDING_TIMEOUT);
      when(dataProductDocumentRepository.findStalePendingScans(expectedCutoff)).thenReturn(List.of());

      try (MockedStatic<QuarkusTransaction> quarkusTransaction = mockStatic(QuarkusTransaction.class)) {
        quarkusTransaction.when(QuarkusTransaction::requiringNew).thenReturn(transactionRunner);
        stubTransactionRunnerToInvokeTask();

        service.recoverStalePendingScans();

        verify(dataProductDocumentRepository).findStalePendingScans(expectedCutoff);
        verifyNoMoreInteractions(dataProductDocumentRepository);
        verifyNoInteractions(dataProductDocumentStorageService);
      }
    }

    @Test
    void givenStaleDocumentWithLateGuardDutyTag_whenRecover_thenAppliesRealResult() {
      var entity = pendingDocument();
      when(dataProductDocumentRepository.findStalePendingScans(any(LocalDateTime.class)))
          .thenReturn(List.of(entity));
      when(dataProductDocumentStorageService.readScanResult(DOCUMENT_ID))
          .thenReturn(GuardDutyScanResultEnum.NO_THREATS_FOUND);
      when(dataProductDocumentRepository.findByIdForUpdate(DOCUMENT_ID)).thenReturn(Optional.of(entity));

      try (MockedStatic<QuarkusTransaction> quarkusTransaction = mockStatic(QuarkusTransaction.class)) {
        quarkusTransaction.when(QuarkusTransaction::requiringNew).thenReturn(transactionRunner);
        stubTransactionRunnerToInvokeTask();
        stubTransactionRunnerToRunRunnable();

        service.recoverStalePendingScans();

        assertThat(entity.getScanStatus()).isEqualTo(DocumentScanStatusEnum.AVAILABLE);
      }
    }

    @Test
    void givenStaleDocumentNeverTagged_whenRecover_thenFailsClosedWithScanFailed() {
      var entity = pendingDocument();
      when(dataProductDocumentRepository.findStalePendingScans(any(LocalDateTime.class)))
          .thenReturn(List.of(entity));
      when(dataProductDocumentStorageService.readScanResult(DOCUMENT_ID)).thenReturn(null);
      when(dataProductDocumentRepository.findByIdForUpdate(DOCUMENT_ID)).thenReturn(Optional.of(entity));

      try (MockedStatic<QuarkusTransaction> quarkusTransaction = mockStatic(QuarkusTransaction.class)) {
        quarkusTransaction.when(QuarkusTransaction::requiringNew).thenReturn(transactionRunner);
        stubTransactionRunnerToInvokeTask();
        stubTransactionRunnerToRunRunnable();

        service.recoverStalePendingScans();

        assertThat(entity.getScanStatus()).isEqualTo(DocumentScanStatusEnum.SCAN_FAILED);
      }
    }

    @Test
    void givenReadScanResultFails_whenRecover_thenDocumentStaysPendingForNextRun() {
      var entity = pendingDocument();
      when(dataProductDocumentRepository.findStalePendingScans(any(LocalDateTime.class)))
          .thenReturn(List.of(entity));
      when(dataProductDocumentStorageService.readScanResult(DOCUMENT_ID))
          .thenThrow(new RuntimeException("S3 unavailable"));

      try (MockedStatic<QuarkusTransaction> quarkusTransaction = mockStatic(QuarkusTransaction.class)) {
        quarkusTransaction.when(QuarkusTransaction::requiringNew).thenReturn(transactionRunner);
        stubTransactionRunnerToInvokeTask();

        assertThatCode(() -> service.recoverStalePendingScans()).doesNotThrowAnyException();

        // Not failed closed: a transient read failure must not condemn a possibly-clean document.
        assertThat(entity.getScanStatus()).isEqualTo(DocumentScanStatusEnum.PENDING_SCAN);
        verify(dataProductDocumentRepository, never()).findByIdForUpdate(any(UUID.class));
      }
    }

    @Test
    void givenOneDocumentFailsToRecover_whenRecover_thenRemainingDocumentsAreStillProcessed() {
      var failingId = UUID.randomUUID();
      var failingEntity = DataProductDocumentEntity.builder()
          .id(failingId)
          .scanStatus(DocumentScanStatusEnum.PENDING_SCAN)
          .build();
      var recoverableEntity = pendingDocument();
      when(dataProductDocumentRepository.findStalePendingScans(any(LocalDateTime.class)))
          .thenReturn(List.of(failingEntity, recoverableEntity));
      when(dataProductDocumentStorageService.readScanResult(failingId))
          .thenThrow(new RuntimeException("S3 unavailable"));
      when(dataProductDocumentStorageService.readScanResult(DOCUMENT_ID))
          .thenReturn(GuardDutyScanResultEnum.THREATS_FOUND);
      when(dataProductDocumentRepository.findByIdForUpdate(DOCUMENT_ID))
          .thenReturn(Optional.of(recoverableEntity));

      try (MockedStatic<QuarkusTransaction> quarkusTransaction = mockStatic(QuarkusTransaction.class)) {
        quarkusTransaction.when(QuarkusTransaction::requiringNew).thenReturn(transactionRunner);
        stubTransactionRunnerToInvokeTask();
        stubTransactionRunnerToRunRunnable();

        service.recoverStalePendingScans();

        assertThat(failingEntity.getScanStatus()).isEqualTo(DocumentScanStatusEnum.PENDING_SCAN);
        assertThat(recoverableEntity.getScanStatus()).isEqualTo(DocumentScanStatusEnum.REJECTED);
      }
    }
  }
}