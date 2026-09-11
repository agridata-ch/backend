package ch.agridata.common.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.common.dto.ResourceQueryDto;
import ch.agridata.common.dto.SupportedLanguage;
import ch.agridata.common.exceptions.SearchSpecificationException;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pure unit tests for the multilingual query-building logic of {@link BaseSearchRepository}.
 * A {@link TestableRepository} test double captures the HQL query and parameters that would be
 * handed to Panache, so the assembled WHERE / ORDER BY clauses and the sort whitelist can be
 * verified without a database.
 */
class BaseSearchRepositoryUnitTest {

  private static final Map<String, SearchField> SORTABLE_FIELDS = Map.of(
      "name", SearchField.translated("dp.name"),
      "code", SearchField.simple("code")
  );

  private static final Map<String, FilterField> FILTERABLE_FIELDS = Map.of(
      "providerId", FilterField.uuid("p.id"),
      "code", FilterField.text("dp.code")
  );

  private static final List<SearchField> SEARCHABLE_FIELDS = List.of(
      SearchField.translated("dp.name"),
      SearchField.simple("code")
  );

  private static final String PROVIDER_ID = "61404b83-078e-4b4f-a6d6-2aa3990f429c";

  private TestableRepository repository;

  @BeforeEach
  void setUp() {
    repository = new TestableRepository();
  }

  private static ResourceQueryDto query(List<String> sortParams, String searchTerm, SupportedLanguage language) {
    return ResourceQueryDto.builder()
        .page(0)
        .size(20)
        .sortParams(sortParams)
        .searchTerm(searchTerm)
        .language(language != null ? language.code() : null)
        .build();
  }

  private static ResourceQueryDto queryWithFilters(List<String> filters) {
    return ResourceQueryDto.builder()
        .page(0)
        .size(20)
        .columnFilters(filters)
        .build();
  }

  // --- ORDER BY building ------------------------------------------------------------------------

  @Test
  @DisplayName("Translated sort field is lowered and resolved to the request language")
  void translatedSortFieldResolvesLanguage() {
    repository.searchWithSelect(query(List.of("name"), null, SupportedLanguage.FR), SORTABLE_FIELDS);

    assertThat(repository.capturedQuery)
        .contains("order by lower(function('jsonb_extract_path_text', dp.name, 'fr')) asc, dp.id");
  }

  @Test
  @DisplayName("Simple sort field is not wrapped in lower() and ignores the language")
  void simpleSortFieldIsNotLowered() {
    repository.searchWithSelect(query(List.of("code"), null, SupportedLanguage.DE), SORTABLE_FIELDS);

    assertThat(repository.capturedQuery).contains("order by code asc, dp.id");
  }

  @Test
  @DisplayName("Leading '-' produces a descending sort")
  void descendingSortIsRendered() {
    repository.searchWithSelect(query(List.of("-name"), null, SupportedLanguage.DE), SORTABLE_FIELDS);

    assertThat(repository.capturedQuery)
        .contains("order by lower(function('jsonb_extract_path_text', dp.name, 'de')) desc, dp.id");
  }

  @Test
  @DisplayName("Comma-separated sort params are split into multiple ORDER BY parts")
  void commaSeparatedSortParamsAreSplit() {
    repository.searchWithSelect(query(List.of("code,-name"), null, SupportedLanguage.DE), SORTABLE_FIELDS);

    assertThat(repository.capturedQuery)
        .contains("order by code asc, lower(function('jsonb_extract_path_text', dp.name, 'de')) desc, dp.id");
  }

  @Test
  @DisplayName("No sort params produces no ORDER BY clause")
  void noSortParamsProducesNoOrderBy() {
    repository.searchWithSelect(query(null, null, SupportedLanguage.DE), SORTABLE_FIELDS);

    assertThat(repository.capturedQuery).doesNotContain("order by");
  }

  @Test
  @DisplayName("Sort params consisting only of blank tokens produce no ORDER BY clause")
  void allBlankSortParamsProduceNoOrderBy() {
    // Non-empty list (passes the null/empty guard) whose tokens are all blank and get filtered out,
    // leaving no order parts -> the empty-orderParts branch.
    repository.searchWithSelect(query(List.of(" ", "", ","), null, SupportedLanguage.DE), SORTABLE_FIELDS);

    assertThat(repository.capturedQuery).doesNotContain("order by");
  }

  @Test
  @DisplayName("Blank tokens mixed with valid sort params are skipped")
  void blankTokensAmongSortParamsAreSkipped() {
    repository.searchWithSelect(query(List.of("code, ,-name"), null, SupportedLanguage.DE), SORTABLE_FIELDS);

    assertThat(repository.capturedQuery)
        .contains("order by code asc, lower(function('jsonb_extract_path_text', dp.name, 'de')) desc, dp.id");
  }

