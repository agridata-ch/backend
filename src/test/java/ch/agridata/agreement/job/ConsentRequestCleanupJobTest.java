package ch.agridata.agreement.job;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.agridata.agreement.service.ConsentRequestCleanupService;
import ch.agridata.common.exceptions.DatabaseConnectionException;
import ch.agridata.common.security.AgridataSecurityIdentity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConsentRequestCleanupJobTest {

  @Mock
  DataSource dataSource;
  @Mock
  Connection connection;
  @Mock
  ConsentRequestCleanupService cleanupService;
  @Mock
  PreparedStatement tryLockPs;
  @Mock
  PreparedStatement unlockPs;
  @Mock
  ResultSet tryLockRs;
  @Mock
  ResultSet unlockRs;
  @Mock
  AgridataSecurityIdentity agridataSecurityIdentity;
  @InjectMocks
  ConsentRequestCleanupJob job;

  @BeforeEach
  void setUp() throws Exception {
    when(dataSource.getConnection()).thenReturn(connection);
  }

  @Test
  void givenLockNotAcquired_whenRun_thenDoesNotExecuteCleanupAndDoesNotUnlock() throws Exception {
    when(connection.prepareStatement("select pg_try_advisory_lock(?)")).thenReturn(tryLockPs);
    when(tryLockPs.executeQuery()).thenReturn(tryLockRs);
    when(tryLockRs.next()).thenReturn(true);
    when(tryLockRs.getBoolean(1)).thenReturn(false);

    job.run();

    verify(cleanupService, never()).cleanupConsentRequestsFromYesterdayAndDayBefore();
    verify(connection, never()).prepareStatement("select pg_advisory_unlock(?)");
  }

  @Test
  void givenLockAcquired_whenRun_thenExecutesCleanupAndUnlocks() throws Exception {
    when(connection.prepareStatement("select pg_try_advisory_lock(?)")).thenReturn(tryLockPs);
    when(tryLockPs.executeQuery()).thenReturn(tryLockRs);
    when(tryLockRs.next()).thenReturn(true);
    when(tryLockRs.getBoolean(1)).thenReturn(true);

    when(connection.prepareStatement("select pg_advisory_unlock(?)")).thenReturn(unlockPs);
    when(unlockPs.executeQuery()).thenReturn(unlockRs);
    when(unlockRs.next()).thenReturn(true);
    when(unlockRs.getBoolean(1)).thenReturn(true);

    job.run();

    InOrder inOrder = inOrder(dataSource, connection, tryLockPs, tryLockRs, cleanupService, unlockPs, unlockRs);

    inOrder.verify(dataSource).getConnection();

    inOrder.verify(connection).prepareStatement("select pg_try_advisory_lock(?)");
    inOrder.verify(tryLockPs).setLong(1, ConsentRequestCleanupJob.LOCK_KEY);
    inOrder.verify(tryLockPs).executeQuery();
    inOrder.verify(tryLockRs).next();
    inOrder.verify(tryLockRs).getBoolean(1);

    inOrder.verify(cleanupService).cleanupConsentRequestsFromYesterdayAndDayBefore();

    inOrder.verify(connection).prepareStatement("select pg_advisory_unlock(?)");
    inOrder.verify(unlockPs).setLong(1, ConsentRequestCleanupJob.LOCK_KEY);
    inOrder.verify(unlockPs).executeQuery();
    inOrder.verify(unlockRs).next();
    inOrder.verify(unlockRs).getBoolean(1);

    inOrder.verify(connection).close();
  }

  @Test
  void givenLockAcquiredAndCleanupThrows_whenRun_thenStillUnlocksAndRethrows() throws Exception {
    when(connection.prepareStatement("select pg_try_advisory_lock(?)")).thenReturn(tryLockPs);
    when(tryLockPs.executeQuery()).thenReturn(tryLockRs);
    when(tryLockRs.next()).thenReturn(true);
    when(tryLockRs.getBoolean(1)).thenReturn(true);

    when(connection.prepareStatement("select pg_advisory_unlock(?)")).thenReturn(unlockPs);
    when(unlockPs.executeQuery()).thenReturn(unlockRs);
    when(unlockRs.next()).thenReturn(true);
    when(unlockRs.getBoolean(1)).thenReturn(true);

    doThrow(new RuntimeException("failure")).when(cleanupService).cleanupConsentRequestsFromYesterdayAndDayBefore();

    assertThatThrownBy(job::run).isInstanceOf(RuntimeException.class).hasMessage("failure");

    verify(connection).prepareStatement("select pg_advisory_unlock(?)");
    verify(unlockPs).setLong(1, ConsentRequestCleanupJob.LOCK_KEY);
    verify(unlockPs).executeQuery();
    verify(unlockRs).next();
    verify(unlockRs).getBoolean(1);

    verify(connection).close();
  }

  @Test
  void givenLockAcquiredButUnlockReturnsFalse_whenRun_thenStillCompletesNormally() throws Exception {
    when(connection.prepareStatement("select pg_try_advisory_lock(?)")).thenReturn(tryLockPs);
    when(tryLockPs.executeQuery()).thenReturn(tryLockRs);
    when(tryLockRs.next()).thenReturn(true);
    when(tryLockRs.getBoolean(1)).thenReturn(true);

    when(connection.prepareStatement("select pg_advisory_unlock(?)")).thenReturn(unlockPs);
    when(unlockPs.executeQuery()).thenReturn(unlockRs);
    when(unlockRs.next()).thenReturn(true);
    when(unlockRs.getBoolean(1)).thenReturn(false);

    job.run();

    verify(cleanupService).cleanupConsentRequestsFromYesterdayAndDayBefore();
    verify(connection).close();
  }

  @Test
  void givenSqlExceptionDuringTryLock_whenRun_thenWrapsInRuntimeExceptionAndClosesConnection() throws Exception {
    when(connection.prepareStatement("select pg_try_advisory_lock(?)")).thenThrow(new SQLException("failure"));

    assertThatThrownBy(job::run).isInstanceOf(DatabaseConnectionException.class).hasCauseInstanceOf(SQLException.class);

    verify(cleanupService, never()).cleanupConsentRequestsFromYesterdayAndDayBefore();
    verify(connection).close();
  }
}
