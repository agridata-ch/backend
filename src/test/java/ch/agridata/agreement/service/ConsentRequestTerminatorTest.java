package ch.agridata.agreement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ch.agridata.agreement.persistence.ConsentRequestEntity;
import ch.agridata.agreement.persistence.ConsentRequestRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConsentRequestTerminatorTest {

  private static final int BATCH_SIZE = 1000;

  @Mock
  private ConsentRequestRepository consentRequestRepository;
  @Mock
  private Clock clock;
  @Mock
  private AuditingService auditingService;
  @InjectMocks
  private ConsentRequestTerminator terminator;

  @Test
  void givenOwnershipAndDeletionIdsWithDuplicates_whenTerminateFor_thenMergesDistinctInEncounterOrderAndTerminatesOnce() {
    UUID id1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
    UUID id2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
    UUID id3 = UUID.fromString("33333333-3333-3333-3333-333333333333");

    var ownershipPairs = List.of(new ConsentRequestRepository.BurUidPair("BUR1", "UID1"));
    var deletedBurs = List.of("BUR9");

    ZoneId zone = ZoneId.of("Europe/Zurich");
    Instant instant = Instant.parse("2026-02-19T10:15:30Z");
    when(clock.getZone()).thenReturn(zone);
    when(clock.instant()).thenReturn(instant);
    LocalDateTime terminatedAt = LocalDateTime.ofInstant(instant, zone);

    when(consentRequestRepository.findIdsToTerminateByChangedFarmOwnerships(ownershipPairs))
        .thenReturn(List.of(id1, id2));
    when(consentRequestRepository.findIdsToTerminateByDataProducerBurs(deletedBurs, BATCH_SIZE))
        .thenReturn(List.of(id2, id3));

    var terminatedRows = List.of(
        ConsentRequestEntity.builder().id(id1).dataProducerBur("BUR1").dataProducerUid("UID1").build(),
        ConsentRequestEntity.builder().id(id2).dataProducerBur("BUR2").dataProducerUid("UID2").build(),
        ConsentRequestEntity.builder().id(id3).dataProducerBur("BUR3").dataProducerUid("UID3").build()
    );

    when(consentRequestRepository.terminateByIdsReturningPairs(List.of(id1, id2, id3), BATCH_SIZE, terminatedAt))
        .thenReturn(terminatedRows);

    long terminated = terminator.terminateFor(ownershipPairs, deletedBurs, BATCH_SIZE);

    assertThat(terminated).isEqualTo(3L);
    verify(consentRequestRepository).terminateByIdsReturningPairs(List.of(id1, id2, id3), BATCH_SIZE, terminatedAt);
    verify(auditingService).logDataRequestTerminated(id1);
    verify(auditingService).logDataRequestTerminated(id2);
    verify(auditingService).logDataRequestTerminated(id3);
  }

  @Test
  void givenNoIdsToArchive_whenTerminateFor_thenDoesNotTerminateAndReturnsZero() {
    var ownershipPairs = List.of(new ConsentRequestRepository.BurUidPair("BUR1", "UID1"));
    var deletedBurs = List.of("BUR9");

    when(consentRequestRepository.findIdsToTerminateByChangedFarmOwnerships(ownershipPairs))
        .thenReturn(List.of());
    when(consentRequestRepository.findIdsToTerminateByDataProducerBurs(deletedBurs, BATCH_SIZE))
        .thenReturn(List.of());

    long terminated = terminator.terminateFor(ownershipPairs, deletedBurs, BATCH_SIZE);

    assertThat(terminated).isZero();

    verify(consentRequestRepository).findIdsToTerminateByChangedFarmOwnerships(ownershipPairs);
    verify(consentRequestRepository).findIdsToTerminateByDataProducerBurs(deletedBurs, BATCH_SIZE);
    verifyNoInteractions(auditingService);
  }
}
