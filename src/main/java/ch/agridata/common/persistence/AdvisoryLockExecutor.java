package ch.agridata.common.persistence;

import ch.agridata.common.exceptions.DatabaseConnectionException;
import jakarta.enterprise.context.ApplicationScoped;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Supplier;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Runs a task at most once across all application instances by guarding it with a PostgreSQL
 * advisory lock.
 *
 * <p>Callers provide a lock key that identifies the task cluster-wide:
 * <ul>
 *   <li>Before execution, a database-level advisory lock is acquired for that key.</li>
 *   <li>If the lock cannot be acquired, another instance (or another trigger on this instance)
 *       is already running the task and execution is skipped.</li>
 *   <li>After completion the lock is released, also when the task failed.</li>
 * </ul>
 *
 * <p>This keeps cluster coordination lightweight and database-owned: the lock is bound to the
 * connection and is released automatically if the connection is closed or the instance dies.
 *
 * @CommentLastReviewed 2026-09-07
 */

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class AdvisoryLockExecutor {

  private static final String TRY_LOCK_SQL = "select pg_try_advisory_lock(?)";
  private static final String UNLOCK_SQL = "select pg_advisory_unlock(?)";

  private final DataSource dataSource;

  /**
   * Runs the given task if the advisory lock for {@code lockKey} is available.
   *
   * @param taskName human-readable task name, used for logging only
   * @return the task result, or {@link Optional#empty()} if the lock was held elsewhere
   */
  public <T> Optional<T> runIfLockAvailable(long lockKey, String taskName, Supplier<T> task) {
    long startedAt = System.nanoTime();
    log.info("{} started.", taskName);

    try (Connection connection = dataSource.getConnection()) {
      if (!tryLock(connection, lockKey)) {
        log.info("{} skipped: another instance is already running.", taskName);
        return Optional.empty();
      }

      log.debug("{} acquired advisory lock.", taskName);
      return Optional.ofNullable(execute(connection, lockKey, taskName, startedAt, task));

    } catch (SQLException e) {
      log.error("{} failed after {} ms due to SQL error.", taskName, durationMs(startedAt), e);
      throw new DatabaseConnectionException(taskName + " cannot create database connection", e);
    }
  }

  /**
   * Runs the given task if the advisory lock for {@code lockKey} is available.
   *
   * @return {@code true} if the task was executed, {@code false} if the lock was held elsewhere
   */
  public boolean runIfLockAvailable(long lockKey, String taskName, Runnable task) {
    return runIfLockAvailable(lockKey, taskName, () -> {
      task.run();
      return Boolean.TRUE;
    }).isPresent();
  }

  private <T> T execute(Connection connection, long lockKey, String taskName, long startedAt, Supplier<T> task)
      throws SQLException {
    try {
      T result = task.get();
      log.info("{} completed in {} ms.", taskName, durationMs(startedAt));
      return result;
    } catch (Exception e) {
      log.error("{} failed after {} ms.", taskName, durationMs(startedAt), e);
      throw e;
    } finally {
      if (!unlock(connection, lockKey)) {
        log.warn("{} failed to release advisory lock.", taskName);
      } else {
        log.debug("{} released advisory lock.", taskName);
      }
    }
  }

  private boolean tryLock(Connection connection, long lockKey) throws SQLException {
    return executeLockStatement(connection, TRY_LOCK_SQL, lockKey);
  }

  private boolean unlock(Connection connection, long lockKey) throws SQLException {
    return executeLockStatement(connection, UNLOCK_SQL, lockKey);
  }

  private boolean executeLockStatement(Connection connection, String sql, long lockKey) throws SQLException {
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setLong(1, lockKey);
      try (ResultSet rs = ps.executeQuery()) {
        rs.next();
        return rs.getBoolean(1);
      }
    }
  }

  private long durationMs(long startedAt) {
    return (System.nanoTime() - startedAt) / 1_000_000;
  }
}
