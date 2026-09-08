package ch.agridata.agreement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.agridata.agis.api.AgisApi;
import ch.agridata.agis.dto.AgisFarmOwnershipDto;
import ch.agridata.agreement.persistence.ConsentRequestRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConsentRequestCleanupServiceTest {

  private static final int BATCH_SIZE = 1000;
  private static final LocalDate FROM = LocalDate.of(2026, 2, 17);
  private static final LocalDate TO = LocalDate.of(2026, 2, 18);

  private ConsentRequestCleanupService consentRequestCleanupService;

  @Mock
  private AgisApi agisApi;
  @Mock
  private ConsentRequestTerminator consentRequestTerminator;

  @BeforeEach
  void setUp() {
    consentRequestCleanupService = new ConsentRequestCleanupService(agisApi, consentRequestTerminator);
  }

  @Test
  void givenExplicitWindow_whenCleanup_thenUsesThatWindowForAgisCalls() {
    when(agisApi.fetchFarmMutations(FROM, TO)).thenReturn(List.of());
    when(agisApi.fetchFarmDeletions(FROM, TO)).thenReturn(List.of());
    when(consentRequestTerminator.terminateFor(List.of(), List.of(), BATCH_SIZE)).thenReturn(0L);

    consentRequestCleanupService.cleanup(FROM, TO);

    verify(agisApi).fetchFarmMutations(FROM, TO);
    verify(agisApi).fetchFarmDeletions(FROM, TO);
  }

  @Test
  void givenTerminatedConsentRequests_whenCleanup_thenReportsWindowAndCount() {
    when(agisApi.fetchFarmMutations(FROM, TO)).thenReturn(List.of());
    when(agisApi.fetchFarmDeletions(FROM, TO)).thenReturn(List.of());
    when(consentRequestTerminator.terminateFor(List.of(), List.of(), BATCH_SIZE)).thenReturn(8L);

    var outcome = consentRequestCleanupService.cleanup(FROM, TO);

    assertThat(outcome.fromInclusive()).isEqualTo(FROM);
    assertThat(outcome.toInclusive()).isEqualTo(TO);
    assertThat(outcome.terminatedConsentRequestCount()).isEqualTo(8L);
  }

  @Test
  void givenNoMutationsAndNoDeletions_whenCleanup_thenTerminatesEmptyLists() {
    when(agisApi.fetchFarmMutations(any(), any())).thenReturn(List.of());
    when(agisApi.fetchFarmDeletions(any(), any())).thenReturn(List.of());
    when(consentRequestTerminator.terminateFor(List.of(), List.of(), BATCH_SIZE)).thenReturn(0L);

    consentRequestCleanupService.cleanup(FROM, TO);

    verify(consentRequestTerminator).terminateFor(List.of(), List.of(), BATCH_SIZE);
  }

  @Test
  void givenMutations_whenCleanup_thenMapsMutationsToBurUidPairsInOrder() {
    var mutations = List.of(
        new AgisFarmOwnershipDto("BUR1", "UID1"),
        new AgisFarmOwnershipDto("BUR2", "UID2")
    );

    when(agisApi.fetchFarmMutations(FROM, TO)).thenReturn(mutations);
    when(agisApi.fetchFarmDeletions(FROM, TO)).thenReturn(List.of());
    when(consentRequestTerminator.terminateFor(any(), any(), eq(BATCH_SIZE)))
        .thenReturn(0L);

    consentRequestCleanupService.cleanup(FROM, TO);

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
  void givenDeletedBurs_whenCleanup_thenPassesDeletedBursWithBatchSize() {
    when(agisApi.fetchFarmMutations(FROM, TO)).thenReturn(List.of());
    when(agisApi.fetchFarmDeletions(FROM, TO)).thenReturn(List.of("BUR3", "BUR4"));

    when(consentRequestTerminator.terminateFor(List.of(), List.of("BUR3", "BUR4"), BATCH_SIZE)).thenReturn(0L);

    consentRequestCleanupService.cleanup(FROM, TO);

    verify(consentRequestTerminator).terminateFor(List.of(), List.of("BUR3", "BUR4"), BATCH_SIZE);
  }
}
