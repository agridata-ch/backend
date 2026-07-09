package ch.agridata.common.persistence;

import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.common.dto.ResourceQueryDto;
import ch.agridata.common.dto.SupportedLanguage;
import ch.agridata.common.utils.JpaUtil;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;

/**
 * Generic base repository with reusable paged "search + sort" queries.
 *
 * @CommentLastReviewed 2026-07-28
 */
public abstract class BaseSearchRepository<T, I> implements PanacheRepositoryBase<T, I> {

  /**
   * Find a page of entities matching the search criteria in {@code resourceQuery}, with support
   * for multilingual (JSON translation) fields. Translated fields are searched and sorted in the request
   * language ({@link ResourceQueryDto#language()}, defaulting to German). Sorting is restricted to the
   * {@link SearchSpec#sortableFields()} whitelist; an unknown sort key results in an
   * {@link IllegalArgumentException}.
   *
   * @param resourceQuery The resource query carrying pagination, sorting, search term and language.
   * @param spec          The static query specification (base query/where, searchable/sortable fields).
   * @return A PageResponseDto containing the results and pagination info.
   */
  protected PageResponseDto<T> findPage(ResourceQueryDto resourceQuery, SearchSpec spec) {
    var language = resourceQuery.supportedLanguage();

    var filter = JpaUtil.createContainsWhereClause(
        resourceQuery.searchTerm(),
        spec.searchableFields().stream().map(field -> field.toHql(language)).toList(),
        spec.combinedFields().stream().map(group -> group.stream().map(field -> field.toHql(language)).toList()).toList()
    );

    String conditions = joinSqlConditions(spec.baseWhere(), filter);
    Map<String, Object> mergedParams = mergeParams(spec.baseParams(), filter.parameters());
    String orderBy = createOrderByClause(resourceQuery.sortParams(), spec.sortableFields(), language, spec.sortTieBreaker());

    String query = spec.baseSelect() != null
        ? spec.baseSelect() + " where " + conditions + orderBy
        : conditions + orderBy;

    return toPageResponse(find(query, mergedParams), resourceQuery);
  }

  private static @NonNull String joinSqlConditions(String baseWhere, JpaUtil.WhereClause filter) {
    return Stream.of(baseWhere, filter.clause())
        .filter(StringUtils::isNotEmpty)
        .map(s -> "(" + s + ")")
        .collect(Collectors.joining(" and "));
  }

  private static @NonNull Map<String, Object> mergeParams(Map<String, Object> baseParams, Map<String, Object> filterParams) {
    // No null check required as both are guaranteed non-Null Maps
    Map<String, Object> mergedParams = new HashMap<>(baseParams);
    mergedParams.putAll(filterParams);
    return mergedParams;
  }

  private PageResponseDto<T> toPageResponse(PanacheQuery<T> panacheQuery, ResourceQueryDto resourceQuery) {
    var paged = panacheQuery.page(resourceQuery.page(), resourceQuery.size());

    return new PageResponseDto<>(
        paged.list(),
        paged.count(),
        paged.pageCount(),
        resourceQuery.page(),
        resourceQuery.size()
    );
  }

  private static String createOrderByClause(
      List<String> sortParams,
      Map<String, SearchField> sortableFields,
      SupportedLanguage language,
      String sortTieBreaker
  ) {
    if (sortParams == null || sortParams.isEmpty()) {
      return "";
    }

    List<String> orderParts = sortParams.stream()
        .flatMap(param -> Arrays.stream(param.split(",")))
        .map(String::trim)
        .filter(s -> !s.isBlank())
        .map(sort -> {
          boolean desc = sort.startsWith("-");
          String key = desc ? sort.substring(1).trim() : sort;

          SearchField field = sortableFields.get(key);
          if (field == null) {
            throw new IllegalArgumentException("Unsupported sort field: " + key);
          }

          String expression = field.translated() ? "lower(" + field.toHql(language) + ")" : field.toHql(language);
          return expression + (desc ? " desc" : " asc");
        })
        .toList();

    if (orderParts.isEmpty()) {
      return "";
    }

    String tieBreaker = StringUtils.isNotEmpty(sortTieBreaker) ? ", " + sortTieBreaker : "";
    return " order by " + String.join(", ", orderParts) + tieBreaker;
  }
}
