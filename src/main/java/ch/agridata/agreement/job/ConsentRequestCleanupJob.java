package ch.agridata.agreement.job;

import ch.agridata.agreement.service.ConsentRequestCleanupService;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Scheduled job responsible for triggering the periodic cleanup of obsolete
 * consent requests.
 *
 * <p>The job runs once per day at 02:15 (Quartz cron) and delegates the actual
 * cleanup logic to {@link ConsentRequestCleanupService}.
 *
 * <p>To ensure safe execution in a clustered environment with multiple
 * application instances, the job uses a PostgreSQL advisory lock:
 * <ul>
 *   <li>Before execution, it attempts to acquire a database-level advisory lock.</li>
 *   <li>If the lock cannot be acquired, another instance is already running
 *       the job and execution is skipped.</li>
 *   <li>After completion, the lock is released.</li>
 * </ul>
 *
 * <p>This guarantees that at most one instance performs the cleanup at a time
 * while keeping the implementation lightweight and database-coordinated.
 *
 * @CommentLastReviewed 2026-02-23
 */

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class ConsentRequestCleanupJob {
  // Generated from the first 16 hex characters of: echo -n "ConsentRequestCleanupJob" | sha256sum
  public static final long LOCK_KEY = 7182194963550522386L;

  private final DataSource dataSource;
  private final ConsentRequestCleanupService cleanupService;

  // 02:15 every day (Quartz cron)
  @Scheduled(cron = "0 15 2 * * ?")
  void run() {
    long startedAt = System.nanoTime();
    log.info("consent request cleanup job started.");

    try (Connection c = dataSource.getConnection()) {
      if (!tryLock(c)) {
        log.info("consent request cleanup job skipped: another instance is already running.");
        return;
      } else {
        log.info("consent request cleanup job acquired advisory lock.");
      }

      try {
        cleanupService.cleanupConsentRequestsFromYesterdayAndDayBefore();

        long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
        log.info("consent request cleanup job completed in {} ms.", durationMs);
      } catch (Exception e) {
        long duration = (System.nanoTime() - startedAt) / 1_000_000;
        log.error("consent request cleanup job failed after {} ms.", duration, e);
        throw e;
      } finally {
        boolean success = unlock(c);
        if (!success) {
          log.warn("consent request cleanup job failed to release advisory lock.");
        }
      }
    } catch (SQLException e) {
      long duration = (System.nanoTime() - startedAt) / 1_000_000;
      log.error("consent request cleanup job failed after {} ms due to SQL error.", duration, e);
      throw new RuntimeException(e);
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
