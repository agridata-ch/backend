package ch.agridata.agreement.persistence;

import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.GRANTED;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
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
public class ConsentRequestRepository implements PanacheRepositoryBase<ConsentRequestEntity, UUID> {
  private final EntityManager entityManager;

  public List<ConsentRequestEntity> findByDataProducerUids(List<String> dataProducerUids) {
    return find("dataProducerUid IN ?1", dataProducerUids).list();
  }

  public List<ConsentRequestEntity> findByDataProducerUidsWithDataRequest(List<String> dataProducerUids) {
    return entityManager.createQuery(
            "SELECT cr FROM ConsentRequestEntity cr "
                + "JOIN FETCH cr.dataRequest dr "
                + "WHERE cr.dataProducerUid IN :uids", ConsentRequestEntity.class
        )
        .setParameter("uids", dataProducerUids)
        .getResultList();
  }

  public Optional<ConsentRequestEntity> findByIdAndDataProducerUids(UUID id, List<String> dataProducerUids) {
    return find(
        "id = :id and dataProducerUid IN :dataProducerUids",
        Map.of(
            "id", id,
            "dataProducerUids", dataProducerUids
        )
    ).firstResultOptional();
  }

  public List<UUID> findConsentRequestIdsOfConsumerGrantedByProducerForProduct(
      String dataConsumerUid,
      String dataProducerUid,
      UUID dataProductId
  ) {
    return entityManager.createQuery(
            "SELECT cr.id "
                + "FROM ConsentRequestEntity cr "
                + "JOIN cr.dataRequest dr "
                + "JOIN dr.dataProducts dp "
                + "WHERE cr.dataProducerUid = :dataProducerUid "
                + "AND cr.stateCode = 'GRANTED' "
                + "AND dr.dataConsumerUid = :dataConsumerUid "
                + "AND dp.dataProductId = :dataProductId", UUID.class
        )
        .setParameter("dataConsumerUid", dataConsumerUid)
        .setParameter("dataProducerUid", dataProducerUid)
        .setParameter("dataProductId", dataProductId)
        .getResultList();

  }

  public Optional<ConsentRequestEntity> findByDataRequestIdAndDataProducerUid(UUID dataRequestId, String dataProducerUid) {
    return find(
        "dataRequest.id = :dataRequestId and dataProducerUid = :dataProducerUid",
        Map.of(
            "dataRequestId", dataRequestId,
            "dataProducerUid", dataProducerUid
        )
    ).firstResultOptional();

  }

  public List<ConsentRequestEntity> findByDataRequestIdAndDataProducerUids(UUID dataRequestId, List<String> dataProducerUids) {
    return find(
        "dataRequest.id = :dataRequestId and dataProducerUid IN :dataProducerUids",
        Map.of(
            "dataRequestId", dataRequestId,
            "dataProducerUids", dataProducerUids
        )
    ).list();
  }

  public List<ConsentRequestEntity> findByDataRequestIdAndDataProducerBurs(UUID dataRequestId, List<String> dataProducerBurs) {
    return find(
        "dataRequest.id = :dataRequestId and dataProducerBur IN :dataProducerBurs",
        Map.of(
            "dataRequestId", dataRequestId,
            "dataProducerBurs", dataProducerBurs
        )
    ).list();
  }

  public List<String> findGrantedConsentRequestUidsForProductOfConsumerSince(
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
                + "AND cr.lastStateChangeDate > :since", String.class
        )
        .setParameter("dataConsumerUid", dataConsumerUid)
        .setParameter("productId", productId)
        .setParameter("stateCode", GRANTED)
        .setParameter("since", since)
        .getResultList();
  }

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

  public List<TerminatedBurUid> terminateByIdsReturningPairs(List<UUID> ids, int batchSize, LocalDateTime terminatedAt) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }
    if (batchSize <= 0) {
      throw new IllegalArgumentException("Batch size must be greater than 0");
    }
    if (terminatedAt == null) {
      throw new IllegalArgumentException("terminatedAt must not be null");
    }

    final String sql = """
        update consent_request
        set uid_bur_relation_until = :terminatedAt
        where id = any(:ids)
          and archived = false
          and uid_bur_relation_until is null
        returning id, data_producer_bur, data_producer_uid
        """;

    List<TerminatedBurUid> result = new ArrayList<>();

    for (int from = 0; from < ids.size(); from += batchSize) {
      int to = Math.min(from + batchSize, ids.size());
      List<UUID> batchIds = ids.subList(from, to);

      var query = entityManager.createNativeQuery(sql)
          .setParameter("terminatedAt", terminatedAt)
          .setParameter("ids", batchIds.toArray(UUID[]::new));

      List<Object[]> rows = query.getResultList();
      for (Object row : rows) {
        Object[] r = (Object[]) row;
        result.add(new TerminatedBurUid((UUID) r[0], (String) r[1], (String) r[2]));
      }
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

  /**
   * Lightweight projection representing a consent request that was terminated.
   *
   * <p>This record is used when terminating consent requests via a database
   * operation that returns affected rows (e.g. PostgreSQL {@code RETURNING}).
   * It contains only the minimal information required for auditing and logging:
   * the consent request identifier together with the associated BUR–UID pair.
   *
   * <p>Instances of this record are typically produced by repository methods
   * performing bulk termination operations and should be treated as immutable
   * snapshots of the state at the time of termination.
   *
   * @param id  unique identifier of the consent request that was terminated
   * @param bur business unit register (BUR) of the data producer at termination time
   * @param uid UID of the data producer at termination time
   * @CommentLastReviewed 2026-03-02
   */
  public record TerminatedBurUid(UUID id, String bur, String uid) {
  }
}
