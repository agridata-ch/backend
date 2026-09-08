package ch.agridata.common.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.agridata.common.exceptions.DatabaseConnectionException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link AdvisoryLockExecutor}. Verifies advisory-lock acquisition, task execution
 * and lock release on both the success and the failure path.
 *
 * @CommentLastReviewed 2026-09-07
 */

@ExtendWith(MockitoExtension.class)
class AdvisoryLockExecutorTest {

  private static final long LOCK_KEY = 42L;
  private static final String TASK_NAME = "test task";

  @Mock
  DataSource dataSource;
  @Mock
  Connection connection;
  @Mock
  PreparedStatement tryLockPs;
  @Mock
  PreparedStatement unlockPs;
  @Mock
  ResultSet tryLockRs;
  @Mock
  ResultSet unlockRs;
  @InjectMocks
  AdvisoryLockExecutor advisoryLockExecutor;

  @BeforeEach
  void setUp() throws Exception {
    when(dataSource.getConnection()).thenReturn(connection);
  }

  @Test
  void givenLockNotAcquired_whenRunIfLockAvailable_thenSkipsTaskAndDoesNotUnlock() throws Exception {
    givenTryLockReturns(false);
    var executed = new AtomicBoolean(false);

    var result = advisoryLockExecutor.runIfLockAvailable(LOCK_KEY, TASK_NAME, () -> {
      executed.set(true);
      return "value";
    });

    assertThat(result).isEmpty();
    assertThat(executed).isFalse();
    verify(connection, never()).prepareStatement("select pg_advisory_unlock(?)");
    verify(connection).close();
  }

  @Test
  void givenLockAcquired_whenRunIfLockAvailable_thenExecutesTaskReturnsResultAndUnlocks() throws Exception {
    givenTryLockReturns(true);
    givenUnlockReturns(true);

    var result = advisoryLockExecutor.runIfLockAvailable(LOCK_KEY, TASK_NAME, () -> "value");

    assertThat(result).contains("value");

    InOrder inOrder = inOrder(dataSource, connection, tryLockPs, tryLockRs, unlockPs, unlockRs);
    inOrder.verify(dataSource).getConnection();
    inOrder.verify(connection).prepareStatement("select pg_try_advisory_lock(?)");
    inOrder.verify(tryLockPs).setLong(1, LOCK_KEY);
    inOrder.verify(tryLockPs).executeQuery();
    inOrder.verify(tryLockRs).next();
    inOrder.verify(tryLockRs).getBoolean(1);
    inOrder.verify(connection).prepareStatement("select pg_advisory_unlock(?)");
    inOrder.verify(unlockPs).setLong(1, LOCK_KEY);
    inOrder.verify(unlockPs).executeQuery();
    inOrder.verify(unlockRs).next();
    inOrder.verify(unlockRs).getBoolean(1);
    inOrder.verify(connection).close();
  }

  @Test
  void givenLockAcquiredAndTaskThrows_whenRunIfLockAvailable_thenStillUnlocksAndRethrows() throws Exception {
    givenTryLockReturns(true);
    givenUnlockReturns(true);

    assertThatThrownBy(() -> advisoryLockExecutor.runIfLockAvailable(LOCK_KEY, TASK_NAME, () -> {
      throw new RuntimeException("failure");
    }))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("failure");

    verify(connection).prepareStatement("select pg_advisory_unlock(?)");
    verify(unlockPs).setLong(1, LOCK_KEY);
    verify(unlockPs).executeQuery();
    verify(connection).close();
  }

  @Test
  void givenUnlockReturnsFalse_whenRunIfLockAvailable_thenStillCompletesNormally() throws Exception {
    givenTryLockReturns(true);
    givenUnlockReturns(false);

    var result = advisoryLockExecutor.runIfLockAvailable(LOCK_KEY, TASK_NAME, () -> "value");

    assertThat(result).contains("value");
    verify(connection).close();
  }

  @Test
  void givenSqlExceptionDuringTryLock_whenRunIfLockAvailable_thenWrapsInDatabaseConnectionExceptionAndClosesConnection()
      throws Exception {
    when(connection.prepareStatement("select pg_try_advisory_lock(?)")).thenThrow(new SQLException("failure"));
    var executed = new AtomicBoolean(false);

    assertThatThrownBy(() -> advisoryLockExecutor.runIfLockAvailable(LOCK_KEY, TASK_NAME, () -> {
      executed.set(true);
      return "value";
    }))
        .isInstanceOf(DatabaseConnectionException.class)
        .hasCauseInstanceOf(SQLException.class);

    assertThat(executed).isFalse();
    verify(connection).close();
  }

  @Test
  void givenRunnableTaskAndLockAcquired_whenRunIfLockAvailable_thenRunsTaskAndReturnsTrue() throws Exception {
    givenTryLockReturns(true);
    givenUnlockReturns(true);
    var executed = new AtomicBoolean(false);

    boolean executedByLock = advisoryLockExecutor.runIfLockAvailable(LOCK_KEY, TASK_NAME, () -> executed.set(true));

    assertThat(executedByLock).isTrue();
    assertThat(executed).isTrue();
  }

  @Test
  void givenRunnableTaskAndLockNotAcquired_whenRunIfLockAvailable_thenSkipsTaskAndReturnsFalse() throws Exception {
    givenTryLockReturns(false);
    var executed = new AtomicBoolean(false);

    boolean executedByLock = advisoryLockExecutor.runIfLockAvailable(LOCK_KEY, TASK_NAME, () -> executed.set(true));

    assertThat(executedByLock).isFalse();
    assertThat(executed).isFalse();
  }

  private void givenTryLockReturns(boolean acquired) throws SQLException {
    when(connection.prepareStatement("select pg_try_advisory_lock(?)")).thenReturn(tryLockPs);
    when(tryLockPs.executeQuery()).thenReturn(tryLockRs);
    when(tryLockRs.next()).thenReturn(true);
    when(tryLockRs.getBoolean(1)).thenReturn(acquired);
  }

  private void givenUnlockReturns(boolean released) throws SQLException {
    when(connection.prepareStatement("select pg_advisory_unlock(?)")).thenReturn(unlockPs);
    when(unlockPs.executeQuery()).thenReturn(unlockRs);
    when(unlockRs.next()).thenReturn(true);
    when(unlockRs.getBoolean(1)).thenReturn(released);
  }
}
