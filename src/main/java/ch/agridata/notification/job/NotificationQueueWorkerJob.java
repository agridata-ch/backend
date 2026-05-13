package ch.agridata.notification.job;

import ch.agridata.common.exceptions.DatabaseConnectionException;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 * Both paths share the same PostgreSQL advisory-lock guard, so only one container instance
 * actually processes the queue at any time. Row-level locking via {@code FOR UPDATE SKIP LOCKED}
 * in the repository provides an additional safety net.</p>
 *
 * @CommentLastReviewed 2026-05-13
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
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

  private final AgridataSecurityIdentity agridataSecurityIdentity;
  private final DataSource dataSource;
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
    long startedAt = System.nanoTime();
    log.info("notification queue worker job started.");

    try (Connection connection = dataSource.getConnection()) {
      if (tryDatabaseJobLock(connection)) {
        log.debug("notification queue worker job acquired advisory lock.");
        tryProcessingBatchesJob(startedAt, connection);
      } else {
        log.debug("notification queue worker job skipped: another instance is already running.");
      }

    } catch (SQLException e) {
      long duration = (System.nanoTime() - startedAt) / 1_000_000;
      log.error("notification queue worker job failed after {} ms due to SQL error.", duration, e);
      throw new DatabaseConnectionException("NotificationQueueWorkerJob cannot create databased connection", e);
    }
  }

  private void tryProcessingBatchesJob(long startedAt, Connection c) throws SQLException {
    try {
      notificationProcessBatchService.processPendingBatches();
      long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
      log.info("notification queue worker job completed in {} ms.", durationMs);
    } catch (Exception e) {
      long duration = (System.nanoTime() - startedAt) / 1_000_000;
      log.error("notification queue worker job failed after {} ms.", duration, e);
      throw e;
    } finally {
      boolean released = unlockDatabaseJob(c);
      if (!released) {
        log.error("notification queue worker job failed to release advisory lock.");
      } else {
        log.debug("notification queue worker job released advisory lock.");
      }
    }
  }

  /**
   * Uses a database lock with the job specific key to makes sure only one container instance can run the job at a time.
   * The lock is automatically released if the connection is closed or if the transaction is rolled back.
   */
  boolean tryDatabaseJobLock(Connection c) throws SQLException {
    try (PreparedStatement ps = c.prepareStatement("select pg_try_advisory_lock(?)")) {
      ps.setLong(1, LOCK_KEY);
      try (ResultSet rs = ps.executeQuery()) {
        rs.next();
        return rs.getBoolean(1);
      }
    }
  }

  boolean unlockDatabaseJob(Connection c) throws SQLException {
    try (PreparedStatement ps = c.prepareStatement("select pg_advisory_unlock(?)")) {
      ps.setLong(1, LOCK_KEY);
      try (ResultSet rs = ps.executeQuery()) {
        rs.next();
        return rs.getBoolean(1);
      }
    }
  }
}
