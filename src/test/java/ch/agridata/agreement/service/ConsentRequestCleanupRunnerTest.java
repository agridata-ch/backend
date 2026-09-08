package ch.agridata.agreement.service;

import static ch.agridata.agreement.service.ConsentRequestCleanupRunner.LOCK_KEY;
import static ch.agridata.agreement.service.ConsentRequestCleanupRunner.USER_ID_SCHEDULED_CLEANUP_JOB;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ch.agridata.agreement.dto.ConsentRequestCleanupOutcomeDto;
import ch.agridata.agreement.dto.ConsentRequestCleanupResultDto;
import ch.agridata.common.persistence.AdvisoryLockExecutor;
import ch.agridata.common.security.AgridataSecurityIdentity;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link ConsentRequestCleanupRunner}. Verifies that the cleanup runs under the
 * shared advisory lock, that the technical cleanup user is applied, that an explicit window is
 * passed through unchanged, that an omitted window defaults to the last two completed days, and
 * that a reversed window is rejected before any state is touched.
 *
 * @CommentLastReviewed 2026-09-08
 */

@ExtendWith(MockitoExtension.class)
class ConsentRequestCleanupRunnerTest {

  private static final ZoneId ZURICH = ZoneId.of("Europe/Zurich");

  @Mock
  AdvisoryLockExecutor advisoryLockExecutor;
  @Mock
  ConsentRequestCleanupService cleanupService;
  @Mock
  AgridataSecurityIdentity agridataSecurityIdentity;

  ConsentRequestCleanupRunner runner;

  @BeforeEach
  void setUp() {
    // "today" = 2026-02-19 (Zurich), so the default window is fromInclusive=2026-02-17, toInclusive=2026-02-18
    Instant fixedNow = LocalDate.of(2026, 2, 19).atStartOfDay(ZURICH).toInstant();
    Clock clock = Clock.fixed(fixedNow, ZURICH);

    runner = new ConsentRequestCleanupRunner(advisoryLockExecutor, cleanupService, agridataSecurityIdentity, clock);
  }

  @Test
  void givenLockAvailable_whenRunCleanupIfNotRunning_thenReturnsResultWithOutcomeOfCleanup() {
    when(cleanupService.cleanup(LocalDate.of(2026, 2, 17), LocalDate.of(2026, 2, 18))).thenReturn(
        ConsentRequestCleanupOutcomeDto.builder()
            .fromInclusive(LocalDate.of(2026, 2, 17))
            .toInclusive(LocalDate.of(2026, 2, 18))
            .terminatedConsentRequestCount(8L)
            .build());
    givenLockIsAvailable();

    var result = runner.runCleanupIfNotRunning(null, null);

    assertThat(result).isPresent();
    assertThat(result.get().fromInclusive()).isEqualTo(LocalDate.of(2026, 2, 17));
    assertThat(result.get().toInclusive()).isEqualTo(LocalDate.of(2026, 2, 18));
    assertThat(result.get().terminatedConsentRequestCount()).isEqualTo(8L);
    assertThat(result.get().durationMs()).isNotNegative();
  }

  @Test
  void givenNullWindow_whenRunCleanupIfNotRunning_thenDefaultsToLastTwoCompletedDays() {
    givenLockIsAvailable();
    when(cleanupService.cleanup(any(), any())).thenReturn(
        ConsentRequestCleanupOutcomeDto.builder()
            .fromInclusive(LocalDate.of(2026, 2, 17))
            .toInclusive(LocalDate.of(2026, 2, 18))
            .terminatedConsentRequestCount(0L)
            .build());

    runner.runCleanupIfNotRunning(null, null);

    verify(cleanupService).cleanup(LocalDate.of(2026, 2, 17), LocalDate.of(2026, 2, 18));
  }

  @Test
  void givenExplicitWindow_whenRunCleanupIfNotRunning_thenPassesItThroughUnchanged() {
    givenLockIsAvailable();
    LocalDate from = LocalDate.of(2026, 1, 1);
    LocalDate to = LocalDate.of(2026, 1, 5);
    when(cleanupService.cleanup(from, to)).thenReturn(
        ConsentRequestCleanupOutcomeDto.builder()
            .fromInclusive(from)
            .toInclusive(to)
            .terminatedConsentRequestCount(0L)
            .build());

    runner.runCleanupIfNotRunning(from, to);

    verify(cleanupService).cleanup(from, to);
  }

  @Test
  void whenRunCleanupIfNotRunning_thenRunsUnderTheSharedLockKeyAsTheTechnicalCleanupUser() {
    when(cleanupService.cleanup(any(), any())).thenReturn(
        ConsentRequestCleanupOutcomeDto.builder()
            .fromInclusive(LocalDate.of(2026, 2, 17))
            .toInclusive(LocalDate.of(2026, 2, 18))
            .terminatedConsentRequestCount(0L)
            .build());
    givenLockIsAvailable();

    runner.runCleanupIfNotRunning(null, null);

    verify(agridataSecurityIdentity).setRunAsUserId(USER_ID_SCHEDULED_CLEANUP_JOB);
    verify(advisoryLockExecutor).runIfLockAvailable(eq(LOCK_KEY), anyString(),
        any(Supplier.class));
  }

  @Test
  void givenLockHeldElsewhere_whenRunCleanupIfNotRunning_thenReturnsEmptyAndDoesNotRunCleanup() {
    when(advisoryLockExecutor.runIfLockAvailable(anyLong(), anyString(), any(Supplier.class)))
        .thenReturn(Optional.empty());

    var result = runner.runCleanupIfNotRunning(null, null);

    assertThat(result).isEmpty();
    verify(cleanupService, never()).cleanup(any(), any());
  }

  @Test
  void givenFromAfterTo_whenRunCleanupIfNotRunning_thenThrowsBadRequestAndTouchesNothing() {
    LocalDate from = LocalDate.of(2026, 2, 20);
    LocalDate to = LocalDate.of(2026, 2, 10);

    assertThatThrownBy(() -> runner.runCleanupIfNotRunning(from, to))
        .isInstanceOf(WebApplicationException.class)
        .extracting(ex -> ((WebApplicationException) ex).getResponse().getStatus())
        .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

    verifyNoInteractions(agridataSecurityIdentity, advisoryLockExecutor, cleanupService);
  }

  @Test
  void givenExplicitFromEqualToExplicitTo_whenRunCleanupIfNotRunning_thenRunsNormally() {
    LocalDate sameDay = LocalDate.of(2026, 2, 10);
    givenLockIsAvailable();
    when(cleanupService.cleanup(sameDay, sameDay)).thenReturn(
        ConsentRequestCleanupOutcomeDto.builder()
            .fromInclusive(sameDay)
            .toInclusive(sameDay)
            .terminatedConsentRequestCount(0L)
            .build());

    var result = runner.runCleanupIfNotRunning(sameDay, sameDay);

    assertThat(result).isPresent();
    verify(cleanupService).cleanup(sameDay, sameDay);
  }

  @SuppressWarnings("unchecked")
  private void givenLockIsAvailable() {
    when(advisoryLockExecutor.runIfLockAvailable(anyLong(), anyString(), any(Supplier.class)))
        .thenAnswer(invocation -> Optional.of(
            ((Supplier<ConsentRequestCleanupResultDto>) invocation.getArgument(2)).get()));
  }
}
