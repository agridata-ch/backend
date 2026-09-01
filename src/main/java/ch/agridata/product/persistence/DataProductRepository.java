package ch.agridata.product.persistence;

import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.common.dto.ResourceQueryDto;
import ch.agridata.common.persistence.BaseSearchRepository;
import ch.agridata.common.persistence.SearchField;
import ch.agridata.common.persistence.SearchSpec;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Provides persistence operations for data products.
 *
 * @CommentLastReviewed 2026-05-18
 */

@ApplicationScoped
public class DataProductRepository extends BaseSearchRepository<DataProductEntity, UUID> {
  private static final String DP_ID = "dp.id";

  private static final String BASE_QUERY = """
      select dp
      from DataProductEntity dp
      left join dp.dataSourceSystem ds
      left join ds.dataProvider p
      """;

  // Parameter name constants
  private static final String PARAM_ID = "id";
  private static final String PARAM_PROVIDER_UID = "providerUid";
  private static final String PARAM_STATE = "state";

  // Condition fragments for where() helper function
  private static final String BY_ID = "dp.id = :" + PARAM_ID;
  private static final String BY_PROVIDER_UID = "dp.dataProviderUid = :" + PARAM_PROVIDER_UID;
  private static final String BY_STATE = "dp.stateCode = :" + PARAM_STATE;

  private static final SearchField FIELD_PRODUCT_NAME = SearchField.translated("dp.name");
  private static final SearchField FIELD_PROVIDER_NAME = SearchField.translated("p.name");
  private static final SearchField FIELD_SYSTEM_NAME = SearchField.translated("ds.name");

  private static final Map<String, SearchField> SORTABLE_FIELDS = Map.of(
      "productName", FIELD_PRODUCT_NAME,
      "dataProviderName", FIELD_PROVIDER_NAME,
      "systemName", FIELD_SYSTEM_NAME
  );

  private static final List<SearchField> SEARCHABLE_FIELDS = List.of(FIELD_PRODUCT_NAME, FIELD_PROVIDER_NAME, FIELD_SYSTEM_NAME);

  public Optional<DataProductEntity> findByIdAndDataProviderUidOptional(UUID id, String dataProviderUid) {
    return find(
        BASE_QUERY + where(BY_ID, BY_PROVIDER_UID),
        Map.of(PARAM_ID, id, PARAM_PROVIDER_UID, dataProviderUid)
    ).firstResultOptional();
  }

  public Optional<DataProductEntity> findActiveByIdOptional(UUID id) {
    return find(BASE_QUERY + where(BY_ID, BY_STATE), Map.of(PARAM_ID, id, PARAM_STATE, DataProductStateEnum.ACTIVE)).firstResultOptional();
  }

  public List<DataProductEntity> findAllActive() {
    return find(BASE_QUERY + where(BY_STATE), Map.of(PARAM_STATE, DataProductStateEnum.ACTIVE)).list();
  }

  public List<DataProductEntity> listActiveByProviderUid(String providerUid) {
    return find(
        BASE_QUERY + where(BY_PROVIDER_UID, BY_STATE) + " order by ds.code, dp.id",
        Map.of(
            PARAM_PROVIDER_UID, providerUid, PARAM_STATE,
            DataProductStateEnum.ACTIVE
        )
    ).list();
  }

  public Optional<UUID> findDataSourceSystemIdByProductIdOptional(UUID productId) {
    return find(
        """
            select dp.dataSourceSystem.id
            from DataProductEntity dp
            """ + where(BY_ID, BY_STATE), Map.of(PARAM_ID, productId, PARAM_STATE, DataProductStateEnum.ACTIVE)
    )
        .project(UUID.class)
        .firstResultOptional();
  }

  public PageResponseDto<DataProductEntity> findPaged(ResourceQueryDto query) {
    return findPage(
        query, SearchSpec.builder()
            .baseSelect(BASE_QUERY)
            .sortableFields(SORTABLE_FIELDS)
            .sortTieBreaker(DP_ID)
            .build()
    );
  }

  public PageResponseDto<DataProductEntity> findActivePaged(ResourceQueryDto query) {
    return findPage(
        query, SearchSpec.builder()
            .baseSelect(BASE_QUERY)
            .baseWhere(BY_STATE)
            .baseParams(Map.of(PARAM_STATE, DataProductStateEnum.ACTIVE))
            .sortableFields(SORTABLE_FIELDS)
            .searchableFields(SEARCHABLE_FIELDS)
            .sortTieBreaker(DP_ID)
            .build()
    );
  }

  public PageResponseDto<DataProductEntity> findPagedByProviderUid(String providerUid, ResourceQueryDto query) {
    return findPage(
        query, SearchSpec.builder()
            .baseSelect(BASE_QUERY)
            .baseWhere(BY_PROVIDER_UID)
            .baseParams(Map.of(PARAM_PROVIDER_UID, providerUid))
            .sortableFields(SORTABLE_FIELDS)
            .sortTieBreaker(DP_ID)
            .build()
    );
  }

  private static String where(String... conditions) {
    return " where " + String.join(" and ", conditions);
  }
}
