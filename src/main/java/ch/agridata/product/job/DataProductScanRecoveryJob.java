package ch.agridata.product.job;

import ch.agridata.common.persistence.AdvisoryLockExecutor;
import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.product.service.DataProductDocumentScanService;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

/**
 * Fallback safety net that recovers documents stuck in {@code PENDING_SCAN} when the in-process
 * GuardDuty poll (started per upload in {@link DataProductDocumentScanService}) never completes — for
 * example because its virtual thread died or the container crashed/redeployed before the scan
 * result was observed. The job delegates the actual reconciliation to
 * {@link DataProductDocumentScanService#recoverStalePendingScans()}.
 *
 * <p>To ensure safe execution in a clustered environment with multiple application instances
 * sharing one PostgreSQL database, the job runs through {@link AdvisoryLockExecutor}, which acquires
 * a database-level advisory lock for this job and skips execution while another instance holds it.
 *
 * <p>This guarantees that at most one instance performs recovery at a time. The recovery pass is
 * itself idempotent, so an occasional overlap with a still-running in-process poll is harmless.
 *
 * @CommentLastReviewed 2026-09-07
 */
@ApplicationScoped
@RequiredArgsConstructor
public class DataProductScanRecoveryJob {
  // Randomly generated long value for the advisory lock key.
  public static final long LOCK_KEY = 8961254816372858291L;
  // Dedicated technical user ID for this scheduled job (traceability/auditing).
  // The corresponding user entry must exist in the users table.
  public static final UUID USER_ID_DOCUMENT_SCAN_RECOVERY_JOB = UUID.fromString("5bb8cb23-6091-4dc1-b2c0-58aac774f832");
  public static final String TASK_NAME = "data product scan recovery job";

  private final AgridataSecurityIdentity agridataSecurityIdentity;
  private final AdvisoryLockExecutor advisoryLockExecutor;
  private final DataProductDocumentScanService dataProductDocumentScanService;

  // Every 5 minutes (Quartz cron)
  @Scheduled(cron = "0 0/5 * * * ?")
  @ActivateRequestContext
  public void run() {
    agridataSecurityIdentity.setRunAsUserId(USER_ID_DOCUMENT_SCAN_RECOVERY_JOB);

    advisoryLockExecutor.runIfLockAvailable(LOCK_KEY, TASK_NAME, dataProductDocumentScanService::recoverStalePendingScans);
  }
}
