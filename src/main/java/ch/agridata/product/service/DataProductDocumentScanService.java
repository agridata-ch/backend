package ch.agridata.product.service;

import ch.agridata.aws.api.GuardDutyScanResultEnum;
import ch.agridata.product.persistence.DataProductDocumentRepository;
import ch.agridata.product.persistence.DocumentScanStatusEnum;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Owns the GuardDuty malware-scan lifecycle of data product documents: polling S3 for the scan
 * result tag, mapping GuardDuty verdicts to {@link DocumentScanStatusEnum}, and recovering documents
 * whose poll never completed. Knows nothing about authorization, DTOs, or upload mechanics.
 *
 * @CommentLastReviewed 2026-07-09
 */

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class DataProductDocumentScanService {

  private static final long SCAN_POLL_INTERVAL_MILLIS = 5_000;
  private static final long SCAN_POLL_TIMEOUT_MILLIS = 60_000;

  private final DataProductDocumentStorageService dataProductDocumentStorageService;
  private final DataProductDocumentRepository dataProductDocumentRepository;
  private final Clock clock;

  @ConfigProperty(name = "agridata.product.scan-recovery.pending-timeout", defaultValue = "PT15M")
  Duration pendingScanTimeout;

  /**
   * Polls S3 until GuardDuty has tagged the object with a scan result (or a timeout elapses),
   * then transitions the document out of PENDING_SCAN. The frontend observes the result via the
   * long-polling metadata endpoint.
   *
   * <p><b>Caller contract:</b> must be invoked inside an active request context with the
   * uploading user's identity set as {@code runAsUserId} (see {@code RequestContextExecutor}),
   * otherwise the {@code AuditingEntityListener} cannot populate {@code modifiedBy} on the
   * status update.
   */
  public void pollUntilScanned(UUID docId) {
    long deadline = System.currentTimeMillis() + SCAN_POLL_TIMEOUT_MILLIS;
    try {
      while (true) {
        var result = readScanResultSafely(docId);
        if (result != null) {
          updateScanStatus(docId, toScanStatus(result));
          return;
        }
        if (System.currentTimeMillis() >= deadline) {
          log.warn("GuardDuty scan poll timed out for document {}", docId);
          return;
        }
        Thread.sleep(SCAN_POLL_INTERVAL_MILLIS);
      }
    } catch (InterruptedException _) {
      Thread.currentThread().interrupt();
      log.debug("GuardDuty scan poll interrupted for document {}", docId);
    }
  }

  /**
   * Fallback safety net for documents whose in-process scan poller never completed — e.g. the
   * async task started after upload died, or the container crashed/redeployed before the
   * GuardDuty result was observed. Finds documents still in {@code PENDING_SCAN} past
   * {@code pendingScanTimeout} and reconciles each against S3 one final time: applies the real
   * GuardDuty result if the object has since been tagged, otherwise fails closed with
   * {@code SCAN_FAILED}.
   *
   * <p>The pass is idempotent (it only ever moves a row out of {@code PENDING_SCAN}) so it is
   * safe to run repeatedly and to overlap with a late in-process poll. Invoked by
   * {@link ch.agridata.product.job.DataProductScanRecoveryJob}, which serialises execution
   * across containers with a PostgreSQL advisory lock.
   */
  public void recoverStalePendingScans() {
    var cutoff = LocalDateTime.now(clock).minus(pendingScanTimeout);
    var staleDocuments = QuarkusTransaction.requiringNew()
        .call(() -> dataProductDocumentRepository.findStalePendingScans(cutoff));
    if (staleDocuments.isEmpty()) {
      log.debug("No stale PENDING_SCAN documents to recover.");
      return;
    }

    log.info("Recovering {} stale PENDING_SCAN document(s) not scanned within {}.",
        staleDocuments.size(), pendingScanTimeout);
    for (var document : staleDocuments) {
      recoverStalePendingScan(document.getId());
    }
  }

  private void recoverStalePendingScan(UUID docId) {
    GuardDutyScanResultEnum result;
    try {
      result = dataProductDocumentStorageService.readScanResult(docId);
    } catch (RuntimeException e) {
      // Transient read failure: leave the document PENDING_SCAN and retry on the next run
      // rather than failing a document that may actually have been scanned cleanly.
      log.warn("Reading GuardDuty scan result failed for stale document {}: {}", docId, e.getMessage());
      return;
    }
    if (result != null) {
      log.info("Recovered GuardDuty result {} for stale document {}.", result, docId);
      updateScanStatus(docId, toScanStatus(result));
    } else {
      log.warn("Stale document {} was not scanned within {} -> SCAN_FAILED.", docId, pendingScanTimeout);
      updateScanStatus(docId, DocumentScanStatusEnum.SCAN_FAILED);
    }
  }

  /**
   * A transient GetObjectTagging failure must not kill the poll; treat it as not-yet-scanned.
   */
  private GuardDutyScanResultEnum readScanResultSafely(UUID docId) {
    try {
      return dataProductDocumentStorageService.readScanResult(docId);
    } catch (RuntimeException e) {
      log.warn("Reading GuardDuty scan result failed for document {}: {}", docId, e.getMessage());
      return null;
    }
  }

  /**
   * Transitions a document to a terminal scan status, but only while it is still
   * {@code PENDING_SCAN}. The row is locked for update so the in-process poll and the recovery
   * watchdog cannot race: whichever commits first wins and the other becomes a no-op instead of
   * clobbering an already-resolved status. This is what makes {@link #recoverStalePendingScans()}
   * idempotent independently of how the poll and recovery timeouts are tuned.
   */
  private void updateScanStatus(UUID docId, DocumentScanStatusEnum status) {
    QuarkusTransaction.requiringNew().run(() ->
        dataProductDocumentRepository.findByIdForUpdate(docId)
            .filter(entity -> entity.getScanStatus() == DocumentScanStatusEnum.PENDING_SCAN)
            .ifPresent(entity -> entity.setScanStatus(status)));
  }

  private static DocumentScanStatusEnum toScanStatus(GuardDutyScanResultEnum result) {
    return switch (result) {
      case NO_THREATS_FOUND -> DocumentScanStatusEnum.AVAILABLE;
      case THREATS_FOUND -> DocumentScanStatusEnum.REJECTED;
      case UNSUPPORTED, ACCESS_DENIED, FAILED, UNKNOWN -> DocumentScanStatusEnum.SCAN_FAILED;
    };
  }
}
