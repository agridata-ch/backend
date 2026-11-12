package ch.agridata.product.job;

import ch.agridata.common.persistence.AdvisoryLockExecutor;
import ch.agridata.common.security.BackgroundTaskRunner;
import ch.agridata.common.security.SystemActorEnum;
import ch.agridata.product.service.DataProductDocumentScanService;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
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
  public static final String TASK_NAME = "data product scan recovery job";

  private final BackgroundTaskRunner backgroundTaskRunner;
  private final AdvisoryLockExecutor advisoryLockExecutor;
  private final DataProductDocumentScanService dataProductDocumentScanService;

  // Every 5 minutes (Quartz cron)
  @Scheduled(cron = "0 0/5 * * * ?")
  public void run() {
    backgroundTaskRunner.runAs(
        SystemActorEnum.DOCUMENT_SCAN_RECOVERY_JOB.getUserId(),
        () -> advisoryLockExecutor.runIfLockAvailable(LOCK_KEY, TASK_NAME, dataProductDocumentScanService::recoverStalePendingScans)
    );
  }
}
