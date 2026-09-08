package ch.agridata.notification.job;

import ch.agridata.common.persistence.AdvisoryLockExecutor;
import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.notification.service.NotificationBatchQueuedEvent;
import ch.agridata.notification.service.NotificationProcessBatchService;
import io.quarkus.arc.Arc;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.context.api.ManagedExecutorConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.context.ThreadContext;

/**
 * Scheduled job that processes PENDING notification batches. For each batch, every recipient is
 * processed in its own independent ({@code REQUIRES_NEW}) transaction via
 * {@link NotificationProcessBatchService}, ensuring that a crash mid-batch does not cause
 * already-dispatched emails or already-created inbox entries to be duplicated on the next run.
 *
 * <p>The job is triggered from two sources:
 * <ul>
 *   <li>The cron schedule below — the safety net that catches any batch that was not picked up
 *       immediately.</li>
 *   <li>A {@link NotificationBatchQueuedEvent} fired after a producer transaction commits
 *       ({@link #onBatchQueued}) — runs the same processing routine on a managed executor thread
 *       so the originating HTTP request is not blocked.</li>
 * </ul>
 * Both paths share the same PostgreSQL advisory-lock guard via {@link AdvisoryLockExecutor}, so only
 * one container instance actually processes the queue at any time. Row-level locking via
 * {@code FOR UPDATE SKIP LOCKED} in the repository provides an additional safety net.</p>
 *
 * @CommentLastReviewed 2026-09-07
 */
@ApplicationScoped
@RequiredArgsConstructor
public class NotificationQueueWorkerJob {

  /**
   * Randomly generated long value for the advisory lock key.
   */
  public static final long LOCK_KEY = 8416790142436573057L;
  /**
   * Dedicated technical user ID for this scheduled job (traceability/auditing).
   * The corresponding user entry must exist in the users table.
   */
  public static final UUID USER_ID_QUEUE_WORKER_JOB = UUID.fromString("9fc651e8-def0-4456-a8d4-7d4c9d3dfc04");
  public static final String TASK_NAME = "notification queue worker job";

  private final AgridataSecurityIdentity agridataSecurityIdentity;
  private final AdvisoryLockExecutor advisoryLockExecutor;
  private final NotificationProcessBatchService notificationProcessBatchService;

  @ManagedExecutorConfig(
      propagated = {},
      cleared = ThreadContext.ALL_REMAINING
  )
  private final ManagedExecutor managedExecutor;

  @Scheduled(cron = "0 0/10 * * * ?")
  @ActivateRequestContext
  public void run() {
    agridataSecurityIdentity.setRunAsUserId(USER_ID_QUEUE_WORKER_JOB);
    tryAcquireLockAndProcess();
  }

  /**
   * Observes a {@link NotificationBatchQueuedEvent} fired after a producer transaction commits
   * and triggers processing immediately on a managed executor thread, so the originating HTTP
   * request is not blocked by the SES dispatch round-trips. A fresh request context is activated
   * and the technical user is set so that auditing attributes such as {@code modifiedBy} are
   * populated correctly even though the original request context is no longer active.
   */
  void onBatchQueued(@Observes(during = TransactionPhase.AFTER_SUCCESS) NotificationBatchQueuedEvent event) {
    managedExecutor.submit(() -> {
      var requestContext = Arc.container().requestContext();
      requestContext.activate();
      try {
        agridataSecurityIdentity.setRunAsUserId(USER_ID_QUEUE_WORKER_JOB);
        tryAcquireLockAndProcess();
      } finally {
        requestContext.deactivate();
      }
    });
  }

  private void tryAcquireLockAndProcess() {
    advisoryLockExecutor.runIfLockAvailable(LOCK_KEY, TASK_NAME, notificationProcessBatchService::processPendingBatches);
  }
}
