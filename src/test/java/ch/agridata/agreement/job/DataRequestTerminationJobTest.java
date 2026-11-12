package ch.agridata.agreement.job;

import static ch.agridata.common.security.SystemActorEnum.DATA_REQUEST_TERMINATION_JOB;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import ch.agridata.agreement.service.DataRequestTerminationRunner;
import ch.agridata.common.security.BackgroundTaskRunner;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link DataRequestTerminationJob}. The job only has to delegate to the shared runner as the technical user.
 *
 * @CommentLastReviewed 2026-11-10
 */

@ExtendWith(MockitoExtension.class)
class DataRequestTerminationJobTest {

  @Mock
  DataRequestTerminationRunner terminationRunner;
  @Mock
  BackgroundTaskRunner backgroundTaskRunner;
  @InjectMocks
  DataRequestTerminationJob job;

  @Test
  void whenRun_thenDelegatesToSharedRunnerAsTechnicalUser() {
    doAnswer(invocation -> invocation.getArgument(1, Supplier.class).get())
        .when(backgroundTaskRunner).runAs(eq(DATA_REQUEST_TERMINATION_JOB.getUserId()), any(Supplier.class));

    job.run();

    verify(terminationRunner).runTerminationIfNotRunning();
  }
}
