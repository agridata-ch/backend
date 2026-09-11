package ch.agridata.agreement.service;

import ch.agridata.agreement.dto.ConsentRequestCleanupResultDto;
import ch.agridata.common.persistence.AdvisoryLockExecutor;
import ch.agridata.common.security.AgridataSecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

/**
 * Single entry point for running the consent request cleanup, shared by the scheduled job and the
 * admin endpoint that triggers the cleanup manually.
 *
 * <p>Both callers go through the same PostgreSQL advisory lock, so the cleanup never runs twice in
 * parallel — neither job versus manual trigger, nor across container instances.
 *
 * <p>The technical cleanup user is applied for every run, regardless of who triggered it, so that
 * the {@code modifiedBy} of the affected consent requests is identical for scheduled and manual
 * runs. Note that this overrides the identity of the current request context: callers that need the
 * triggering user must read it before invoking this runner.
 *
 * <p>The cleanup window defaults to the last two completed days (matching the scheduled job's
 * original behaviour) when {@code fromInclusive}/{@code toInclusive} are {@code null}, but a caller
 * may pass an explicit window — e.g. to catch up on days the scheduled job missed.
 *
 * @CommentLastReviewed 2026-09-08
 */

@ApplicationScoped
@RequiredArgsConstructor
public class ConsentRequestCleanupRunner {

  // Randomly generated long value for the advisory lock key
  public static final long LOCK_KEY = 7182194963550522386L;
  // Dedicated technical user ID for the cleanup (traceability/auditing).
  // The corresponding user entry must exist in the users table.
  public static final UUID USER_ID_SCHEDULED_CLEANUP_JOB = UUID.fromString("3899f61d-c517-40da-a4a6-f2b062cc0f20");

  private static final String TASK_NAME = "consent request cleanup";
  private static final int DEFAULT_WINDOW_DAYS = 2;

  private final AdvisoryLockExecutor advisoryLockExecutor;
  private final ConsentRequestCleanupService cleanupService;
  private final AgridataSecurityIdentity agridataSecurityIdentity;
  private final Clock clock;

  /**
   * Runs the cleanup if no other instance or trigger is currently running it.
   *
   * @param fromInclusive first day of the window to evaluate (inclusive), or {@code null} to
   *                       default to two days ago
   * @param toInclusive    last day of the window to evaluate (inclusive), or {@code null} to
   *                       default to yesterday
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
          Response.Status.BAD_REQUEST);
    }

    agridataSecurityIdentity.setRunAsUserId(USER_ID_SCHEDULED_CLEANUP_JOB);

    long startedAt = System.nanoTime();

    return advisoryLockExecutor.runIfLockAvailable(LOCK_KEY, TASK_NAME, () -> {
      var outcome = cleanupService.cleanup(effectiveFrom, effectiveTo);

      return ConsentRequestCleanupResultDto.builder()
          .fromInclusive(outcome.fromInclusive())
          .toInclusive(outcome.toInclusive())
          .terminatedConsentRequestCount(outcome.terminatedConsentRequestCount())
          .durationMs((System.nanoTime() - startedAt) / 1_000_000)
          .build();
    });
  }
}
