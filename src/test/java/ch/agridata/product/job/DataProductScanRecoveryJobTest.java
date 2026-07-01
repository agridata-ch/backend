package ch.agridata.product.job;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.agridata.common.exceptions.DatabaseConnectionException;
import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.product.service.DataProductDocumentScanService;
import ch.agridata.product.service.DataProductDocumentService;
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

/**
 * Unit tests for {@link DataProductScanRecoveryJob}. Verifies advisory-lock acquisition,
 * delegation to {@link DataProductDocumentService}, and proper lock release on both the success
 * and failure paths.
 */

@ExtendWith(MockitoExtension.class)
class DataProductScanRecoveryJobTest {

  @Mock
  DataSource dataSource;
  @Mock
  Connection connection;
  @Mock
  DataProductDocumentScanService dataProductDocumentScanService;
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
  DataProductScanRecoveryJob job;

  @BeforeEach
  void setUp() throws Exception {
    when(dataSource.getConnection()).thenReturn(connection);
  }

  @Test
  void givenLockNotAcquired_whenRun_thenDoesNotRecoverAndDoesNotUnlock() throws Exception {
    when(connection.prepareStatement("select pg_try_advisory_lock(?)")).thenReturn(tryLockPs);
    when(tryLockPs.executeQuery()).thenReturn(tryLockRs);
    when(tryLockRs.next()).thenReturn(true);
    when(tryLockRs.getBoolean(1)).thenReturn(false);

    job.run();

    verify(dataProductDocumentScanService, never()).recoverStalePendingScans();
    verify(connection, never()).prepareStatement("select pg_advisory_unlock(?)");
  }

  @Test
  void givenLockAcquired_whenRun_thenRecoversAndUnlocks() throws Exception {
    when(connection.prepareStatement("select pg_try_advisory_lock(?)")).thenReturn(tryLockPs);
    when(tryLockPs.executeQuery()).thenReturn(tryLockRs);
    when(tryLockRs.next()).thenReturn(true);
    when(tryLockRs.getBoolean(1)).thenReturn(true);

    when(connection.prepareStatement("select pg_advisory_unlock(?)")).thenReturn(unlockPs);
    when(unlockPs.executeQuery()).thenReturn(unlockRs);
    when(unlockRs.next()).thenReturn(true);
    when(unlockRs.getBoolean(1)).thenReturn(true);

    job.run();

    InOrder inOrder = inOrder(dataSource, connection, tryLockPs, tryLockRs, dataProductDocumentScanService, unlockPs, unlockRs);

    inOrder.verify(dataSource).getConnection();

    inOrder.verify(connection).prepareStatement("select pg_try_advisory_lock(?)");
    inOrder.verify(tryLockPs).setLong(1, DataProductScanRecoveryJob.LOCK_KEY);
    inOrder.verify(tryLockPs).executeQuery();
    inOrder.verify(tryLockRs).next();
    inOrder.verify(tryLockRs).getBoolean(1);

    inOrder.verify(dataProductDocumentScanService).recoverStalePendingScans();

    inOrder.verify(connection).prepareStatement("select pg_advisory_unlock(?)");
    inOrder.verify(unlockPs).setLong(1, DataProductScanRecoveryJob.LOCK_KEY);
    inOrder.verify(unlockPs).executeQuery();
    inOrder.verify(unlockRs).next();
    inOrder.verify(unlockRs).getBoolean(1);

    inOrder.verify(connection).close();
  }

  @Test
  void givenLockAcquiredAndRecoveryThrows_whenRun_thenStillUnlocksAndRethrows() throws Exception {
    when(connection.prepareStatement("select pg_try_advisory_lock(?)")).thenReturn(tryLockPs);
    when(tryLockPs.executeQuery()).thenReturn(tryLockRs);
    when(tryLockRs.next()).thenReturn(true);
    when(tryLockRs.getBoolean(1)).thenReturn(true);

    when(connection.prepareStatement("select pg_advisory_unlock(?)")).thenReturn(unlockPs);
    when(unlockPs.executeQuery()).thenReturn(unlockRs);
    when(unlockRs.next()).thenReturn(true);
    when(unlockRs.getBoolean(1)).thenReturn(true);

    doThrow(new RuntimeException("failure")).when(dataProductDocumentScanService).recoverStalePendingScans();

    assertThatThrownBy(job::run).isInstanceOf(RuntimeException.class).hasMessage("failure");

    verify(connection).prepareStatement("select pg_advisory_unlock(?)");
    verify(unlockPs).setLong(1, DataProductScanRecoveryJob.LOCK_KEY);
    verify(unlockPs).executeQuery();
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

    verify(dataProductDocumentScanService).recoverStalePendingScans();
    verify(connection).close();
  }

  @Test
  void givenSqlExceptionDuringTryLock_whenRun_thenWrapsInDatabaseConnectionExceptionAndClosesConnection() throws Exception {
    when(connection.prepareStatement("select pg_try_advisory_lock(?)")).thenThrow(new SQLException("failure"));

    assertThatThrownBy(job::run).isInstanceOf(DatabaseConnectionException.class).hasCauseInstanceOf(SQLException.class);

    verify(dataProductDocumentScanService, never()).recoverStalePendingScans();
    verify(connection).close();
  }
}
