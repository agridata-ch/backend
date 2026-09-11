package ch.agridata.notification.job;

import static ch.agridata.notification.job.NotificationQueueWorkerJob.LOCK_KEY;
import static ch.agridata.notification.job.NotificationQueueWorkerJob.USER_ID_QUEUE_WORKER_JOB;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.agridata.common.persistence.AdvisoryLockExecutor;
import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.notification.service.NotificationProcessBatchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link NotificationQueueWorkerJob}. The advisory-lock behaviour itself is covered
 * by {@code AdvisoryLockExecutorTest}, so this test verifies that the job processes the queue under
 * its own lock key and as its technical user, and that a busy lock skips processing.
 *
 * @CommentLastReviewed 2026-09-07
 */

@ExtendWith(MockitoExtension.class)
class NotificationQueueWorkerJobTest {

  @Mock
  private AdvisoryLockExecutor advisoryLockExecutor;

  @Mock
  private AgridataSecurityIdentity securityIdentity;

  @Mock
  private NotificationProcessBatchService notificationProcessBatchService;

  @InjectMocks
  private NotificationQueueWorkerJob job;

  @Test
  void givenLockAcquired_whenRun_thenDelegatesToProcessBatchServiceAsTechnicalUser() {
    when(advisoryLockExecutor.runIfLockAvailable(anyLong(), anyString(), any(Runnable.class)))
        .thenAnswer(invocation -> {
          invocation.getArgument(2, Runnable.class).run();
          return true;
        });

    job.run();

    verify(securityIdentity).setRunAsUserId(USER_ID_QUEUE_WORKER_JOB);
    verify(advisoryLockExecutor).runIfLockAvailable(eq(LOCK_KEY), anyString(), any(Runnable.class));
    verify(notificationProcessBatchService).processPendingBatches();
  }

  @Test
  void givenLockNotAcquired_whenRun_thenSkipsProcessing() {
    when(advisoryLockExecutor.runIfLockAvailable(anyLong(), anyString(), any(Runnable.class)))
        .thenReturn(false);

    job.run();

    verify(notificationProcessBatchService, never()).processPendingBatches();
  }
}
