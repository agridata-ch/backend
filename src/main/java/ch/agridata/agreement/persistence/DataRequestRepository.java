package ch.agridata.agreement.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Provides access to data request entities in the database. It abstracts storage interactions behind a clean interface.
 *
 * @CommentLastReviewed 2025-08-25
 */

@ApplicationScoped
public class DataRequestRepository implements PanacheRepositoryBase<DataRequestEntity, UUID> {
  public Optional<DataRequestEntity> findByIdAndDataConsumerUid(UUID id, String dataConsumerUid) {
    return find("id = :id and dataConsumerUid = :dataConsumerUid",
        Parameters.with("id", id).and("dataConsumerUid", dataConsumerUid))
        .firstResultOptional();
  }

  public List<DataRequestEntity> findByDataConsumerUid(String dataConsumerUid) {
    return find("dataConsumerUid = :dataConsumerUid",
        Parameters.with("dataConsumerUid", dataConsumerUid)).list();
  }

  public List<DataRequestEntity> findAllNotDraft() {
    return find("stateCode <> :state_code",
        Parameters.with("state_code", DataRequestEntity.DataRequestStateEnum.DRAFT)).list();
  }

  public Optional<DataRequestEntity> findByIdAndStateCodeNotDraft(UUID id) {
    return find("id = :id and stateCode <> :state_code",
        Parameters.with("id", id).and("state_code", DataRequestEntity.DataRequestStateEnum.DRAFT))
        .firstResultOptional();
  }

  public boolean existsByHumanFriendlyId(String humanFriendlyId) {
    return count("humanFriendlyId", humanFriendlyId) > 0;
  }
}
