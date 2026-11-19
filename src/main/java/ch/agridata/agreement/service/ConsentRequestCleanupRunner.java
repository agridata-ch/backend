package ch.agridata.agreement.service;

import ch.agridata.agreement.dto.ConsentRequestCleanupResultDto;
import ch.agridata.common.persistence.AdvisoryLockExecutor;
import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.common.security.BackgroundTaskRunner;
import ch.agridata.common.security.SystemActorEnum;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Single entry point for running the consent request cleanup, shared by the scheduled job and the
 * admin endpoint that triggers the cleanup manually.
 *
 * <p>Both callers go through the same PostgreSQL advisory lock, so the cleanup never runs twice in
 * parallel — neither job versus manual trigger, nor across container instances.
 *
 * <p>Callers are expected to run this under {@link SystemActorEnum#CONSENT_REQUEST_CLEANUP_JOB} (see
 * {@code BackgroundTaskRunner}), regardless of who triggered it, so that the {@code modifiedBy} of
 * the affected consent requests is identical for scheduled and manual runs.
 *
 * <p>The cleanup window defaults to the last two completed days (matching the scheduled job's
 * original behaviour) when {@code fromInclusive}/{@code toInclusive} are {@code null}, but a caller
 * may pass an explicit window — e.g. to catch up on days the scheduled job missed.
 *
 * @CommentLastReviewed 2026-09-08
 */

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class ConsentRequestCleanupRunner {

  // Randomly generated long value for the advisory lock key
  public static final long LOCK_KEY = 7182194963550522386L;

  private static final String TASK_NAME = "consent request cleanup";
  private static final int DEFAULT_WINDOW_DAYS = 2;

  private final AdvisoryLockExecutor advisoryLockExecutor;
  private final ConsentRequestCleanupService cleanupService;
  private final Clock clock;
  private final BackgroundTaskRunner backgroundTaskRunner;
  private final AgridataSecurityIdentity identity;

  /**
   * Runs the cleanup on behalf of an administrator who triggered it manually, as the technical cleanup user. The trigger is logged
   * as the administrator, before the technical user takes over.
   *
   * @return the cleanup result
   * @throws WebApplicationException with {@code 409 Conflict} if the cleanup is already running
   */
  public ConsentRequestCleanupResultDto triggerCleanupJobManually(LocalDate fromInclusive, LocalDate toInclusive) {
    var triggeredBy = identity.getUserId();
    log.info("consent request cleanup manually triggered by user {}.", triggeredBy);

    var result = backgroundTaskRunner.runAs(
            SystemActorEnum.CONSENT_REQUEST_CLEANUP_JOB.getUserId(),
            () -> runCleanupIfNotRunning(fromInclusive, toInclusive)
        )
        .orElseThrow(() -> new WebApplicationException("A consent request cleanup is already running.", Response.Status.CONFLICT));

    log.info(
        "consent request cleanup triggered by user {} terminated {} consent requests for {} - {} in {} ms.",
        triggeredBy,
        result.terminatedConsentRequestCount(),
        result.fromInclusive(),
        result.toInclusive(),
        result.durationMs()
    );
    return result;
  }

  /**
   * Runs the cleanup if no other instance or trigger is currently running it.
   *
   * @param fromInclusive first day of the window to evaluate (inclusive), or {@code null} to
   *                      default to two days ago
   * @param toInclusive   last day of the window to evaluate (inclusive), or {@code null} to
   *                      default to yesterday
   * @return the cleanup result, or {@link Optional#empty()} if the cleanup was already running
   * @throws WebApplicationException with {@code 400 Bad Request} if {@code fromInclusive} is after
   *                                 {@code toInclusive}
   */
  public Optional<ConsentRequestCleanupResultDto> runCleanupIfNotRunning(LocalDate fromInclusive, LocalDate toInclusive) {
    LocalDate today = LocalDate.now(clock);
    LocalDate effectiveFrom = fromInclusive != null ? fromInclusive : today.minusDays(DEFAULT_WINDOW_DAYS);
    LocalDate effectiveTo = toInclusive != null ? toInclusive : today.minusDays(1);

    if (effectiveFrom.isAfter(effectiveTo)) {
      throw new WebApplicationException(
          "fromInclusive (%s) must not be after toInclusive (%s).".formatted(effectiveFrom, effectiveTo),
          Response.Status.BAD_REQUEST
      );
    }

    long startedAt = System.nanoTime();

    return advisoryLockExecutor.runIfLockAvailable(
        LOCK_KEY, TASK_NAME, () -> {
          var outcome = cleanupService.cleanup(effectiveFrom, effectiveTo);

          return ConsentRequestCleanupResultDto.builder()
              .fromInclusive(outcome.fromInclusive())
              .toInclusive(outcome.toInclusive())
              .terminatedConsentRequestCount(outcome.terminatedConsentRequestCount())
              .durationMs((System.nanoTime() - startedAt) / 1_000_000)
              .build();
        }
    );
  }
}
