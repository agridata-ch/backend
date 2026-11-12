package ch.agridata.agreement.job;

import ch.agridata.agreement.service.DataRequestTerminationRunner;
import ch.agridata.common.security.BackgroundTaskRunner;
import ch.agridata.common.security.SystemActorEnum;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

/**
 * Scheduled job that sets data requests to terminated once their termination period has expired, which stops the data exchange.
 *
 * <p>The job runs once per day at 02:30 (Quartz cron) and delegates to {@link DataRequestTerminationRunner}, which owns the cluster-wide
 * advisory lock and is shared with the admin endpoint that triggers the same run manually.
 *
 * @CommentLastReviewed 2026-11-10
 */

@ApplicationScoped
@RequiredArgsConstructor
public class DataRequestTerminationJob {

  private final DataRequestTerminationRunner terminationRunner;
  private final BackgroundTaskRunner backgroundTaskRunner;

  // 02:30 every day (Quartz cron)
  @Scheduled(cron = "0 30 2 * * ?")
  public void run() {
    backgroundTaskRunner.runAs(SystemActorEnum.DATA_REQUEST_TERMINATION_JOB.getUserId(), terminationRunner::runTerminationIfNotRunning);
  }
}
