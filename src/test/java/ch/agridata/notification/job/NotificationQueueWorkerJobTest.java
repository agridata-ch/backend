package ch.agridata.notification.job;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.agridata.common.exceptions.DatabaseConnectionException;
import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.notification.service.NotificationProcessBatchService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link NotificationQueueWorkerJob}.
 * Verifies advisory-lock acquisition, delegation to {@link NotificationProcessBatchService},
 * and proper lock-release on both success and failure paths.
 *
 * @CommentLastReviewed 2026-04-29
 */
@ExtendWith(MockitoExtension.class)
class NotificationQueueWorkerJobTest {

  @InjectMocks
  private NotificationQueueWorkerJob job;

  @Mock
  private AgridataSecurityIdentity securityIdentity;

  @Mock
  private DataSource dataSource;

  @Mock
  private NotificationProcessBatchService notificationProcessBatchService;

  @Mock
  private Connection connection;

  @Mock
  private PreparedStatement lockStatement;

  @Mock
  private PreparedStatement unlockStatement;

  @Mock
  private ResultSet lockResultSet;

  @Mock
  private ResultSet unlockResultSet;

  @BeforeEach
  void setUpConnection() throws SQLException {
    when(dataSource.getConnection()).thenReturn(connection);
    // lenient: unlock result set is only consumed when the lock was acquired
    lenient().when(connection.prepareStatement(anyString())).thenAnswer(inv -> {
      String sql = inv.getArgument(0);
      return sql.contains("pg_try_advisory_lock") ? lockStatement : unlockStatement;
    });
    lenient().when(lockStatement.executeQuery()).thenReturn(lockResultSet);
    lenient().when(unlockStatement.executeQuery()).thenReturn(unlockResultSet);
    lenient().when(unlockResultSet.next()).thenReturn(true);
    lenient().when(unlockResultSet.getBoolean(1)).thenReturn(true);
  }

  @Test
  void givenLockAcquired_whenRun_thenDelegatesToProcessBatchService() throws SQLException {
    when(lockResultSet.next()).thenReturn(true);
    when(lockResultSet.getBoolean(1)).thenReturn(true);

    job.run();

    verify(notificationProcessBatchService).processPendingBatches();
  }

  @Test
  void givenLockNotAcquired_whenRun_thenSkipsProcessing() throws SQLException {
    when(lockResultSet.next()).thenReturn(true);
    when(lockResultSet.getBoolean(1)).thenReturn(false);

    job.run();

    verify(notificationProcessBatchService, never()).processPendingBatches();
  }

  @Test
  void givenLockAcquired_whenServiceThrows_thenExceptionPropagatedAndLockReleased() throws SQLException {
    when(lockResultSet.next()).thenReturn(true);
    when(lockResultSet.getBoolean(1)).thenReturn(true);
    doThrow(new RuntimeException("processing error")).when(notificationProcessBatchService).processPendingBatches();

    assertThatThrownBy(() -> job.run()).isInstanceOf(RuntimeException.class).hasMessage("processing error");

    verify(unlockStatement).executeQuery();
  }

  @Test
  void givenLockAcquired_whenServiceThrows_thenExceptionPropagatedAndUnlockAttempted_butAdvisoryLockNotReleased() throws SQLException {
    when(lockResultSet.next()).thenReturn(true);
    when(lockResultSet.getBoolean(1)).thenReturn(true);
    when(unlockResultSet.getBoolean(1)).thenReturn(false);
    doThrow(new RuntimeException("processing error")).when(notificationProcessBatchService).processPendingBatches();

    assertThatThrownBy(() -> job.run()).isInstanceOf(RuntimeException.class).hasMessage("processing error");

    verify(unlockStatement).executeQuery();
  }

  @Test
  void givenSqlExceptionOnGetConnection_whenRun_thenWrappedAsRuntimeException() throws SQLException {
    when(dataSource.getConnection()).thenThrow(new SQLException("db down"));

    assertThatThrownBy(() -> job.run()).isInstanceOf(DatabaseConnectionException.class).hasCauseInstanceOf(SQLException.class);

    verify(notificationProcessBatchService, never()).processPendingBatches();
  }
}
