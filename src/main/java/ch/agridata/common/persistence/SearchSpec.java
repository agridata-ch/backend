package ch.agridata.common.persistence;

import java.util.List;
import java.util.Map;
import lombok.Builder;

/**
 * Immutable specification of the static (non-request) parts of a generic paged search: the base
 * query/where, its parameters, the searchable and sortable fields. Combined with a
 * {@link ch.agridata.common.dto.ResourceQueryDto} (which carries the request-specific page, sort,
 * search term and language) by {@link BaseSearchRepository#findPageMultilingual}.
 *
 * <p>The compact constructor normalises optional collections to empty and defaults
 * {@code sortTieBreaker} to {@code "id"} for stable pagination.
 *
 * @param baseSelect     Optional full select clause including joins; null for a plain entity query.
 *                       When given, field paths must use its aliases.
 *                       E.g. {@code "select dp from DataProductEntity dp left join dp.dataProvider p"}.
 * @param baseWhere      Optional base WHERE clause combined with the search filter (no "where" keyword).
 *                       E.g. {@code "dp.dataProviderUid = :providerUid"}.
 * @param baseParams     Parameters for the base WHERE clause.
 *                       E.g. {@code Map.of("providerUid", "CHE-123")}.
 * @param filterFields   Fields searched individually by the search term.
 *                       E.g. {@code [SearchField.translated("dp.name"), SearchField.simple("dp.code")]}:
 *                       search term "milk" matches products whose value (in the request language) contains it.
 * @param combinedFields Groups of fields searched together by the search term.
 *                       E.g. for search term "john doe" and a group {@code [firstName, lastName]}
 *                       any permutation of "john" and "doe" across firstName and lastName will match.
 * @param sortFields     Whitelist mapping API sort keys to sortable fields; an unknown key throws.
 *                       E.g. {@code Map.of("productName", SearchField.translated("dp.name"))}:
 *                       {@code ?sortBy=-productName} sorts by the translated name, descending.
 * @param sortTieBreaker Field appended to every ORDER BY for stable pagination.
 *                       E.g. {@code "dp.id"}; defaults to {@code "id"}.
 * @CommentLastReviewed 2026-07-27
 */
@Builder
public record SearchSpec(
    String baseSelect,
    String baseWhere,
    Map<String, Object> baseParams,
    List<SearchField> filterFields,
    List<List<SearchField>> combinedFields,
    Map<String, SearchField> sortFields,
    String sortTieBreaker
) {

  public SearchSpec {
    baseParams = baseParams != null ? baseParams : Map.of();
    filterFields = filterFields != null ? filterFields : List.of();
    combinedFields = combinedFields != null ? combinedFields : List.of();
    sortFields = sortFields != null ? sortFields : Map.of();
    sortTieBreaker = sortTieBreaker != null ? sortTieBreaker : "id";
  }
}
