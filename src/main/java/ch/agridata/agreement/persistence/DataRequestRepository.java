package ch.agridata.agreement.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Provides access to data request entities in the database. It abstracts storage interactions behind a clean interface.
 *
 * @CommentLastReviewed 2025-08-25
 */

@ApplicationScoped
public class DataRequestRepository implements PanacheRepositoryBase<DataRequestEntity, UUID> {
  private static final String PARAM_STATE_CODE = "state_code";

  public Optional<DataRequestEntity> findByIdAndDataConsumerUid(UUID id, String dataConsumerUid) {
    return find(
        "id = :id and dataConsumerUid = :dataConsumerUid",
        Map.of(
            "id", id,
            "dataConsumerUid", dataConsumerUid
        )
    )
        .firstResultOptional();
  }

  public Optional<DataRequestEntity> findByIdAndInProviderWorkflowStates(UUID id) {
    return find(
        """
            id = :id and stateCode in (:state_codes)
            """,
        Map.of(
            "id", id,
            "state_codes", List.of(
                DataRequestEntity.DataRequestStateEnum.ACTIVE,
                DataRequestEntity.DataRequestStateEnum.TO_BE_SIGNED_BY_PROVIDER,
                DataRequestEntity.DataRequestStateEnum.TO_BE_RELEASED_BY_PROVIDER
            )
        )
    )
        .firstResultOptional();
  }

  public List<DataRequestEntity> findByDataConsumerUid(String dataConsumerUid) {
    return find(
        "dataConsumerUid = :dataConsumerUid",
        Map.of(
            "dataConsumerUid", dataConsumerUid
        )
    ).list();
  }

  public List<DataRequestEntity> findByInProviderWorkflowStates() {
    return find(
        "stateCode in (:state_codes)",
        Map.of(
            "state_codes", List.of(
                DataRequestEntity.DataRequestStateEnum.ACTIVE,
                DataRequestEntity.DataRequestStateEnum.TO_BE_ACTIVATED,
                DataRequestEntity.DataRequestStateEnum.TO_BE_SIGNED_BY_PROVIDER,
                DataRequestEntity.DataRequestStateEnum.TO_BE_RELEASED_BY_PROVIDER
            )
        )
    ).list();
  }

  public List<DataRequestEntity> findAllNotDraft() {
    return find(
        "stateCode <> :state_code",
        Map.of(
            PARAM_STATE_CODE, DataRequestEntity.DataRequestStateEnum.DRAFT
        )
    ).list();
  }

  public Optional<DataRequestEntity> findByIdAndStateCodeNotDraft(UUID id) {
    return find(
        "id = :id and stateCode <> :state_code",
        Map.of(
            "id", id,
            PARAM_STATE_CODE, DataRequestEntity.DataRequestStateEnum.DRAFT
        )
    )
        .firstResultOptional();
  }

  public boolean existsByHumanFriendlyId(String humanFriendlyId) {
    return count("humanFriendlyId", humanFriendlyId) > 0;
  }

  public long countByDataConsumerUidAndState(String dataConsumerUid, DataRequestEntity.DataRequestStateEnum state) {
    return count(
        "dataConsumerUid = :dataConsumerUid and stateCode = :state_code",
        Map.of(
            "dataConsumerUid", dataConsumerUid,
            PARAM_STATE_CODE, state
        )
    );
  }

  public long archiveDraftByIdAndConsumerUid(UUID id, String consumerUid) {
    return update("archived = true where id = ?1 and dataConsumerUid = ?2 and stateCode = ?3 and archived = false",
        id, consumerUid, DataRequestEntity.DataRequestStateEnum.DRAFT);
  }

  public boolean existsByIdAndConsumerUid(UUID id, String consumerUid) {
    return count("id = ?1 and dataConsumerUid = ?2", id, consumerUid) > 0;
  }
}
