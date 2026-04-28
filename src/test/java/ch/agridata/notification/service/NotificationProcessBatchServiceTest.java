package ch.agridata.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.agridata.common.persistence.TranslationPersistenceDto;
import ch.agridata.notification.persistence.NotificationBatchEntity;
import ch.agridata.notification.persistence.NotificationBatchRepository;
import ch.agridata.notification.persistence.NotificationBatchStatusEnum;
import ch.agridata.notification.persistence.NotificationRecipientEntity;
import ch.agridata.notification.persistence.NotificationRecipientRepository;
import ch.agridata.notification.persistence.NotificationTemplateEntity;
import ch.agridata.notification.service.NotificationProcessRecipientService.RecipientProcessingResult;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link NotificationProcessBatchService}.
 * Verifies batch status transitions and delegation to {@link NotificationProcessRecipientService}
 * across all result combinations (inbox-only, email sent, email failed, mixed).
 *
 * @CommentLastReviewed 2026-04-29
 */
@ExtendWith(MockitoExtension.class)
class NotificationProcessBatchServiceTest {

  @InjectMocks
  private NotificationProcessBatchService service;

  @Mock
  private NotificationBatchRepository batchRepository;

  @Mock
  private NotificationRecipientRepository recipientRepository;

  @Mock
  private NotificationProcessRecipientService recipientProcessorService;

  // ----- helpers -----

  private static final RecipientProcessingResult INBOX_ONLY = new RecipientProcessingResult(true, false, false);
  private static final RecipientProcessingResult EMAIL_SENT = new RecipientProcessingResult(false, true, false);
  private static final RecipientProcessingResult EMAIL_FAILED = new RecipientProcessingResult(false, false, true);
  private static final RecipientProcessingResult NOTHING = new RecipientProcessingResult(false, false, false);

  private static NotificationTemplateEntity buildTemplate() {
    return NotificationTemplateEntity.builder()
        .id(UUID.randomUUID())
        .eventTypeCode("TEST_EVENT")
        .emailSubject(new TranslationPersistenceDto("Testbetreff", null, null))
        .emailText(new TranslationPersistenceDto("<p>Testinhalt</p>", null, null))
        .build();
  }

  private static NotificationBatchEntity buildBatch() {
    return NotificationBatchEntity.builder()
        .id(UUID.randomUUID())
        .statusCode(NotificationBatchStatusEnum.PENDING)
        .template(buildTemplate())
        .build();
  }

  private static NotificationRecipientEntity buildRecipient(NotificationBatchEntity batch) {
    return NotificationRecipientEntity.builder().id(UUID.randomUUID()).batch(batch).build();
  }

  // ----- tests -----

  @Test
  void givenNoPendingBatches_whenProcessPendingBatches_thenNoRecipientsProcessed() {
    when(batchRepository.findPendingWithLock()).thenReturn(List.of());

    service.processPendingBatches();

    verify(recipientProcessorService, never()).processRecipient(any(), any(), any());
  }

  @Test
  void givenPendingBatchWithNoRecipients_whenProcessPendingBatches_thenBatchSetToComplete() {
    var batch = buildBatch();
    when(batchRepository.findPendingWithLock()).thenReturn(List.of(batch));
    when(recipientRepository.findByBatchId(batch.getId())).thenReturn(List.of());

    service.processPendingBatches();

    assertThat(batch.getStatusCode()).isEqualTo(NotificationBatchStatusEnum.COMPLETE);
  }

  @Test
  void givenRecipientsWithInboxResult_whenProcessPendingBatches_thenDelegatesToProcessorService() {
    var batch = buildBatch();
    var r1 = buildRecipient(batch);
    var r2 = buildRecipient(batch);

    when(batchRepository.findPendingWithLock()).thenReturn(List.of(batch));
    when(recipientRepository.findByBatchId(batch.getId())).thenReturn(List.of(r1, r2));
    when(recipientProcessorService.processRecipient(eq(r1.getId()), any(), any())).thenReturn(INBOX_ONLY);
    when(recipientProcessorService.processRecipient(eq(r2.getId()), any(), any())).thenReturn(INBOX_ONLY);

    service.processPendingBatches();

    verify(recipientProcessorService, times(2)).processRecipient(any(), any(), any());
    assertThat(batch.getStatusCode()).isEqualTo(NotificationBatchStatusEnum.COMPLETE);
  }

