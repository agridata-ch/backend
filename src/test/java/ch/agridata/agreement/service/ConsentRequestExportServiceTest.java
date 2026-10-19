package ch.agridata.agreement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.agridata.agreement.persistence.ConsentRequestRepository;
import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.agreement.persistence.DataRequestEntity.DataRequestStateEnum;
import ch.agridata.agreement.persistence.DataRequestRepository;
import ch.agridata.common.exceptions.ExportCapacityExhaustedException;
import ch.agridata.common.exceptions.ExportLimitExceededException;
import ch.agridata.common.security.AgridataSecurityIdentity;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link ConsentRequestExportService}.
 *
 * @CommentLastReviewed 2026-10-12
 */
@ExtendWith(MockitoExtension.class)
class ConsentRequestExportServiceTest {

  private static final UUID DATA_REQUEST_ID = UUID.randomUUID();
  private static final String CONSUMER_UID = "CHE101708094";

  // Already 2026-10-13 in Zurich while still 2026-10-12 in UTC: the file name must use the local date.
  private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-12T22:30:00Z"), ZoneId.of("Europe/Zurich"));

  @Mock
  private ConsentRequestRepository consentRequestRepository;

  @Mock
  private DataRequestRepository dataRequestRepository;

  @Mock
  private AgridataSecurityIdentity identity;

  private ConsentRequestExportService service;

  @BeforeEach
  void setUp() {
    service = new ConsentRequestExportService(consentRequestRepository, dataRequestRepository, identity, CLOCK);
  }

  private void givenDataRequestVisibleToAdmin(DataRequestStateEnum stateCode) {
    var dataRequest = DataRequestEntity.builder().id(DATA_REQUEST_ID).humanFriendlyId("SV29").stateCode(stateCode).build();
    when(dataRequestRepository.findByIdAndStateCodeNotDraft(DATA_REQUEST_ID)).thenReturn(Optional.of(dataRequest));
  }

  private void givenDataRequestOfCurrentConsumer(DataRequestStateEnum stateCode) {
    var dataRequest = DataRequestEntity.builder().id(DATA_REQUEST_ID).humanFriendlyId("SV29").stateCode(stateCode).build();
    when(identity.getUidOrElseThrow()).thenReturn(CONSUMER_UID);
    when(dataRequestRepository.findByIdAndDataConsumerUid(DATA_REQUEST_ID, CONSUMER_UID)).thenReturn(Optional.of(dataRequest));
  }

  @Test
  void givenDataRequest_whenExporting_thenFileNameContainsHumanFriendlyIdAndLocalDate() {
    givenDataRequestVisibleToAdmin(DataRequestStateEnum.ACTIVE);
    var export = service.exportConsentRequestsOfDataRequestAsCsv(DATA_REQUEST_ID);

    assertThat(export.fileName()).isEqualTo("consent-requests-SV29-2026-10-13.csv");
  }

  @Test
  void givenMaxConcurrentExportsRunning_whenExporting_thenRejectedUntilAnExportFinished() throws Exception {
    givenDataRequestVisibleToAdmin(DataRequestStateEnum.ACTIVE);
    var queriesStarted = new CountDownLatch(2);
    var queriesMayFinish = new CountDownLatch(1);
    when(consentRequestRepository.findActiveExportProjectionsByDataRequestId(any())).thenAnswer(invocation -> {
      queriesStarted.countDown();
      queriesMayFinish.await();
      return List.of();
    });

    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      List<Future<?>> runningExports = List.of(
          executor.submit(() -> service.exportConsentRequestsOfDataRequestAsCsv(DATA_REQUEST_ID)),
          executor.submit(() -> service.exportConsentRequestsOfDataRequestAsCsv(DATA_REQUEST_ID))
      );
      assertThat(queriesStarted.await(5, TimeUnit.SECONDS)).isTrue();

      assertThatThrownBy(() -> service.exportConsentRequestsOfDataRequestAsCsv(DATA_REQUEST_ID))
          .isInstanceOf(ExportCapacityExhaustedException.class);

      queriesMayFinish.countDown();
      for (var runningExport : runningExports) {
        runningExport.get(5, TimeUnit.SECONDS);
      }
    }

