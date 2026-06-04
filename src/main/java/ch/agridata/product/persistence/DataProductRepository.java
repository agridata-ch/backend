package ch.agridata.product.persistence;

import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.common.dto.ResourceQueryDto;
import ch.agridata.common.dto.SupportedLanguage;
import ch.agridata.common.persistence.BaseSearchRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

/**
 * Provides persistence operations for data products.
 *
 * @CommentLastReviewed 2026-05-18
 */

@ApplicationScoped
public class DataProductRepository extends BaseSearchRepository<DataProductEntity, UUID> {

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


  private static final Map<String, Function<SupportedLanguage, String>> SORT_FIELDS = Map.of(
      "productName", lang -> jsonbField("dp.name", lang),
      "providerName", lang -> jsonbField("p.name", lang),
      "systemName", lang -> jsonbField("ds.name", lang)
  );

  public Optional<DataProductEntity> findByIdAndDataProviderUidOptional(UUID id, String dataProviderUid) {
    return find(BASE_QUERY + where(BY_ID, BY_PROVIDER_UID),
        Map.of(PARAM_ID, id, PARAM_PROVIDER_UID, dataProviderUid)).firstResultOptional();
  }

  public Optional<DataProductEntity> findActiveByIdOptional(UUID id) {
    return find(BASE_QUERY + where(BY_ID, BY_STATE), Map.of(PARAM_ID, id, PARAM_STATE, DataProductStateEnum.ACTIVE)).firstResultOptional();
  }

  public List<DataProductEntity> findAllActive() {
    return find(BASE_QUERY + where(BY_STATE), Map.of(PARAM_STATE, DataProductStateEnum.ACTIVE)).list();
  }

  public List<DataProductEntity> listActiveByProviderUid(String providerUid) {
    return find(BASE_QUERY + where(BY_PROVIDER_UID, BY_STATE) + " order by ds.code, dp.id",
        Map.of(PARAM_PROVIDER_UID, providerUid, PARAM_STATE,
            DataProductStateEnum.ACTIVE)).list();
  }

  public Optional<UUID> findDataSourceSystemIdByProductIdOptional(UUID productId) {
    return find("""
        select dp.dataSourceSystem.id
        from DataProductEntity dp
        """ + where(BY_ID, BY_STATE), Map.of(PARAM_ID, productId, PARAM_STATE, DataProductStateEnum.ACTIVE))
        .project(UUID.class)
        .firstResultOptional();
  }

  public PageResponseDto<DataProductEntity> findPaged(
      ResourceQueryDto query,
      SupportedLanguage language
  ) {
    var paged = find(BASE_QUERY + orderBy(query, language))
        .page(query.page(), query.size());

    return toPageResponse(paged, query);
  }

  public PageResponseDto<DataProductEntity> findPagedByProviderUid(
      String providerUid,
      ResourceQueryDto query,
      SupportedLanguage language
  ) {
    var paged = find(BASE_QUERY + where(BY_PROVIDER_UID) + orderBy(query, language), Map.of(PARAM_PROVIDER_UID, providerUid))
        .page(query.page(), query.size());

    return toPageResponse(paged, query);
  }

  private String orderBy(ResourceQueryDto query, SupportedLanguage lang) {

    if (query.sortParams() == null || query.sortParams().isEmpty()) {
      return "";
    }

    List<String> orderParts = query.sortParams().stream()
        .flatMap(p -> Arrays.stream(p.split(",")))
        .map(String::trim)
        .filter(s -> !s.isBlank())
        .map(sort -> {

          boolean desc = sort.startsWith("-");
          String key = desc ? sort.substring(1) : sort;

          Function<SupportedLanguage, String> resolver = SORT_FIELDS.get(key);

          if (resolver == null) {
            throw new IllegalArgumentException("Unsupported sort field: " + key);
          }

          return resolver.apply(lang) + (desc ? " desc" : " asc");
        })
        .toList();

    return " order by " + String.join(", ", orderParts) + ", dp.id";
  }

  private PageResponseDto<DataProductEntity> toPageResponse(
      PanacheQuery<DataProductEntity> paged,
      ResourceQueryDto query
  ) {
    return new PageResponseDto<>(
        paged.list(),
        paged.count(),
        paged.pageCount(),
        query.page(),
        query.size()
    );
  }

  private static String jsonbField(String column, SupportedLanguage lang) {
    String locale = switch (lang) {
      case FR -> "fr";
      case IT -> "it";
      default -> "de";
    };

    return "lower(function('jsonb_extract_path_text', %s, '%s'))"
        .formatted(column, locale);
  }

  private static String where(String... conditions) {
    return " where " + String.join(" and ", conditions);
  }
}
