package ch.agridata.product.job;

import ch.agridata.common.exceptions.DatabaseConnectionException;
import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.product.service.DataProductDocumentScanService;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Fallback safety net that recovers documents stuck in {@code PENDING_SCAN} when the in-process
 * GuardDuty poll (started per upload in {@link DataProductDocumentScanService}) never completes — for
 * example because its virtual thread died or the container crashed/redeployed before the scan
 * result was observed. The job delegates the actual reconciliation to
 * {@link DataProductDocumentScanService#recoverStalePendingScans()}.
 *
 * <p>To ensure safe execution in a clustered environment with multiple application instances
 * sharing one PostgreSQL database, the job uses a PostgreSQL advisory lock:
 * <ul>
 *   <li>Before execution, it attempts to acquire a database-level advisory lock.</li>
 *   <li>If the lock cannot be acquired, another instance is already running the job and execution
 *       is skipped.</li>
 *   <li>After completion, the lock is released.</li>
 * </ul>
 *
 * <p>This guarantees that at most one instance performs recovery at a time. The recovery pass is
 * itself idempotent, so an occasional overlap with a still-running in-process poll is harmless.
 *
 * @CommentLastReviewed 2026-07-02
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class DataProductScanRecoveryJob {
  // Randomly generated long value for the advisory lock key.
  public static final long LOCK_KEY = 8961254816372858291L;
  // Dedicated technical user ID for this scheduled job (traceability/auditing).
  // The corresponding user entry must exist in the users table.
  public static final UUID USER_ID_DOCUMENT_SCAN_RECOVERY_JOB =
      UUID.fromString("5bb8cb23-6091-4dc1-b2c0-58aac774f832");

  private final AgridataSecurityIdentity agridataSecurityIdentity;
  private final DataSource dataSource;
  private final DataProductDocumentScanService dataProductDocumentScanService;

  // Every 5 minutes (Quartz cron)
  @Scheduled(cron = "0 0/5 * * * ?")
  @ActivateRequestContext
  public void run() {
    agridataSecurityIdentity.setRunAsUserId(USER_ID_DOCUMENT_SCAN_RECOVERY_JOB);
    long startedAt = System.nanoTime();
    log.info("data product scan recovery job started.");

    try (Connection c = dataSource.getConnection()) {
      if (!tryLock(c)) {
        log.info("data product scan recovery job skipped: another instance is already running.");
        return;
      }

      log.info("data product scan recovery job acquired advisory lock.");
      executeRecovery(startedAt, c);

    } catch (SQLException e) {
      long duration = (System.nanoTime() - startedAt) / 1_000_000;
      log.error("data product scan recovery job failed after {} ms due to SQL error.", duration, e);
      throw new DatabaseConnectionException("DataProductScanRecoveryJob cannot create database connection", e);
    }
  }

  private void executeRecovery(long startedAt, Connection c) throws SQLException {
    try {
      dataProductDocumentScanService.recoverStalePendingScans();

      long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
      log.info("data product scan recovery job completed in {} ms.", durationMs);
    } catch (Exception e) {
      long duration = (System.nanoTime() - startedAt) / 1_000_000;
      log.error("data product scan recovery job failed after {} ms.", duration, e);
      throw e;
    } finally {
      boolean success = unlock(c);
      if (!success) {
        log.warn("data product scan recovery job failed to release advisory lock.");
      }
    }
  }

  boolean tryLock(Connection c) throws SQLException {
    try (PreparedStatement ps = c.prepareStatement("select pg_try_advisory_lock(?)")) {
      ps.setLong(1, LOCK_KEY);
      try (ResultSet rs = ps.executeQuery()) {
        rs.next();
        return rs.getBoolean(1);
      }
    }
  }

  boolean unlock(Connection c) throws SQLException {
    try (PreparedStatement ps = c.prepareStatement("select pg_advisory_unlock(?)")) {
      ps.setLong(1, LOCK_KEY);
      try (ResultSet rs = ps.executeQuery()) {
        rs.next();
        return rs.getBoolean(1);
      }
    }
  }
}