    assertThat(service.exportConsentRequestsOfDataRequestAsCsv(DATA_REQUEST_ID)).isNotNull();
  }

  @Test
  void givenExportsFailing_whenExportingAgain_thenPermitsWereReleased() {
    givenDataRequestVisibleToAdmin(DataRequestStateEnum.ACTIVE);
    when(consentRequestRepository.findActiveExportProjectionsByDataRequestId(DATA_REQUEST_ID))
        .thenThrow(new IllegalStateException("query failed"))
        .thenThrow(new IllegalStateException("query failed"))
        .thenReturn(List.of());

    // As many failures as there are permits: a single leaked permit would make the subsequent export fail.
    for (var i = 0; i < 2; i++) {
      assertThatThrownBy(() -> service.exportConsentRequestsOfDataRequestAsCsv(DATA_REQUEST_ID))
          .isInstanceOf(IllegalStateException.class);
    }

    assertThat(service.exportConsentRequestsOfDataRequestAsCsv(DATA_REQUEST_ID)).isNotNull();
  }

  @Test
  void givenNoConsentRequests_whenExporting_thenCsvContainsHeaderOnly() {
    givenDataRequestVisibleToAdmin(DataRequestStateEnum.ACTIVE);

    var export = service.exportConsentRequestsOfDataRequestAsCsv(DATA_REQUEST_ID);

    assertThat(new String(export.content(), StandardCharsets.UTF_8))
        .isEqualTo("\uFEFFuid;bur;requestDate;lastChangeDate;stateCode\r\n");
  }

  @ParameterizedTest
  @EnumSource(value = DataRequestStateEnum.class, names = {"ACTIVE", "PAUSED"})
  void givenExportableDataRequestOfCurrentConsumer_whenExporting_thenExported(DataRequestStateEnum stateCode) {
    givenDataRequestOfCurrentConsumer(stateCode);

    assertThat(service.exportConsentRequestsOfDataRequestOfCurrentConsumerAsCsv(DATA_REQUEST_ID)).isNotNull();
  }

  @ParameterizedTest
  @EnumSource(value = DataRequestStateEnum.class, names = {"ACTIVE", "PAUSED"}, mode = EnumSource.Mode.EXCLUDE)
  void givenNonExportableDataRequestOfCurrentConsumer_whenExporting_thenRejected(DataRequestStateEnum stateCode) {
    givenDataRequestOfCurrentConsumer(stateCode);

    assertThatThrownBy(() -> service.exportConsentRequestsOfDataRequestOfCurrentConsumerAsCsv(DATA_REQUEST_ID))
        .isInstanceOf(IllegalStateException.class);
    verify(consentRequestRepository, never()).findActiveExportProjectionsByDataRequestId(any());
  }

  // Drafts are excluded, because the repository does not return them to admins.
  @ParameterizedTest
  @EnumSource(value = DataRequestStateEnum.class, names = {"ACTIVE", "PAUSED", "DRAFT"}, mode = EnumSource.Mode.EXCLUDE)
  void givenNonExportableDataRequest_whenAdminExports_thenRejected(DataRequestStateEnum stateCode) {
    givenDataRequestVisibleToAdmin(stateCode);

    assertThatThrownBy(() -> service.exportConsentRequestsOfDataRequestAsCsv(DATA_REQUEST_ID))
        .isInstanceOf(IllegalStateException.class);
    verify(consentRequestRepository, never()).findActiveExportProjectionsByDataRequestId(any());
  }

  @Test
  void givenMoreThanMaxRows_whenExporting_thenRejectedWithoutLoadingRows() {
    givenDataRequestVisibleToAdmin(DataRequestStateEnum.ACTIVE);
    when(consentRequestRepository.countActiveByDataRequestId(DATA_REQUEST_ID)).thenReturn(100_001L);

    assertThatThrownBy(() -> service.exportConsentRequestsOfDataRequestAsCsv(DATA_REQUEST_ID))
        .isInstanceOf(ExportLimitExceededException.class);
    verify(consentRequestRepository, never()).findActiveExportProjectionsByDataRequestId(any());
  }

  @Test
  void givenExactlyMaxRows_whenExporting_thenExported() {
    givenDataRequestVisibleToAdmin(DataRequestStateEnum.ACTIVE);
    when(consentRequestRepository.countActiveByDataRequestId(DATA_REQUEST_ID)).thenReturn(100_000L);

    assertThat(service.exportConsentRequestsOfDataRequestAsCsv(DATA_REQUEST_ID)).isNotNull();
    verify(consentRequestRepository).findActiveExportProjectionsByDataRequestId(DATA_REQUEST_ID);
  }
}
