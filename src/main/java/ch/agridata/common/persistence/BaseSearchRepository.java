package ch.agridata.common.persistence;

import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.common.dto.ResourceQueryDto;
import ch.agridata.common.utils.JpaUtil;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;

/**
 * Generic base repository with reusable paged "search + sort" queries.
 *
 * @CommentLastReviewed 2025-09-11
 */
public abstract class BaseSearchRepository<T, IdT> implements PanacheRepositoryBase<T, IdT> {

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

    String parts = Stream.of(baseWhere, filter.clause())
        .filter(StringUtils::isNotEmpty)
        .map(s -> "(" + s + ")")
        .collect(Collectors.joining(" and "));

    Map<String, Object> mergedParams = new HashMap<>();
    if (baseParams != null) {
      mergedParams.putAll(baseParams);
    }
    if (filter.parameters() != null) {
      mergedParams.putAll(filter.parameters());
    }

    Sort sort = JpaUtil.parseSort(resourceQuery.sortParams());

    var panacheQuery = parts.isEmpty()
        ? findAll(sort)
        : find(parts, sort, mergedParams);

    var paged = panacheQuery.page(resourceQuery.page(), resourceQuery.size());

    return new PageResponseDto<>(
        paged.list(),
        paged.count(),
        paged.pageCount(),
        resourceQuery.page(),
        resourceQuery.size()
    );
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
}