  @Test
  void givenAllEmailsSucceed_whenProcessPendingBatches_thenBatchComplete() {
    var batch = buildBatch();
    var r1 = buildRecipient(batch);
    var r2 = buildRecipient(batch);

    when(batchRepository.findPendingWithLock()).thenReturn(List.of(batch));
    when(recipientRepository.findByBatchId(batch.getId())).thenReturn(List.of(r1, r2));
    when(recipientProcessorService.processRecipient(eq(r1.getId()), any(), any())).thenReturn(EMAIL_SENT);
    when(recipientProcessorService.processRecipient(eq(r2.getId()), any(), any())).thenReturn(EMAIL_SENT);

    service.processPendingBatches();

    assertThat(batch.getStatusCode()).isEqualTo(NotificationBatchStatusEnum.COMPLETE);
  }

  @Test
  void givenAllEmailsFail_whenProcessPendingBatches_thenBatchFailed() {
    var batch = buildBatch();
    var r1 = buildRecipient(batch);

    when(batchRepository.findPendingWithLock()).thenReturn(List.of(batch));
    when(recipientRepository.findByBatchId(batch.getId())).thenReturn(List.of(r1));
    when(recipientProcessorService.processRecipient(eq(r1.getId()), any(), any())).thenReturn(EMAIL_FAILED);

    service.processPendingBatches();

    assertThat(batch.getStatusCode()).isEqualTo(NotificationBatchStatusEnum.FAILED);
  }

  @Test
  void givenSomeEmailsFail_whenProcessPendingBatches_thenBatchPartiallyFailed() {
    var batch = buildBatch();
    var r1 = buildRecipient(batch);
    var r2 = buildRecipient(batch);

    when(batchRepository.findPendingWithLock()).thenReturn(List.of(batch));
    when(recipientRepository.findByBatchId(batch.getId())).thenReturn(List.of(r1, r2));
    when(recipientProcessorService.processRecipient(eq(r1.getId()), any(), any())).thenReturn(EMAIL_SENT);
    when(recipientProcessorService.processRecipient(eq(r2.getId()), any(), any())).thenReturn(EMAIL_FAILED);

    service.processPendingBatches();

    assertThat(batch.getStatusCode()).isEqualTo(NotificationBatchStatusEnum.PARTIALLY_FAILED);
  }

  @Test
  void givenRecipientLookupThrows_whenProcessPendingBatches_thenBatchSetToFailedAndExceptionRethrown() {
    var batch = buildBatch();

    when(batchRepository.findPendingWithLock()).thenReturn(List.of(batch));
    when(recipientRepository.findByBatchId(batch.getId())).thenThrow(new RuntimeException("DB error"));

    assertThatThrownBy(() -> service.processPendingBatches())
        .isInstanceOf(RuntimeException.class)
        .hasMessage("DB error");

    assertThat(batch.getStatusCode()).isEqualTo(NotificationBatchStatusEnum.FAILED);
    verify(recipientProcessorService, never()).processRecipient(any(), any(), any());
  }

  @Test
  void givenRecipientProcessorThrows_whenProcessPendingBatches_thenBatchSetToFailedAndExceptionRethrown() {
    var batch = buildBatch();
    var r1 = buildRecipient(batch);

    when(batchRepository.findPendingWithLock()).thenReturn(List.of(batch));
    when(recipientRepository.findByBatchId(batch.getId())).thenReturn(List.of(r1));
    when(recipientProcessorService.processRecipient(eq(r1.getId()), any(), any()))
        .thenThrow(new RuntimeException("processor error"));

    assertThatThrownBy(() -> service.processPendingBatches())
        .isInstanceOf(RuntimeException.class)
        .hasMessage("processor error");

    assertThat(batch.getStatusCode()).isEqualTo(NotificationBatchStatusEnum.FAILED);
  }

  @Test
  void givenMultipleBatches_whenProcessPendingBatches_thenAllBatchesProcessed() {
    var batch1 = buildBatch();
    var batch2 = buildBatch();
    var r1 = buildRecipient(batch1);
    var r2 = buildRecipient(batch2);

    when(batchRepository.findPendingWithLock()).thenReturn(List.of(batch1, batch2));
    when(recipientRepository.findByBatchId(batch1.getId())).thenReturn(List.of(r1));
    when(recipientRepository.findByBatchId(batch2.getId())).thenReturn(List.of(r2));
    when(recipientProcessorService.processRecipient(any(), any(), any())).thenReturn(NOTHING);

    service.processPendingBatches();

    verify(recipientProcessorService, times(2)).processRecipient(any(), any(), any());
    assertThat(batch1.getStatusCode()).isEqualTo(NotificationBatchStatusEnum.COMPLETE);
    assertThat(batch2.getStatusCode()).isEqualTo(NotificationBatchStatusEnum.COMPLETE);
  }
}