  @Test
  @DisplayName("Empty sort tie-breaker yields an ORDER BY without a trailing tie-breaker")
  void emptyTieBreakerOmitsTieBreaker() {
    var spec = SearchSpec.builder()
        .sortableFields(SORTABLE_FIELDS)
        .sortTieBreaker("")
        .build();

    repository.search(query(List.of("code"), null, SupportedLanguage.DE), spec);

    assertThat(repository.capturedQuery)
        .contains("order by code asc")
        .doesNotContain("asc,");
  }

  @Test
  @DisplayName("Unknown sort key is rejected and no query is executed")
  void unknownSortKeyIsRejected() {
    var q = query(List.of("createdBy; drop table users"), null, SupportedLanguage.DE);

    assertThatIllegalArgumentException()
        .isThrownBy(() -> repository.searchWithSelect(q, SORTABLE_FIELDS))
        .withMessageContaining("Unsupported sort field");

    assertThat(repository.capturedQuery).isNull();
  }

  @Test
  @DisplayName("Null language falls back to German")
  void nullLanguageFallsBackToGerman() {
    repository.searchWithSelect(query(List.of("name"), null, null), SORTABLE_FIELDS);

    assertThat(repository.capturedQuery).contains("'de'");
  }

  // --- WHERE / search building ------------------------------------------------------------------

  @Test
  @DisplayName("Base select, base where and search filter are combined into one query")
  void baseSelectWhereAndSearchAreCombined() {
    repository.searchWithSelect(query(null, "apfel", SupportedLanguage.DE), SORTABLE_FIELDS);

    assertThat(repository.capturedQuery)
        .startsWith("select dp from DataProductEntity dp")
        .contains("where (dp.dataProviderUid = :providerUid)")
        .contains("LOWER(function('jsonb_extract_path_text', dp.name, 'de')) LIKE :paramfull")
        .contains("LOWER(code) LIKE :paramfull");
    assertThat(repository.capturedParams)
        .containsKey("providerUid")
        .containsKey("paramfull");
  }

  @Test
  @DisplayName("Search term is applied even without a base where clause")
  void searchAppliesWithoutBaseWhere() {
    repository.searchWithoutSelect(query(null, "apfel", SupportedLanguage.IT), SORTABLE_FIELDS);

    assertThat(repository.capturedQuery)
        .contains("LOWER(function('jsonb_extract_path_text', dp.name, 'it')) LIKE :paramfull");
  }

  @Test
  @DisplayName("Convenience overload without base select appends the default 'id' tie breaker")
  void convenienceOverloadUsesIdTieBreaker() {
    repository.searchWithoutSelect(query(List.of("name"), null, SupportedLanguage.DE), SORTABLE_FIELDS);

    assertThat(repository.capturedQuery)
        .doesNotStartWith("select")
        .endsWith(", id");
  }

  // --- Column filter building -------------------------------------------------------------------

  @Test
  @DisplayName("Values of one column are combined with OR, filters on different columns with AND")
  void filtersAreCombinedWithOrWithinAndAndAcrossColumns() {
    repository.searchWithFilters(queryWithFilters(List.of("code:a,b", "providerId:" + PROVIDER_ID)));

    assertThat(repository.capturedQuery)
        .contains("(dp.code in :columnFilter0 and p.id in :columnFilter1)");
    assertThat(repository.capturedParams)
        .containsEntry("columnFilter0", List.of("a", "b"))
        .containsEntry("columnFilter1", List.of(UUID.fromString(PROVIDER_ID)));
  }

  @Test
  @DisplayName("Repeating the same column merges its values into one OR group")
  void repeatedColumnIsMergedIntoOneGroup() {
    repository.searchWithFilters(queryWithFilters(List.of("code:a", "code:b")));

    assertThat(repository.capturedQuery).contains("dp.code in :columnFilter0");
    assertThat(repository.capturedParams).containsEntry("columnFilter0", List.of("a", "b"));
  }

  @Test
  @DisplayName("No filters leave the query without a filter clause")
  void noFiltersProduceNoFilterClause() {
    repository.searchWithFilters(queryWithFilters(null));

    assertThat(repository.capturedQuery).doesNotContain(":columnFilter0");
  }

  @Test
  @DisplayName("Unknown filter field is rejected and no query is executed")
  void unknownFilterFieldIsRejected() {
    var q = queryWithFilters(List.of("createdBy:x"));

    assertThatIllegalArgumentException()
        .isThrownBy(() -> repository.searchWithFilters(q))
        .withMessageContaining("Unsupported filter field: createdBy");

    assertThat(repository.capturedQuery).isNull();
  }

  @Test
  @DisplayName("A value that does not fit the column type is rejected")
  void invalidFilterValueIsRejected() {
    var q = queryWithFilters(List.of("providerId:not-a-uuid"));

    assertThatIllegalArgumentException()
        .isThrownBy(() -> repository.searchWithFilters(q))
        .withMessageContaining("Invalid filter value");
  }

