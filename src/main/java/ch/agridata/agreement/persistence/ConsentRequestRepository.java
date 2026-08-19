package ch.agridata.agreement.persistence;

import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.GRANTED;

import ch.agridata.common.persistence.BaseSearchRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

/**
 * Manages persistence of consent requests. It supports querying and updating consent-related records.
 *
 * @CommentLastReviewed 2026-02-26
 */

@ApplicationScoped
@RequiredArgsConstructor
public class ConsentRequestRepository extends BaseSearchRepository<ConsentRequestEntity, UUID> {
  private final EntityManager entityManager;

  //region "UID and BUR" based queries

  public List<ConsentRequestEntity> findActiveUidAndBurBasedByDataProducerUidsWithDataRequest(List<String> dataProducerUids) {
    return entityManager.createQuery(
            "SELECT cr FROM ConsentRequestEntity cr "
                + "JOIN FETCH cr.dataRequest dr "
                + "WHERE cr.dataProducerUid IN :uids "
                + "AND cr.uidBurRelationUntil IS NULL "
                + "ORDER BY cr.id", ConsentRequestEntity.class
        )
        .setParameter("uids", dataProducerUids)
        .getResultList();
  }

  public Optional<ConsentRequestEntity> findActiveUidAndBurBasedByIdAndDataProducerUids(UUID id, List<String> dataProducerUids) {
    return find(
        "id = :id and dataProducerUid IN :dataProducerUids and uidBurRelationUntil is null",
        Map.of(
            "id", id,
            "dataProducerUids", dataProducerUids
        )
    ).firstResultOptional();
  }

  public List<ConsentRequestEntity> findActiveUidAndBurBasedByDataRequestIdAndDataProducerUid(UUID dataRequestId, String dataProducerUid) {
    return find(
        "dataRequest.id = :dataRequestId and dataProducerUid = :dataProducerUid and uidBurRelationUntil is null",
        Map.of(
            "dataRequestId", dataRequestId,
            "dataProducerUid", dataProducerUid
        )
    ).list();
  }

  //endregion

  //region "Only UID" based queries

  public List<ConsentRequestEntity> findUidBasedByDataProducerUids(List<String> dataProducerUids) {
    return find("dataProducerUid IN ?1 and dataProducerBur is null", dataProducerUids).list();
  }

  public Optional<ConsentRequestEntity> findUidBasedByDataRequestIdAndDataProducerUid(UUID dataRequestId, String dataProducerUid) {
    return find(
        "dataRequest.id = :dataRequestId and dataProducerUid = :dataProducerUid and dataProducerBur is null",
        Map.of(
            "dataRequestId", dataRequestId,
            "dataProducerUid", dataProducerUid
        )
    ).firstResultOptional();
  }

  /**
   * Loads the UID consent request (the {@code dataProducerBur is null} row) with a pessimistic write lock. Concurrent BUR status
   * updates for the same (data request, UID) all sync through this single row, so locking it serializes them and prevents the
   * write-skew that would otherwise let the last committer overwrite the UID state from a stale snapshot of its sibling BUR rows.
   */
  public Optional<ConsentRequestEntity> findAndLockUidBasedByDataRequestIdAndDataProducerUid(UUID dataRequestId, String dataProducerUid) {
    return find(
        "dataRequest.id = :dataRequestId and dataProducerUid = :dataProducerUid and dataProducerBur is null",
        Map.of(
            "dataRequestId", dataRequestId,
            "dataProducerUid", dataProducerUid
        )
    ).withLock(LockModeType.PESSIMISTIC_WRITE).firstResultOptional();
  }

  public List<ConsentRequestEntity> findUidBasedByDataRequestIdAndDataProducerUids(UUID dataRequestId,
                                                                                   List<String> dataProducerUids) {
    return find(
        "dataRequest.id = :dataRequestId and dataProducerUid IN :dataProducerUids and dataProducerBur is null",
        Sort.by("id"),
        Map.of(
            "dataRequestId", dataRequestId,
            "dataProducerUids", dataProducerUids
        )
    ).list();
  }

  //endregion

  //region "Only BUR" based queries

  public List<ConsentRequestEntity> findActiveBurBasedByDataRequestIdAndDataProducerBurs(UUID dataRequestId,
                                                                                         List<String> dataProducerBurs) {
    return find(
        "dataRequest.id = :dataRequestId and dataProducerBur IN :dataProducerBurs and uidBurRelationUntil is null",
        Map.of(
            "dataRequestId", dataRequestId,
            "dataProducerBurs", dataProducerBurs
        )
    ).list();
  }

