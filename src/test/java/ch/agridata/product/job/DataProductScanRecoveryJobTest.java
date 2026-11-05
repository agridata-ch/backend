package ch.agridata.product.job;

import static ch.agridata.product.job.DataProductScanRecoveryJob.LOCK_KEY;
import static ch.agridata.product.job.DataProductScanRecoveryJob.USER_ID_DOCUMENT_SCAN_RECOVERY_JOB;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.agridata.common.persistence.AdvisoryLockExecutor;
import ch.agridata.common.security.BackgroundTaskRunner;
import ch.agridata.product.service.DataProductDocumentScanService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link DataProductScanRecoveryJob}. The advisory-lock behaviour itself is covered
 * by {@code AdvisoryLockExecutorTest}, so this test verifies that the job runs the recovery under
 * its own lock key and as its technical user.
 *
 * @CommentLastReviewed 2026-09-07
 */

@ExtendWith(MockitoExtension.class)
class DataProductScanRecoveryJobTest {

  @Mock
  AdvisoryLockExecutor advisoryLockExecutor;
  @Mock
  DataProductDocumentScanService dataProductDocumentScanService;
  @Mock
  BackgroundTaskRunner backgroundTaskRunner;
  @InjectMocks
  DataProductScanRecoveryJob job;

  @Test
  void whenRun_thenRecoversUnderOwnLockKeyAsTechnicalUser() {
    doAnswer(invocation -> {
      invocation.getArgument(1, Runnable.class).run();
      return null;
    }).when(backgroundTaskRunner).runInNewRequest(eq(USER_ID_DOCUMENT_SCAN_RECOVERY_JOB), any(Runnable.class));
    when(advisoryLockExecutor.runIfLockAvailable(anyLong(), anyString(), any(Runnable.class)))
        .thenAnswer(invocation -> {
          invocation.getArgument(2, Runnable.class).run();
          return true;
        });

    job.run();

    verify(backgroundTaskRunner).runInNewRequest(eq(USER_ID_DOCUMENT_SCAN_RECOVERY_JOB), any(Runnable.class));
    verify(advisoryLockExecutor).runIfLockAvailable(eq(LOCK_KEY), anyString(), any(Runnable.class));
    verify(dataProductDocumentScanService).recoverStalePendingScans();
  }
}