  @Test
  @DisplayName("A filter without a column separator or without values is rejected")
  void malformedFilterIsRejected() {
    var withoutSeparator = queryWithFilters(List.of("code"));
    var withoutValue = queryWithFilters(List.of("code: , "));

    assertThatIllegalArgumentException()
        .isThrownBy(() -> repository.searchWithFilters(withoutSeparator))
        .withMessageContaining("Invalid filter");
    assertThatIllegalArgumentException()
        .isThrownBy(() -> repository.searchWithFilters(withoutValue))
        .withMessageContaining("Filter without value");
  }

  @Test
  @DisplayName("Duplicate values of a column are bound only once")
  void duplicateFilterValuesAreIgnored() {
    repository.searchWithFilters(queryWithFilters(List.of("code:a,b,a", "code:b")));

    assertThat(repository.capturedParams).containsEntry("columnFilter0", List.of("a", "b"));
  }

  @Test
  @DisplayName("A base parameter colliding with a generated filter parameter fails fast")
  void collidingParameterNameIsRejected() {
    var spec = SearchSpec.builder()
        .baseSelect("select dp from DataProductEntity dp")
        .baseWhere("dp.code = :columnFilter0")
        .baseParams(Map.of("columnFilter0", "X"))
        .filterableFields(FILTERABLE_FIELDS)
        .sortableFields(SORTABLE_FIELDS)
        .build();
    var q = queryWithFilters(List.of("code:a"));

    assertThatExceptionOfType(SearchSpecificationException.class)
        .isThrownBy(() -> repository.search(q, spec))
        .withMessageContaining("Duplicate query parameter name: columnFilter0");
  }

  // --- Result assembly --------------------------------------------------------------------------

  @Test
  @DisplayName("PageResponseDto is assembled from the Panache query result")
  void pageResponseIsAssembledFromResult() {
    var result = repository.searchWithSelect(query(null, null, SupportedLanguage.DE), SORTABLE_FIELDS);

    assertThat(result.items()).containsExactly("a", "b");
    assertThat(result.totalItems()).isEqualTo(2L);
    assertThat(result.totalPages()).isEqualTo(1);
    assertThat(result.currentPage()).isZero();
    assertThat(result.pageSize()).isEqualTo(20);
  }

  /**
   * Test double that intercepts the Panache {@code find(query, params)} call, records its
   * arguments, and returns a stubbed {@link PanacheQuery} so no database is required.
   */
  static class TestableRepository extends BaseSearchRepository<Object, Long> {

    private static final String BASE_SELECT = "select dp from DataProductEntity dp";
    private static final String BASE_WHERE = "dp.dataProviderUid = :providerUid";

    String capturedQuery;
    Map<String, Object> capturedParams;

    private final PanacheQuery<Object> stubQuery;

    @SuppressWarnings("unchecked")
    TestableRepository() {
      stubQuery = mock(PanacheQuery.class);
      when(stubQuery.page(anyInt(), anyInt())).thenReturn(stubQuery);
      when(stubQuery.list()).thenReturn(List.of("a", "b"));
      when(stubQuery.count()).thenReturn(2L);
      when(stubQuery.pageCount()).thenReturn(1);
    }

    @Override
    public PanacheQuery<Object> find(String query, Map<String, Object> params) {
      this.capturedQuery = query;
      this.capturedParams = params;
      return stubQuery;
    }

    PageResponseDto<Object> searchWithSelect(ResourceQueryDto query, Map<String, SearchField> sortableFields) {
      return findPage(
          query, SearchSpec.builder()
              .baseSelect(BASE_SELECT)
              .baseWhere(BASE_WHERE)
              .baseParams(Map.of("providerUid", "X"))
              .searchableFields(SEARCHABLE_FIELDS)
              .sortableFields(sortableFields)
              .sortTieBreaker("dp.id")
              .build()
      );
    }

    PageResponseDto<Object> searchWithoutSelect(ResourceQueryDto query, Map<String, SearchField> sortableFields) {
      return findPage(
          query, SearchSpec.builder()
              .searchableFields(SEARCHABLE_FIELDS)
              .sortableFields(sortableFields)
              .build()
      );
    }

    PageResponseDto<Object> searchWithFilters(ResourceQueryDto query) {
      return findPage(
          query, SearchSpec.builder()
              .baseSelect(BASE_SELECT)
              .filterableFields(FILTERABLE_FIELDS)
              .sortableFields(SORTABLE_FIELDS)
              .sortTieBreaker("dp.id")
              .build()
      );
    }

    PageResponseDto<Object> search(ResourceQueryDto query, SearchSpec spec) {
      return findPage(query, spec);
    }
  }
}
