package integration.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import ch.agridata.aws.api.GuardDutyScanResultEnum;
import ch.agridata.aws.api.PdfStorageApi;
import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.product.job.DataProductScanRecoveryJob;
import ch.agridata.product.persistence.DataProductDocumentEntity;
import ch.agridata.product.persistence.DataProductDocumentRepository;
import ch.agridata.product.persistence.DataProductEntity;
import ch.agridata.product.persistence.DocumentScanStatusEnum;
import integration.testutils.TestDataIdentifiers;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for the scan recovery watchdog. Exercises {@link DataProductScanRecoveryJob}
 * end-to-end against the devservices PostgreSQL database (real stale-document query, advisory lock
 * and transactional status updates) with a mocked {@link PdfStorageApi} standing in for S3/GuardDuty.
 *
 * <p>The scheduler is disabled so only the explicit {@code job.run()} calls drive the behaviour
 * under test.
 */

@QuarkusTest
@TestProfile(DataProductScanRecoveryJobTest.NoSchedulerProfile.class)
@RequiredArgsConstructor
class DataProductScanRecoveryJobTest {

  private final DataProductScanRecoveryJob job;
  private final DataProductDocumentRepository repository;
  private final Clock clock;

  @InjectMock
  PdfStorageApi pdfStorageApi;

  @InjectMock
  AgridataSecurityIdentity agridataSecurityIdentity;

  private static final UUID TEST_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000042");

  public static class NoSchedulerProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
      return Map.of("quarkus.scheduler.enabled", "false");
    }
  }

  @BeforeEach
  void setUpSecurityIdentity() {
    when(agridataSecurityIdentity.getUserId()).thenReturn(TEST_USER_ID);
  }

  @Test
  void givenStalePendingDocumentTaggedClean_whenRun_thenTransitionsToAvailable() {
    UUID docId = insertPendingDocument(true);
    when(pdfStorageApi.readScanResult(anyString(), anyString())).thenReturn(GuardDutyScanResultEnum.NO_THREATS_FOUND);

    job.run();

    assertThat(scanStatusOf(docId)).isEqualTo(DocumentScanStatusEnum.AVAILABLE);
  }

  @Test
  void givenStalePendingDocumentTaggedThreat_whenRun_thenTransitionsToRejected() {
    UUID docId = insertPendingDocument(true);
    when(pdfStorageApi.readScanResult(anyString(), anyString())).thenReturn(GuardDutyScanResultEnum.THREATS_FOUND);

    job.run();

    assertThat(scanStatusOf(docId)).isEqualTo(DocumentScanStatusEnum.REJECTED);
  }

  @Test
  void givenStalePendingDocumentNeverTagged_whenRun_thenFailsClosedWithScanFailed() {
    UUID docId = insertPendingDocument(true);
    when(pdfStorageApi.readScanResult(anyString(), anyString())).thenReturn(null);

    job.run();

    assertThat(scanStatusOf(docId)).isEqualTo(DocumentScanStatusEnum.SCAN_FAILED);
  }

  @Test
  void givenRecentPendingDocument_whenRun_thenLeftPendingUntilTimeoutElapses() {
    UUID docId = insertPendingDocument(false);
    when(pdfStorageApi.readScanResult(anyString(), anyString())).thenReturn(GuardDutyScanResultEnum.NO_THREATS_FOUND);

    job.run();

    assertThat(scanStatusOf(docId)).isEqualTo(DocumentScanStatusEnum.PENDING_SCAN);
  }

  private UUID insertPendingDocument(boolean stale) {
    UUID docId = UUID.randomUUID();
    DataProductEntity dataProductEntity = DataProductEntity.builder().id(TestDataIdentifiers.DataProduct.UUID_E08AF9D2.uuid()).build();
    QuarkusTransaction.requiringNew().run(() -> repository.persist(
        DataProductDocumentEntity.builder()
            .id(docId)
            .dataProduct(dataProductEntity)
            .originalFilename("recovery-test.pdf")
            .sizeBytes(1024L)
            .scanStatus(DocumentScanStatusEnum.PENDING_SCAN)
            .build()));
    if (stale) {
      QuarkusTransaction.requiringNew().run(() ->
          repository.update("createdAt = ?1 where id = ?2", LocalDateTime.now(clock).minusHours(1), docId));
    }
    return docId;
  }

  private DocumentScanStatusEnum scanStatusOf(UUID docId) {
    return QuarkusTransaction.requiringNew().call(() -> repository.findById(docId).getScanStatus());
  }
}
