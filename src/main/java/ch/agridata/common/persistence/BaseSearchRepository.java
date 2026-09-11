package ch.agridata.common.persistence;

import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.common.dto.ResourceQueryDto;
import ch.agridata.common.dto.SupportedLanguage;
import ch.agridata.common.exceptions.SearchSpecificationException;
import ch.agridata.common.utils.JpaUtil;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;

/**
 * Generic base repository with reusable paged "search + filter + sort" queries.
 *
 * @CommentLastReviewed 2026-09-14
 */
public abstract class BaseSearchRepository<T, I> implements PanacheRepositoryBase<T, I> {

  private static final String FILTER_PARAM_PREFIX = "columnFilter";

  /**
   * Find a page of entities matching the search criteria in {@code resourceQuery}, with support
   * for multilingual (JSON translation) fields. Translated fields are searched and sorted in the request
   * language ({@link ResourceQueryDto#language()}, defaulting to German). Sorting is restricted to the
   * {@link SearchSpec#sortableFields()} whitelist and column filtering to the
   * {@link SearchSpec#filterableFields()} whitelist; an unknown key results in an
   * {@link IllegalArgumentException}.
   *
   * @param resourceQuery The resource query carrying pagination, sorting, search term, column filters and language.
   * @param spec          The static query specification (base query/where, searchable/sortable fields).
   * @return A PageResponseDto containing the results and pagination info.
   */
  protected PageResponseDto<T> findPage(ResourceQueryDto resourceQuery, SearchSpec spec) {
    var language = resourceQuery.supportedLanguage();

    var searchClause = JpaUtil.createContainsWhereClause(
        resourceQuery.searchTerm(),
        spec.searchableFields().stream().map(field -> field.toHql(language)).toList(),
        spec.combinedFields().stream().map(group -> group.stream().map(field -> field.toHql(language)).toList()).toList()
    );

    var columnFilterClause = createColumnFilterWhereClause(resourceQuery.columnFilters(), spec.filterableFields());

    String conditions = joinSqlConditions(spec.baseWhere(), searchClause.clause(), columnFilterClause.clause());
    Map<String, Object> mergedParams = mergeParams(List.of(spec.baseParams(), searchClause.parameters(), columnFilterClause.parameters()));
    String orderBy = createOrderByClause(resourceQuery.sortParams(), spec.sortableFields(), language, spec.sortTieBreaker());

    String query = spec.baseSelect() != null
        ? spec.baseSelect() + " where " + conditions + orderBy
        : conditions + orderBy;

    return toPageResponse(find(query, mergedParams), resourceQuery);
  }

  /**
   * Build the column filter clause: values of the same column are combined with OR, filters on
   * different columns with AND. Filtering on a column outside {@link SearchSpec#filterableFields()}
   * or with a value that does not fit the column type results in an {@link IllegalArgumentException}.
   */
  private static JpaUtil.WhereClause createColumnFilterWhereClause(List<String> filters, Map<String, FilterField> filterableFields) {
    if (filters == null || filters.isEmpty()) {
      return new JpaUtil.WhereClause("", Map.of());
    }

    var valuesByColumn = parseFilters(filters);

    List<String> clauses = new ArrayList<>();
    Map<String, Object> parameters = new HashMap<>();
    var index = 0;

    for (var entry : valuesByColumn.entrySet()) {
      FilterField field = filterableFields.get(entry.getKey());

      if (field == null) {
        throw new IllegalArgumentException("Unsupported filter field: " + entry.getKey());
      }

      String paramName = FILTER_PARAM_PREFIX + index++;

      parameters.put(paramName, entry.getValue().stream().map(field::parse).toList());
      clauses.add(field.path() + " in :" + paramName);
    }

    return new JpaUtil.WhereClause(String.join(" and ", clauses), parameters);
  }

  /**
   * Parse the raw {@code <column>:<value>[,<value>]} filters, merging repetitions of the same column
   * and dropping duplicate values, which would only repeat the same value in the generated IN list.
   */
  private static Map<String, Set<String>> parseFilters(List<String> filters) {
    Map<String, Set<String>> valuesByColumn = new LinkedHashMap<>();

    for (String filter : filters) {
      int separator = filter == null ? -1 : filter.indexOf(':');
      if (separator < 1) {
        throw new IllegalArgumentException("Invalid filter, expected '<column>:<value>[,<value>]'");
      }

      var values = Arrays.stream(filter.substring(separator + 1).split(","))
          .map(String::trim)
          .filter(StringUtils::isNotEmpty)
          .toList();
      if (values.isEmpty()) {
        throw new IllegalArgumentException("Filter without value");
      }

      valuesByColumn.computeIfAbsent(filter.substring(0, separator).trim(), column -> new LinkedHashSet<>()).addAll(values);
    }

    return valuesByColumn;
  }

  private static @NonNull String joinSqlConditions(String... conditions) {
    return Stream.of(conditions)
        .filter(StringUtils::isNotEmpty)
        .map(s -> "(" + s + ")")
        .collect(Collectors.joining(" and "));
  }

  /**
   * Merge the base, search and column filter parameters. A name used by more than one of them would
   * silently bind the wrong value, so a collision fails fast with a {@link SearchSpecificationException}:
   * base parameters must not use the {@code paramfull}/{@code param<n>} names of the search clause nor
   * the {@code columnFilter<n>} names of the column filter clause. A collision can only be caused by a
   * faulty {@link SearchSpec}, never by the request.
   */
  private static @NonNull Map<String, Object> mergeParams(List<Map<String, Object>> paramMaps) {
    // No null check required as all maps are guaranteed non-Null
    Map<String, Object> mergedParams = new HashMap<>();
    for (var paramMap : paramMaps) {
      paramMap.forEach((name, value) -> {
        if (mergedParams.containsKey(name)) {
          throw new SearchSpecificationException("Duplicate query parameter name: " + name);
        }
        mergedParams.put(name, value);
      });
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
