package ch.agridata.agreement.service;

import ch.agridata.agreement.dto.DataRequestTerminationResultDto;
import ch.agridata.common.persistence.AdvisoryLockExecutor;
import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.common.security.BackgroundTaskRunner;
import ch.agridata.common.security.SystemActorEnum;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Single entry point for running the data request termination, shared by the scheduled job and the admin endpoint that
 * triggers it manually.
 *
 * <p>Each run goes through a PostgreSQL advisory lock, so it never runs twice in parallel, neither job versus manual trigger nor across
 * container instances.
 *
 * <p>Callers run it as {@link ch.agridata.common.security.SystemActorEnum#DATA_REQUEST_TERMINATION_JOB} via the background task runner,
 * so that {@code modifiedBy} is identical for scheduled and manual runs.
 *
 * @CommentLastReviewed 2026-11-10
 */

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class DataRequestTerminationRunner {

  // Randomly generated long value for the advisory lock key
  public static final long TERMINATION_LOCK_KEY = 4417902285731660921L;

  private static final String TERMINATION_TASK_NAME = "data request termination";

  private final AdvisoryLockExecutor advisoryLockExecutor;
  private final DataRequestTerminator terminator;
  private final BackgroundTaskRunner backgroundTaskRunner;
  private final AgridataSecurityIdentity identity;

  /**
   * Runs the termination on behalf of an administrator who triggered it manually, as the technical termination user. The trigger is
   * logged as the administrator, before the technical user takes over.
   *
   * @return the termination result
   * @throws WebApplicationException with {@code 409 Conflict} if the termination is already running
   */
  public DataRequestTerminationResultDto triggerTerminationManually() {
    var triggeredBy = identity.getUserId();
    log.info("data request termination manually triggered by user {}.", triggeredBy);

    var result = backgroundTaskRunner.runAs(SystemActorEnum.DATA_REQUEST_TERMINATION_JOB.getUserId(), this::runTerminationIfNotRunning)
        .orElseThrow(() -> new WebApplicationException("A data request termination is already running.", Response.Status.CONFLICT));

    log.info(
        "data request termination triggered by user {} terminated {} data requests ({} failed) in {} ms.",
        triggeredBy, result.terminatedCount(), result.failedCount(), result.durationMs()
    );
    return result;
  }

  /**
   * Runs the termination if no other instance or trigger is currently running it.
   *
   * @return the termination result, or {@link Optional#empty()} if the termination was already running
   */
  public Optional<DataRequestTerminationResultDto> runTerminationIfNotRunning() {
    return advisoryLockExecutor.runIfLockAvailable(TERMINATION_LOCK_KEY, TERMINATION_TASK_NAME, terminator::terminateAllExpired);
  }
}