  public List<String> findGrantedUidBasedUidsForProductOfConsumerSince(
      UUID productId,
      String dataConsumerUid,
      LocalDateTime since
  ) {
    return entityManager.createQuery(
            "SELECT DISTINCT cr.dataProducerUid "
                + "FROM ConsentRequestEntity cr "
                + "JOIN cr.dataRequest dr "
                + "JOIN dr.dataProducts dp "
                + "WHERE dr.dataConsumerUid = :dataConsumerUid "
                + "AND dp.dataProductId = :productId "
                + "AND cr.stateCode = :stateCode "
                + "AND cr.lastStateChangeDate > :since "
                + "AND cr.dataProducerBur IS NULL", String.class
        )
        .setParameter("dataConsumerUid", dataConsumerUid)
        .setParameter("productId", productId)
        .setParameter("stateCode", GRANTED)
        .setParameter("since", since)
        .getResultList();
  }

  //endregion

  //region Termination queries

  public List<UUID> findIdsToTerminateByDataProducerBurs(List<String> burs, int batchSize) {
    if (burs == null || burs.isEmpty()) {
      return List.of();
    }
    if (batchSize <= 0) {
      throw new IllegalArgumentException("Batch size must be greater than 0");
    }

    var ids = new java.util.ArrayList<UUID>();

    for (int i = 0; i < burs.size(); i += batchSize) {
      List<String> batch = burs.subList(i, Math.min(i + batchSize, burs.size()));

      ids.addAll(entityManager.createQuery(
              "select cr.id from ConsentRequestEntity cr "
                  + "where cr.archived = false and cr.uidBurRelationUntil is null and cr.dataProducerBur in :burs",
              UUID.class
          )
          .setParameter("burs", batch)
          .getResultList());
    }

    return ids;
  }

  public List<UUID> findIdsToTerminateByChangedFarmOwnerships(List<BurUidPair> currentFarmOwnerships) {
    if (currentFarmOwnerships == null || currentFarmOwnerships.isEmpty()) {
      return List.of();
    }

    Map<String, Set<String>> burToUids = currentFarmOwnerships.stream()
        .filter(d -> d.bur() != null && d.uid() != null)
        .collect(Collectors.groupingBy(
            BurUidPair::bur,
            Collectors.mapping(BurUidPair::uid, Collectors.toSet())
        ));

    if (burToUids.isEmpty()) {
      return List.of();
    }

    List<UUID> ids = new java.util.ArrayList<>();

    for (var entry : burToUids.entrySet()) {
      String bur = entry.getKey();
      Set<String> uids = entry.getValue();

      if (uids.size() > 1) {
        // archive everything for that BUR
        ids.addAll(entityManager.createQuery(
                "select cr.id from ConsentRequestEntity cr "
                    + "where cr.archived = false and cr.uidBurRelationUntil is null and cr.dataProducerBur = :bur",
                UUID.class
            )
            .setParameter("bur", bur)
            .getResultList());
      } else {
        String uid = uids.iterator().next();
        ids.addAll(entityManager.createQuery(
                "select cr.id from ConsentRequestEntity cr "
                    + "where cr.archived = false and cr.uidBurRelationUntil is null "
                    + "and cr.dataProducerBur = :bur and cr.dataProducerUid <> :uid",
                UUID.class
            )
            .setParameter("bur", bur)
            .setParameter("uid", uid)
            .getResultList());
      }
    }

    return ids.stream().distinct().toList();
  }

  public List<ConsentRequestEntity> terminateByIdsReturningPairs(List<UUID> ids, int batchSize, LocalDateTime terminatedAt) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }
    if (batchSize <= 0) {
      throw new IllegalArgumentException("Batch size must be greater than 0");
    }
    if (terminatedAt == null) {
      throw new IllegalArgumentException("terminatedAt must not be null");
    }

    List<ConsentRequestEntity> result = new ArrayList<>();

    for (int from = 0; from < ids.size(); from += batchSize) {
      int to = Math.min(from + batchSize, ids.size());
      List<UUID> batchIds = ids.subList(from, to);

      List<ConsentRequestEntity> batchResult = entityManager.createQuery(
              """
                  select cr
                  from ConsentRequestEntity cr
                  where cr.id in :ids
                    and cr.archived = false
                    and cr.uidBurRelationUntil is null
                  """, ConsentRequestEntity.class
          )
          .setParameter("ids", batchIds)
          .getResultList();

      batchResult.forEach(entity -> entity.setUidBurRelationUntil(terminatedAt));

      result.addAll(batchResult);
    }

    return result;
  }

  /**
   * Represents a pairing of a farm business register number (BUR) and its associated owner UID.
   *
   * @param bur the farm’s business register number (Betriebs- und Unternehmensregister)
   * @param uid the unique identifier of the current farm owner
   * @CommentLastReviewed 2026-02-23
   */

  public record BurUidPair(String bur, String uid) {
  }

  //endregion
}
