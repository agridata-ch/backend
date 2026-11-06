package ch.agridata.agreement.job;

import static ch.agridata.agreement.service.ConsentRequestCleanupRunner.USER_ID_SCHEDULED_CLEANUP_JOB;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import ch.agridata.agreement.service.ConsentRequestCleanupRunner;
import ch.agridata.common.security.BackgroundTaskRunner;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link ConsentRequestCleanupJob}. The advisory-lock behaviour itself is covered by
 * {@code AdvisoryLockExecutorTest} and {@code ConsentRequestCleanupRunnerTest}, so this test only
 * verifies that the schedule delegates to the shared runner as the technical cleanup user.
 *
 * @CommentLastReviewed 2026-11-03
 */

@ExtendWith(MockitoExtension.class)
class ConsentRequestCleanupJobTest {

  @Mock
  ConsentRequestCleanupRunner cleanupRunner;
  @Mock
  BackgroundTaskRunner backgroundTaskRunner;
  @InjectMocks
  ConsentRequestCleanupJob job;

  @Test
  void whenRun_thenDelegatesToSharedCleanupRunnerAsTechnicalUser() {
    doAnswer(invocation -> invocation.getArgument(1, Supplier.class).get())
        .when(backgroundTaskRunner).runAs(eq(USER_ID_SCHEDULED_CLEANUP_JOB), any(Supplier.class));

    job.run();

    verify(cleanupRunner).runCleanupIfNotRunning(null, null);
  }
}
