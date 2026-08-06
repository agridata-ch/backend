package ch.agridata.common.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.common.dto.ResourceQueryDto;
import ch.agridata.common.dto.SupportedLanguage;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import java.util.List;
import java.util.Map;
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

  private static final List<SearchField> SEARCHABLE_FIELDS = List.of(
      SearchField.translated("dp.name"),
      SearchField.simple("code")
  );

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

    PageResponseDto<Object> search(ResourceQueryDto query, SearchSpec spec) {
      return findPage(query, spec);
    }
  }
}
