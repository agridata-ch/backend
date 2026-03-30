package ch.agridata.agreement.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConsentRequestRepositoryTest {

  @Mock
  private EntityManager entityManager;
  @Mock
  private TypedQuery query;
  @InjectMocks
  private ConsentRequestRepository repo;

  @Test
  void givenNullBurs_whenFindIdsToTerminateByDataProducerBurs_thenReturnsEmptyAndDoesNotQuery() {
    var result = repo.findIdsToTerminateByDataProducerBurs(null, 1000);

    assertThat(result).isEmpty();
    verifyNoInteractions(entityManager);
  }

  @Test
  void givenEmptyBurs_whenFindIdsToTerminateByDataProducerBurs_thenReturnsEmptyAndDoesNotQuery() {
    var result = repo.findIdsToTerminateByDataProducerBurs(List.of(), 1000);

    assertThat(result).isEmpty();
    verifyNoInteractions(entityManager);
  }

  @Test
  void givenInvalidBatchSize_whenFindIdsToTerminateByDataProducerBurs_thenThrows() {
    List<String> burs = List.of("BUR1");

    assertThatThrownBy(() -> repo.findIdsToTerminateByDataProducerBurs(burs, 0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Batch size must be greater than 0");

    verifyNoInteractions(entityManager);
  }

  @Test
  void givenBursMoreThanBatchSize_whenFindIdsToTerminateByDataProducerBurs_thenQueriesInBatchesAndReturnsAllIds() {
    @SuppressWarnings("unchecked")
    TypedQuery<UUID> q1 = (TypedQuery<UUID>) mock(TypedQuery.class);
    @SuppressWarnings("unchecked")
    TypedQuery<UUID> q2 = (TypedQuery<UUID>) mock(TypedQuery.class);

    UUID id1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
    UUID id2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
    UUID id3 = UUID.fromString("33333333-3333-3333-3333-333333333333");

    String jpql =
        "select cr.id from ConsentRequestEntity cr " +
            "where cr.archived = false and cr.uidBurRelationUntil is null and cr.dataProducerBur in :burs";

    when(entityManager.createQuery(jpql, UUID.class)).thenReturn(q1, q2);

    when(q1.setParameter("burs", List.of("B1", "B2"))).thenReturn(q1);
    when(q1.getResultList()).thenReturn(List.of(id1));

    when(q2.setParameter("burs", List.of("B3"))).thenReturn(q2);
    when(q2.getResultList()).thenReturn(List.of(id2, id3));

    var result = repo.findIdsToTerminateByDataProducerBurs(List.of("B1", "B2", "B3"), 2);

    assertThat(result).containsExactly(id1, id2, id3);

    verify(entityManager, times(2)).createQuery(jpql, UUID.class);
    verify(q1).setParameter("burs", List.of("B1", "B2"));
    verify(q2).setParameter("burs", List.of("B3"));
  }

  // ------------------------------------------------------------
  // findIdsToTerminateByChangedFarmOwnerships
  // ------------------------------------------------------------

  @Test
  void givenNullOwnerships_whenFindIdsToTerminateByChangedFarmOwnerships_thenReturnsEmptyAndDoesNotQuery() {
    var result = repo.findIdsToTerminateByChangedFarmOwnerships(null);

    assertThat(result).isEmpty();
    verifyNoInteractions(entityManager);
  }

  @Test
  void givenEmptyOwnerships_whenFindIdsToTerminateByChangedFarmOwnerships_thenReturnsEmptyAndDoesNotQuery() {
    var result = repo.findIdsToTerminateByChangedFarmOwnerships(List.of());

    assertThat(result).isEmpty();
    verifyNoInteractions(entityManager);
  }

  @Test
  void givenOnlyNullBurOrUid_whenFindIdsToTerminateByChangedFarmOwnerships_thenReturnsEmptyAndDoesNotQuery() {
    var ownerships = List.of(
        new ConsentRequestRepository.BurUidPair(null, "UID1"),
        new ConsentRequestRepository.BurUidPair("BUR1", null),
        new ConsentRequestRepository.BurUidPair(null, null)
    );

    var result = repo.findIdsToTerminateByChangedFarmOwnerships(ownerships);

    assertThat(result).isEmpty();
    verifyNoInteractions(entityManager);
  }

  @Test
  void givenSingleUidForBur_whenFindIdsToTerminateByChangedFarmOwnerships_thenQueriesUidNotEqual() {
    @SuppressWarnings("unchecked")
    TypedQuery<UUID> q = (TypedQuery<UUID>) mock(TypedQuery.class);

    UUID id1 = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    UUID id2 = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    String jpql =
        "select cr.id from ConsentRequestEntity cr " +
            "where cr.archived = false and cr.uidBurRelationUntil is null and cr.dataProducerBur = :bur and cr.dataProducerUid <> :uid";

    when(entityManager.createQuery(jpql, UUID.class)).thenReturn(q);
    when(q.setParameter("bur", "BUR1")).thenReturn(q);
    when(q.setParameter("uid", "UID1")).thenReturn(q);
    when(q.getResultList()).thenReturn(List.of(id1, id2));

    var result = repo.findIdsToTerminateByChangedFarmOwnerships(
        List.of(new ConsentRequestRepository.BurUidPair("BUR1", "UID1"))
    );

    assertThat(result).containsExactly(id1, id2);

    verify(entityManager).createQuery(jpql, UUID.class);
    verify(q).setParameter("bur", "BUR1");
    verify(q).setParameter("uid", "UID1");
  }

  @Test
  void givenMultipleUidsForSameBur_whenFindIdsToTerminateByChangedFarmOwnerships_thenTerminatesEverythingForThatBur() {
    @SuppressWarnings("unchecked")
    TypedQuery<UUID> q = (TypedQuery<UUID>) mock(TypedQuery.class);

    UUID id1 = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    String jpql =
        "select cr.id from ConsentRequestEntity cr " +
            "where cr.archived = false and cr.uidBurRelationUntil is null and cr.dataProducerBur = :bur";

    when(entityManager.createQuery(jpql, UUID.class)).thenReturn(q);
    when(q.setParameter("bur", "BUR1")).thenReturn(q);
    when(q.getResultList()).thenReturn(List.of(id1));

    var result = repo.findIdsToTerminateByChangedFarmOwnerships(List.of(
        new ConsentRequestRepository.BurUidPair("BUR1", "UID1"),
        new ConsentRequestRepository.BurUidPair("BUR1", "UID2")
    ));

    assertThat(result).containsExactly(id1);

    verify(entityManager).createQuery(jpql, UUID.class);
    verify(q).setParameter("bur", "BUR1");
    verify(q, never()).setParameter(eq("uid"), any());
  }

  @Test
  void givenMultipleBursAndDuplicatesAcrossQueries_whenFindIdsToTerminateByChangedFarmOwnerships_thenReturnsDistinctIds() {
    @SuppressWarnings("unchecked")
    TypedQuery<UUID> qBurOnly = (TypedQuery<UUID>) mock(TypedQuery.class);
    @SuppressWarnings("unchecked")
    TypedQuery<UUID> qBurUidNe = (TypedQuery<UUID>) mock(TypedQuery.class);

    UUID shared = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    UUID onlyFromSecond = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    String jpqlBurOnly =
        "select cr.id from ConsentRequestEntity cr " +
            "where cr.archived = false and cr.uidBurRelationUntil is null and cr.dataProducerBur = :bur";

    String jpqlBurUidNe =
        "select cr.id from ConsentRequestEntity cr " +
            "where cr.archived = false and cr.uidBurRelationUntil is null and cr.dataProducerBur = :bur and cr.dataProducerUid <> :uid";

    // Called once per BUR entry in the loop:
    // - BUR_MULTI has multiple uids => bur-only query
    // - BUR_SINGLE has single uid => bur+uid<> query
    when(entityManager.createQuery(jpqlBurOnly, UUID.class)).thenReturn(qBurOnly);
    when(entityManager.createQuery(jpqlBurUidNe, UUID.class)).thenReturn(qBurUidNe);

    when(qBurOnly.setParameter("bur", "BUR_MULTI")).thenReturn(qBurOnly);
    when(qBurOnly.getResultList()).thenReturn(List.of(shared));

    when(qBurUidNe.setParameter("bur", "BUR_SINGLE")).thenReturn(qBurUidNe);
    when(qBurUidNe.setParameter("uid", "UID_X")).thenReturn(qBurUidNe);
    when(qBurUidNe.getResultList()).thenReturn(List.of(shared, onlyFromSecond));

    var result = repo.findIdsToTerminateByChangedFarmOwnerships(List.of(
        new ConsentRequestRepository.BurUidPair("BUR_MULTI", "UID1"),
        new ConsentRequestRepository.BurUidPair("BUR_MULTI", "UID2"),
        new ConsentRequestRepository.BurUidPair("BUR_SINGLE", "UID_X")
    ));

    assertThat(result).containsExactly(shared, onlyFromSecond);
  }

  // ------------------------------------------------------------
  // terminateByIds
  // ------------------------------------------------------------

  @Test
  void givenNullIds_whenTerminateByIds_thenReturns0AndDoesNotQuery() {
    var result = repo.terminateByIdsReturningPairs(null, 1000, LocalDateTime.now());

    assertThat(result).isEmpty();
    verifyNoInteractions(entityManager);
  }

  @Test
  void givenEmptyIds_whenTerminateByIds_thenReturns0AndDoesNotQuery() {
    var result = repo.terminateByIdsReturningPairs(List.of(), 1000, LocalDateTime.now());

    assertThat(result).isEmpty();
    verifyNoInteractions(entityManager);
  }

  @Test
  void givenInvalidBatchSize_whenTerminateByIds_thenThrows() {
    assertThatThrownBy(() -> repo.terminateByIdsReturningPairs(List.of(UUID.randomUUID()), 0, LocalDateTime.now()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Batch size must be greater than 0");

    verifyNoInteractions(entityManager);
  }

  @Test
  void givenNullTerminatedAt_whenTerminateByIds_thenThrows() {
    assertThatThrownBy(() -> repo.terminateByIdsReturningPairs(List.of(UUID.randomUUID()), 1000, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("terminatedAt must not be null");

    verifyNoInteractions(entityManager);
  }

  @Test
  void givenIdsSpanningBatches_whenTerminateByIdsReturningPairs_thenExecutesPerBatchAndReturnsAllRows() {
    UUID id1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
    UUID id2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
    UUID id3 = UUID.fromString("33333333-3333-3333-3333-333333333333");

    var terminatedAt = LocalDateTime.of(2026, 2, 19, 0, 0);

    when(entityManager.createQuery(anyString(), eq(ConsentRequestEntity.class))).thenReturn(query);
    when(query.setParameter(eq("ids"), any(List.class))).thenReturn(query);

    // Batch 1 => 2 rows, Batch 2 => 1 row
    List<ConsentRequestEntity> batch1Rows = List.of(
        ConsentRequestEntity.builder().id(id1).dataProducerBur("BUR1").dataProducerUid("UID1").build(),
        ConsentRequestEntity.builder().id(id2).dataProducerBur("BUR2").dataProducerUid("UID2").build()
    );
    List<ConsentRequestEntity> batch2Rows = List.of(
        ConsentRequestEntity.builder().id(id3).dataProducerBur("BUR3").dataProducerUid("UID3").build()
    );

    when(query.getResultList()).thenReturn(batch1Rows, batch2Rows);

    var result = repo.terminateByIdsReturningPairs(List.of(id1, id2, id3), 2, terminatedAt);

    assertThat(result)
        .hasSize(3)
        .extracting(ConsentRequestEntity::getId)
        .containsExactlyInAnyOrder(id1, id2, id3);

    verify(entityManager, times(2)).createQuery(anyString(), eq(ConsentRequestEntity.class));
    verify(query, times(2)).setParameter(eq("ids"), any(List.class));
    verify(query, times(2)).getResultList();

    // If you removed clear(): change to never()
    verify(entityManager, never()).flush();
  }

  @Test
  void givenIdsButUpdateReturnsNoRows_whenTerminateByIdsReturningPairs_thenReturnsEmpty() {
    UUID id1 = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    UUID id2 = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    var terminatedAt = LocalDateTime.of(2026, 2, 19, 0, 0);

    when(entityManager.createQuery(anyString(), eq(ConsentRequestEntity.class))).thenReturn(query);
    when(query.setParameter(eq("ids"), any(List.class))).thenReturn(query);

    when(query.getResultList()).thenReturn(List.of());

    var result = repo.terminateByIdsReturningPairs(List.of(id1, id2), 1000, terminatedAt);

    assertThat(result).isEmpty();

    verify(entityManager).createQuery(anyString(), eq(ConsentRequestEntity.class));
    verify(query).setParameter(eq("ids"), any(List.class));
    verify(query).getResultList();

    verify(entityManager, never()).flush();
    verifyNoMoreInteractions(entityManager, query);
  }

  @Test
  void givenIdsSpanningMultipleBatches_whenTerminateByIdsReturningPairs_thenBindsCorrectBatchArrays() {
    // 5 ids, batchSize=2 => 3 batches
    List<UUID> ids = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      ids.add(UUID.fromString(String.format("00000000-0000-0000-0000-%012d", i + 1)));
    }

    var terminatedAt = LocalDateTime.of(2026, 2, 19, 0, 0);

    when(entityManager.createQuery(anyString(), eq(ConsentRequestEntity.class))).thenReturn(query);
    when(query.setParameter(eq("ids"), any(List.class))).thenReturn(query);

    List<ConsentRequestEntity> batch1 = List.of();

    List<ConsentRequestEntity> batch2 = List.of(
        ConsentRequestEntity.builder().id(ids.get(2)).dataProducerBur("BUR3").dataProducerUid("UID3").build(),
        ConsentRequestEntity.builder().id(ids.get(3)).dataProducerBur("BUR4").dataProducerUid("UID4").build()
    );

    List<ConsentRequestEntity> batch3 = Collections.singletonList(
        ConsentRequestEntity.builder().id(ids.get(4)).dataProducerBur("BUR5").dataProducerUid("UID5").build()
    );
    // Pretend results per batch: 0 rows, 2 rows, 1 row => total 3 records
    when(query.getResultList()).thenReturn(
        batch1,
        batch2,
        batch3
    );

    ArgumentCaptor<List<UUID>> idsCaptor = ArgumentCaptor.forClass(List.class);

    var result = repo.terminateByIdsReturningPairs(ids, 2, terminatedAt);

    assertThat(result).hasSize(3);

    verify(entityManager, times(3)).createQuery(anyString(), eq(ConsentRequestEntity.class));
    verify(query, times(3)).setParameter(eq("ids"), idsCaptor.capture());
    verify(query, times(3)).getResultList();

    List<List<UUID>> captured = idsCaptor.getAllValues();
    assertThat(captured).hasSize(3);
    assertThat(captured.get(0)).containsExactly(ids.get(0), ids.get(1));
    assertThat(captured.get(1)).containsExactly(ids.get(2), ids.get(3));
    assertThat(captured.get(2)).containsExactly(ids.get(4));

    verify(entityManager, never()).flush();
  }

}
