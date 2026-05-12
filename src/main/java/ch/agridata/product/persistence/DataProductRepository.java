package ch.agridata.product.persistence;

import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.common.dto.ResourceQueryDto;
import ch.agridata.common.dto.SupportedLanguage;
import ch.agridata.common.persistence.BaseSearchRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
      join dp.dataSourceSystem ds
      join ds.dataProvider p
      """;

  private static final String PROVIDER_FILTER = "where p.id = ?1";

  private static final Map<String, Function<SupportedLanguage, String>> SORT_FIELDS = Map.of(
      "productName", lang -> jsonbField("dp.name", lang),
      "providerName", lang -> jsonbField("p.name", lang),
      "systemName", lang -> jsonbField("ds.name", lang)
  );

  public List<DataProductEntity> listByProviderId(UUID providerId) {
    return find(BASE_QUERY + PROVIDER_FILTER + " order by ds.code, dp.id", providerId).list();
  }

  public UUID findDataSourceSystemIdByProductId(UUID productId) {
    return find("""
        select dp.dataSourceSystem.id
        from DataProductEntity dp
        where dp.id = ?1
        """, productId)
        .project(UUID.class)
        .firstResultOptional()
        .orElseThrow(() -> new NotFoundException(productId.toString()));
  }

  public PageResponseDto<DataProductEntity> findPaged(
      ResourceQueryDto query,
      SupportedLanguage language
  ) {
    var paged = find(BASE_QUERY + orderBy(query, language))
        .page(query.page(), query.size());

    return toPageResponse(paged, query);
  }

  public PageResponseDto<DataProductEntity> findPagedByProviderId(
      UUID providerId,
      ResourceQueryDto query,
      SupportedLanguage language
  ) {
    var paged = find(BASE_QUERY + PROVIDER_FILTER + orderBy(query, language), providerId)
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
}
