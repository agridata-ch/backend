package ch.agridata.agreement.job;

import static org.mockito.Mockito.verify;

import ch.agridata.agreement.service.ConsentRequestCleanupRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link ConsentRequestCleanupJob}. The advisory-lock behaviour itself is covered by
 * {@code AdvisoryLockExecutorTest} and {@code ConsentRequestCleanupRunnerTest}, so this test only
 * verifies that the schedule delegates to the shared runner.
 *
 * @CommentLastReviewed 2026-09-07
 */

@ExtendWith(MockitoExtension.class)
class ConsentRequestCleanupJobTest {

  @Mock
  ConsentRequestCleanupRunner cleanupRunner;
  @InjectMocks
  ConsentRequestCleanupJob job;

  @Test
  void whenRun_thenDelegatesToSharedCleanupRunner() {
    job.run();

    verify(cleanupRunner).runCleanupIfNotRunning(null, null);
  }
}
