package ch.agridata.common.persistence;

import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.common.dto.ResourceQueryDto;
import ch.agridata.common.dto.SupportedLanguage;
import ch.agridata.common.utils.JpaUtil;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;
import java.util.Arrays;
import java.util.Collections;
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
 * @CommentLastReviewed 2026-07-27
 */
public abstract class BaseSearchRepository<T, I> implements PanacheRepositoryBase<T, I> {

  /**
   * Find a page of entities matching the given search criteria.
   *
   * @param resourceQuery  The resource query containing pagination, sorting, and search term.
   * @param baseWhere      An optional base WHERE clause to be combined with the search filter.
   * @param baseParams     Parameters for the base WHERE clause.
   * @param filterFields   Fields to be searched individually by the search term.
   * @param combinedFields Groups of fields to be searched together by the search term.
   *                       E.g  for search Term "john doe" and combinedList [firstName,lastName]
   *                       any permutation of "john" and "doe" in firstName and lastName will match.
   * @return A PageResponseDto containing the results and pagination info.
   */
  protected PageResponseDto<T> findPage(
      ResourceQueryDto resourceQuery,
      String baseWhere,
      Map<String, Object> baseParams,
      List<String> filterFields,
      List<List<String>> combinedFields
  ) {
    var searchTerm = resourceQuery.searchTerm();
    var filter = JpaUtil.createContainsWhereClause(searchTerm, filterFields, combinedFields);

    String parts = joinSqlConditions(baseWhere, filter);
    Map<String, Object> mergedParams = mergeParams(baseParams, filter.parameters());
    Sort sort = JpaUtil.parseSort(resourceQuery.sortParams());

    var panacheQuery = parts.isEmpty()
        ? findAll(sort)
        : find(parts, sort, mergedParams);

    return toPageResponse(panacheQuery, resourceQuery);
  }

  /**
   * Convenience overload when you have no base WHERE/params.
   */
  protected PageResponseDto<T> findPage(
      ResourceQueryDto resourceQuery,
      List<String> filterFields,
      List<List<String>> combinedFields
  ) {
    return findPage(resourceQuery, null, Collections.emptyMap(), filterFields, combinedFields);
  }

  /**
   * Find a page of entities matching the search criteria in {@code resourceQuery}, with support
   * for multilingual (JSON translation) fields. Translated fields are searched and sorted in the request
   * language ({@link ResourceQueryDto#language()}, defaulting to German). Sorting is restricted to the
   * {@link SearchSpec#sortFields()} whitelist; an unknown sort key results in an
   * {@link IllegalArgumentException}.
   *
   * @param resourceQuery The resource query carrying pagination, sorting, search term and language.
   * @param spec          The static query specification (base query/where, searchable/sortable fields).
   * @return A PageResponseDto containing the results and pagination info.
   */
  protected PageResponseDto<T> findPageMultilingual(ResourceQueryDto resourceQuery, SearchSpec spec) {
    var language = resourceQuery.supportedLanguage();

    var filter = JpaUtil.createContainsWhereClause(
        resourceQuery.searchTerm(),
        spec.filterFields().stream().map(field -> field.toHql(language)).toList(),
        spec.combinedFields().stream().map(group -> group.stream().map(field -> field.toHql(language)).toList()).toList()
    );

    String conditions = joinSqlConditions(spec.baseWhere(), filter);
    Map<String, Object> mergedParams = mergeParams(spec.baseParams(), filter.parameters());
    String orderBy = createOrderByClause(resourceQuery.sortParams(), spec.sortFields(), language, spec.sortTieBreaker());

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
    Map<String, Object> mergedParams = new HashMap<>();
    if (baseParams != null) {
      mergedParams.putAll(baseParams);
    }
    if (filterParams != null) {
      mergedParams.putAll(filterParams);
    }
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
      Map<String, SearchField> sortFields,
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

          SearchField field = sortFields.get(key);
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
