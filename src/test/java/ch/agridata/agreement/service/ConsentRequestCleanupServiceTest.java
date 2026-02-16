package ch.agridata.agreement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.agridata.agis.api.AgisApi;
import ch.agridata.agis.dto.AgisFarmOwnershipDto;
import ch.agridata.agreement.persistence.ConsentRequestRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConsentRequestCleanupServiceTest {

  private static final ZoneId ZURICH = ZoneId.of("Europe/Zurich");
  private static final int BATCH_SIZE = 1000;

  private ConsentRequestCleanupService consentRequestCleanupService;

  @Mock
  private AgisApi agisApi;
  @Mock
  private ConsentRequestTerminator consentRequestTerminator;

  @BeforeEach
  void setUp() {
    // "today" = 2026-02-19 (Zurich), so fromInclusive=2026-02-17, toInclusive=2026-02-18
    Instant fixedNow = LocalDate.of(2026, 2, 19).atStartOfDay(ZURICH).toInstant();
    Clock clock = Clock.fixed(fixedNow, ZURICH);

    consentRequestCleanupService =
        new ConsentRequestCleanupService(agisApi, consentRequestTerminator, clock);
  }

  @Test
  void givenFixedClock_whenCleanupConsentRequestsFromYesterdayAndDayBefore_thenUsesExpectedDateRangeForAgisCalls() {
    LocalDate expectedFrom = LocalDate.of(2026, 2, 17);
    LocalDate expectedTo = LocalDate.of(2026, 2, 18);

    when(agisApi.fetchFarmMutations(expectedFrom, expectedTo)).thenReturn(List.of());
    when(agisApi.fetchFarmDeletions(expectedFrom, expectedTo)).thenReturn(List.of());
    when(consentRequestTerminator.terminateFor(List.of(), List.of(), BATCH_SIZE)).thenReturn(0L);

    consentRequestCleanupService.cleanupConsentRequestsFromYesterdayAndDayBefore();

    verify(agisApi).fetchFarmMutations(expectedFrom, expectedTo);
    verify(agisApi).fetchFarmDeletions(expectedFrom, expectedTo);
  }

  @Test
  void givenNoMutationsAndNoDeletions_whenCleanupConsentRequestsFromYesterdayAndDayBefore_thenTerminatesEmptyLists() {
    when(agisApi.fetchFarmMutations(any(), any())).thenReturn(List.of());
    when(agisApi.fetchFarmDeletions(any(), any())).thenReturn(List.of());
    when(consentRequestTerminator.terminateFor(List.of(), List.of(), BATCH_SIZE)).thenReturn(0L);

    consentRequestCleanupService.cleanupConsentRequestsFromYesterdayAndDayBefore();

    verify(consentRequestTerminator).terminateFor(List.of(), List.of(), BATCH_SIZE);
  }

  @Test
  void givenMutations_whenCleanupConsentRequestsFromYesterdayAndDayBefore_thenMapsMutationsToBurUidPairsInOrder() {
    LocalDate from = LocalDate.of(2026, 2, 17);
    LocalDate to = LocalDate.of(2026, 2, 18);

    var mutations = List.of(
        new AgisFarmOwnershipDto("BUR1", "UID1"),
        new AgisFarmOwnershipDto("BUR2", "UID2")
    );

    when(agisApi.fetchFarmMutations(from, to)).thenReturn(mutations);
    when(agisApi.fetchFarmDeletions(from, to)).thenReturn(List.of());
    when(consentRequestTerminator.terminateFor(any(), any(), eq(BATCH_SIZE)))
        .thenReturn(0L);

    consentRequestCleanupService.cleanupConsentRequestsFromYesterdayAndDayBefore();

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<ConsentRequestRepository.BurUidPair>> captor =
        ArgumentCaptor.forClass((Class) List.class);

    verify(consentRequestTerminator).terminateFor(captor.capture(), eq(List.of()),
        eq(BATCH_SIZE));

    assertThat(captor.getValue()).containsExactly(
        new ConsentRequestRepository.BurUidPair("BUR1", "UID1"),
        new ConsentRequestRepository.BurUidPair("BUR2", "UID2")
    );
  }

  @Test
  void givenDeletedBurs_whenCleanupConsentRequestsFromYesterdayAndDayBefore_thenPassesDeletedBursWithBatchSize() {
    LocalDate from = LocalDate.of(2026, 2, 17);
    LocalDate to = LocalDate.of(2026, 2, 18);

    when(agisApi.fetchFarmMutations(from, to)).thenReturn(List.of());
    when(agisApi.fetchFarmDeletions(from, to)).thenReturn(List.of("BUR3", "BUR4"));

    when(consentRequestTerminator.terminateFor(List.of(), List.of("BUR3", "BUR4"), BATCH_SIZE)).thenReturn(0L);

    consentRequestCleanupService.cleanupConsentRequestsFromYesterdayAndDayBefore();

    verify(consentRequestTerminator).terminateFor(List.of(), List.of("BUR3", "BUR4"), BATCH_SIZE);
  }
}