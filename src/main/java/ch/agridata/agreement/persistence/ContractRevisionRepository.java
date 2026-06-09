package ch.agridata.agreement.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Manages persistence operations for contract revisions. It allows querying contract revisions by their ID and data consumer UID.
 *
 * @CommentLastReviewed: 2026-03-16
 */

@ApplicationScoped
public class ContractRevisionRepository implements PanacheRepositoryBase<ContractRevisionEntity, UUID> {
  public Optional<ContractRevisionEntity> findByIdAndDataConsumerUid(UUID id, String consumerUid) {
    return find(
        "select cr "
            + "from ContractRevisionEntity cr "
            + "join fetch cr.dataRequest dr "
            + "where cr.id = :id and dr.dataConsumerUid = :consumerUid",
        Map.of("id", id, "consumerUid", consumerUid)).firstResultOptional();
  }

  public Optional<ContractRevisionEntity> findByIdAndDataRequestStateNotDraft(UUID id) {
    return find(
        "select cr "
            + "from ContractRevisionEntity cr "
            + "join fetch cr.dataRequest dr "
            + "where cr.id = :id and dr.stateCode <> :state_code",
        Map.of("id", id,
            "state_code", DataRequestEntity.DataRequestStateEnum.DRAFT
        )
    ).firstResultOptional();
  }

  public void deleteByDataRequestId(UUID dataRequestId) {
    update("archived = true where dataRequest.id = ?1", dataRequestId);
  }
}
