package ch.agridata.agreement.persistence;

import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.GRANTED;

import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.common.dto.ResourceQueryDto;
import ch.agridata.common.persistence.BaseSearchRepository;
import ch.agridata.common.persistence.SearchField;
import ch.agridata.common.persistence.SearchSpec;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

/**
 * Read-only queries over {@link ConsentRequestFundamentalViewEntity} — the performant path for loading consents in the
 * data-transfer flow, avoiding the eager data request fetch of {@link ConsentRequestEntity}.
 *
 * @CommentLastReviewed 2026-08-14
 */

@ApplicationScoped
@RequiredArgsConstructor
public class ConsentRequestFundamentalViewRepository extends BaseSearchRepository<ConsentRequestFundamentalViewEntity, UUID> {

  public List<ConsentRequestFundamentalViewEntity> findGrantedByDataRequestIdsAndDataProducerBurs(
      List<UUID> dataRequestIds, List<String> dataProducerBurs) {
    return find(
        "dataRequestId IN :dataRequestIds and dataProducerBur IN :dataProducerBurs and stateCode = :stateCode",
        Map.of(
            "dataRequestIds", dataRequestIds,
            "dataProducerBurs", dataProducerBurs,
            "stateCode", GRANTED
        )
    ).list();
  }

  public List<ConsentRequestFundamentalViewEntity> findGrantedByDataRequestIdsAndDataProducerUids(
      List<UUID> dataRequestIds, List<String> dataProducerUids) {
    return find(
        "dataRequestId IN :dataRequestIds and dataProducerUid IN :dataProducerUids and stateCode = :stateCode",
        Map.of(
            "dataRequestIds", dataRequestIds,
            "dataProducerUids", dataProducerUids,
            "stateCode", GRANTED
        )
    ).list();
  }

  public PageResponseDto<ConsentRequestFundamentalViewEntity> findByDataRequestIdAndLastModifiedFrom(
      ResourceQueryDto resourceQueryDto,
      UUID dataRequestId,
      LocalDateTime lastModifiedFrom
  ) {
    return findPage(
        resourceQueryDto,
        SearchSpec.builder()
            .baseWhere("dataRequestId = :dataRequestId AND modifiedAt >= :lastModifiedFrom")
            .baseParams(Map.of("dataRequestId", dataRequestId, "lastModifiedFrom", lastModifiedFrom))
            .sortableFields(Map.of("modifiedAt", SearchField.simple("modifiedAt")))
            .build()
    );
  }

}
