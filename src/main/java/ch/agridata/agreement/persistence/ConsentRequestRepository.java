package ch.agridata.agreement.persistence;

import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.GRANTED;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

/**
 * Manages persistence of consent requests. It supports querying and updating consent-related records.
 *
 * @CommentLastReviewed 2025-10-23
 */

@ApplicationScoped
@RequiredArgsConstructor
public class ConsentRequestRepository implements PanacheRepositoryBase<ConsentRequestEntity, UUID> {
  private final EntityManager entityManager;

  public List<ConsentRequestEntity> findByDataProducerUids(List<String> dataProducerUids) {
    return find("dataProducerUid IN ?1", dataProducerUids).list();
  }

  public Optional<ConsentRequestEntity> findByIdAndDataProducerUids(UUID id, List<String> dataProducerUids) {
    return find("id = :id and dataProducerUid IN :dataProducerUids",
        Parameters.with("id", id).and("dataProducerUids", dataProducerUids))
        .firstResultOptional();
  }

  public List<ConsentRequestEntity> findByDataRequestId(UUID dataRequestId) {
    return find("dataRequest.id", dataRequestId).list();
  }

  public List<UUID> findConsentRequestIdsOfConsumerGrantedByProducerForProduct(String dataConsumerUid,
                                                                               String dataProducerUid,
                                                                               UUID dataProductId) {
    return entityManager.createQuery(
            "SELECT cr.id "
                + "FROM ConsentRequestEntity cr "
                + "JOIN cr.dataRequest dr "
                + "JOIN dr.dataProducts dp "
                + "WHERE cr.dataProducerUid = :dataProducerUid "
                + "AND cr.stateCode = 'GRANTED' "
                + "AND dr.dataConsumerUid = :dataConsumerUid "
                + "AND dp.dataProductId = :dataProductId", UUID.class)
        .setParameter("dataConsumerUid", dataConsumerUid)
        .setParameter("dataProducerUid", dataProducerUid)
        .setParameter("dataProductId", dataProductId)
        .getResultList();

  }

  public Optional<ConsentRequestEntity> findByDataRequestIdAndDataProducerUid(UUID dataRequestId, String dataProducerUid) {
    return find("dataRequest.id = :dataRequestId and dataProducerUid = :dataProducerUid",
        Parameters.with("dataRequestId", dataRequestId).and("dataProducerUid", dataProducerUid)).firstResultOptional();

  }

  public List<ConsentRequestEntity> findByDataRequestIdAndDataProducerUids(UUID dataRequestId, List<String> dataProducerUids) {
    return find("dataRequest.id = :dataRequestId and dataProducerUid IN :dataProducerUids",
        Parameters.with("dataRequestId", dataRequestId).and("dataProducerUids", dataProducerUids))
        .list();
  }

  public List<String> findGrantedConsentRequestUidsForProductOfConsumerSince(UUID productId,
                                                                             String dataConsumerUid,
                                                                             LocalDateTime since) {
    return entityManager.createQuery(
            "SELECT DISTINCT cr.dataProducerUid "
                + "FROM ConsentRequestEntity cr "
                + "JOIN cr.dataRequest dr "
                + "JOIN dr.dataProducts dp "
                + "WHERE dr.dataConsumerUid = :dataConsumerUid "
                + "AND dp.dataProductId = :productId "
                + "AND cr.stateCode = :stateCode "
                + "AND cr.lastStateChangeDate > :since", String.class)
        .setParameter("dataConsumerUid", dataConsumerUid)
        .setParameter("productId", productId)
        .setParameter("stateCode", GRANTED)
        .setParameter("since", since)
        .getResultList();
  }
}
