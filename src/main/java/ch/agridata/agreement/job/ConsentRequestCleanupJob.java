package ch.agridata.agreement.job;

import static ch.agridata.agreement.service.ConsentRequestCleanupRunner.USER_ID_SCHEDULED_CLEANUP_JOB;

import ch.agridata.agreement.service.ConsentRequestCleanupRunner;
import ch.agridata.common.security.BackgroundTaskRunner;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

/**
 * Scheduled job responsible for triggering the periodic cleanup of obsolete
 * consent requests.
 *
 * <p>The job runs once per day at 02:15 (Quartz cron) and delegates to
 * {@link ConsentRequestCleanupRunner}, which owns the cluster-wide advisory lock and is shared with
 * the admin endpoint that triggers the same cleanup manually. This guarantees that at most one
 * cleanup runs at a time, no matter whether it was started by the schedule, by an administrator or
 * on another container instance.
 *
 * @CommentLastReviewed 2026-09-07
 */

@ApplicationScoped
@RequiredArgsConstructor
public class ConsentRequestCleanupJob {

  private final ConsentRequestCleanupRunner cleanupRunner;
  private final BackgroundTaskRunner backgroundTaskRunner;

  // 02:15 every day (Quartz cron)
  @Scheduled(cron = "0 15 2 * * ?")
  public void run() {
    backgroundTaskRunner.runAs(USER_ID_SCHEDULED_CLEANUP_JOB, () -> cleanupRunner.runCleanupIfNotRunning(null, null));
  }
}
