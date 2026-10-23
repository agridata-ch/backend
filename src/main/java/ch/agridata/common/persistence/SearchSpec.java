package ch.agridata.common.persistence;

import java.util.List;
import java.util.Map;
import lombok.Builder;

/**
 * Immutable specification of the static (non-request) parts of a generic paged search: the base
 * query/where, its parameters, the searchable, filterable and sortable fields. Combined with a
 * {@link ch.agridata.common.dto.ResourceQueryDto} (which carries the request-specific page, sort,
 * search term and language) by {@link BaseSearchRepository#findPage}.
 *
 * <p>The compact constructor normalises optional collections to empty and defaults
 * {@code sortTieBreaker} to {@code "id"} for stable pagination.
 *
 * @param baseSelect       Optional full select clause including joins; null for a plain entity query.
 *                         When given, field paths must use its aliases.
 *                         E.g. {@code "select dp from DataProductEntity dp left join dp.dataProvider p"}.
 * @param baseWhere        Optional base WHERE clause combined with the search filter (no "where" keyword).
 *                         E.g. {@code "dp.dataProviderUid = :providerUid"}.
 * @param baseParams       Parameters for the base WHERE clause.
 *                         E.g. {@code Map.of("providerUid", "CHE-123")}.
 * @param searchableFields Fields searched by the search term: every word of the term must be contained in at
 *                         least one of them, in any order.
 *                         E.g. {@code [SearchField.translated("dp.name"), SearchField.simple("dp.code")]}:
 *                         search term "milk p-1" matches products whose name (in the request language) or code
 *                         contains "milk" and whose name or code contains "p-1".
 * @param filterableFields Whitelist mapping API filter keys to filterable columns; an unknown key throws.
 *                         E.g. {@code Map.of("dataProviderId", FilterField.uuid("p.id"))}:
 *                         {@code ?filter=dataProviderId:<uuid>,<uuid>} keeps products of either provider.
 * @param sortableFields   Whitelist mapping API sort keys to sortable fields; an unknown key throws.
 *                         E.g. {@code Map.of("productName", SearchField.translated("dp.name"))}:
 *                         {@code ?sortBy=-productName} sorts by the translated name, descending.
 * @param sortTieBreaker   Field appended to every ORDER BY for stable pagination.
 *                         E.g. {@code "dp.id"}; defaults to {@code "id"}.
 * @CommentLastReviewed 2026-09-14
 */
@Builder
public record SearchSpec(
    String baseSelect,
    String baseWhere,
    Map<String, Object> baseParams,
    List<SearchField> searchableFields,
    Map<String, FilterField> filterableFields,
    Map<String, SearchField> sortableFields,
    String sortTieBreaker
) {

  public SearchSpec {
    baseParams = baseParams != null ? baseParams : Map.of();
    searchableFields = searchableFields != null ? searchableFields : List.of();
    filterableFields = filterableFields != null ? filterableFields : Map.of();
    sortableFields = sortableFields != null ? sortableFields : Map.of();
    sortTieBreaker = sortTieBreaker != null ? sortTieBreaker : "id";
  }
}
