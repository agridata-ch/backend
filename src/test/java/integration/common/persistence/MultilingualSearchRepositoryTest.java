package integration.common.persistence;

import static ch.agridata.common.dto.SupportedLanguage.DE;
import static ch.agridata.common.dto.SupportedLanguage.FR;
import static ch.agridata.common.dto.SupportedLanguage.IT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import ch.agridata.common.dto.ResourceQueryDto;
import ch.agridata.common.dto.SupportedLanguage;
import io.quarkus.test.junit.QuarkusTest;
import java.util.List;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Specification for the multilingual extension of {@code BaseSearchRepository#findPage}:
 * searching, filtering and sorting on JSON translation fields ({@code TranslationPersistenceDto})
 * resolved against the request language.
 *
 * <p>Test data (V999.1): 6 entities whose alphabetical name order differs per language,
 * P-600 has no French name translation.
 */
@QuarkusTest
@RequiredArgsConstructor
class MultilingualSearchRepositoryTest {

  private final MultilingualTestEntityRepository repository;

  // --- Search on translated fields -------------------------------------------------------------

  @ParameterizedTest
  @MethodSource("searchTestCases")
  @DisplayName("Search resolves translated fields in the request language")
  void testSearch(SearchCase testCase) {
    var query = ResourceQueryDto.builder()
        .page(0)
        .size(20)
        .searchTerm(testCase.searchTerm)
        .language(testCase.language.code())
        .build();

    var result = repository.search(query, testCase.category);

    assertThat(result.items())
        .extracting(e -> e.code)
        .containsExactlyInAnyOrderElementsOf(testCase.expectedCodes);
    assertThat(result.totalItems()).isEqualTo(testCase.expectedCodes.size());
  }

  static Stream<SearchCase> searchTestCases() {
    return Stream.of(
        SearchCase.builder()
            .name("Match in request language FR")
            .searchTerm("pomme").language(FR)
            .expectedCodes(List.of("P-100"))
            .build(),

        SearchCase.builder()
            .name("Same term does NOT match in language DE (no cross-language leakage)")
            .searchTerm("pomme").language(DE)
            .expectedCodes(List.of())
            .build(),

        SearchCase.builder()
            .name("Match in request language IT")
            .searchTerm("cipolla").language(IT)
            .expectedCodes(List.of("P-300"))
            .build(),

        SearchCase.builder()
            .name("Match in request language DE")
            .searchTerm("zwiebel").language(DE)
            .expectedCodes(List.of("P-300"))
            .build(),

        SearchCase.builder()
            .name("Search is case-insensitive")
            .searchTerm("POMME").language(FR)
            .expectedCodes(List.of("P-100"))
            .build(),

        SearchCase.builder()
            .name("Partial (contains) match")
            .searchTerm("arot").language(FR) // Carotte
            .expectedCodes(List.of("P-400"))
            .build(),

        SearchCase.builder()
            .name("Search term also matches second translated field (description)")
            .searchTerm("vitamines").language(FR)
            .expectedCodes(List.of("P-400"))
            .build(),

        SearchCase.builder()
            .name("Simple (single-language) field still searchable alongside translated fields")
            .searchTerm("p-300").language(DE)
            .expectedCodes(List.of("P-300"))
            .build(),

        SearchCase.builder()
            .name("Combined fields: tokens distributed over name + description (DE)")
            .searchTerm("apfel knackig").language(DE)
            .expectedCodes(List.of("P-100"))
            .build(),

        SearchCase.builder()
            .name("Combined fields: token order irrelevant")
            .searchTerm("knackig apfel").language(DE)
            .expectedCodes(List.of("P-100"))
            .build(),

        SearchCase.builder()
            .name("Missing translation is not found in that language (P-600 has no fr name)")
            .searchTerm("uovo").language(FR)
            .expectedCodes(List.of())
            .build(),

        SearchCase.builder()
            .name("Missing translation is found in the language where it exists")
            .searchTerm("uovo").language(IT)
            .expectedCodes(List.of("P-600"))
            .build(),

        SearchCase.builder()
            .name("Search combined with base WHERE filter (category)")
            .searchTerm("arot").language(FR).category("VEGETABLE")
            .expectedCodes(List.of("P-400"))
            .build(),

        SearchCase.builder()
            .name("Base WHERE filter excludes matches from other categories")
            .searchTerm("arot").language(FR).category("FRUIT")
            .expectedCodes(List.of())
            .build(),

        SearchCase.builder()
            .name("Filter only, no search term")
            .searchTerm(null).language(DE).category("FRUIT")
            .expectedCodes(List.of("P-100", "P-200"))
            .build(),

        SearchCase.builder()
            .name("No search term and no filter returns everything")
            .searchTerm(null).language(DE)
            .expectedCodes(List.of("P-100", "P-200", "P-300", "P-400", "P-500", "P-600"))
            .build(),

        SearchCase.builder()
            .name("No match returns empty page")
            .searchTerm("ananas").language(DE)
            .expectedCodes(List.of())
            .build()
    );
  }

  // --- Sorting on translated fields ------------------------------------------------------------

  @ParameterizedTest
  @MethodSource("sortTestCases")
  @DisplayName("Sorting on translated fields follows the request language")
  void testSort(SortCase testCase) {
    var query = ResourceQueryDto.builder()
        .page(0)
        .size(20)
        .sortParams(testCase.sortParams)
        .language(testCase.language.code())
        .build();

    var result = repository.search(query, null);

    assertThat(result.items())
        .extracting(e -> e.code)
        .containsExactlyElementsOf(testCase.expectedOrder);
  }

  static Stream<SortCase> sortTestCases() {
    return Stream.of(
        // Order differs per language for the SAME data - this is the core requirement.
        SortCase.builder()
            .name("Sort by name ascending in DE (Apfel, Birne, Ei, Karotte, Milch, Zwiebel)")
            .sortParams(List.of("name")).language(DE)
            .expectedOrder(List.of("P-100", "P-200", "P-600", "P-400", "P-500", "P-300"))
            .build(),

        SortCase.builder()
            .name("Sort by name ascending in IT (Carota, Cipolla, Latte, Mela, Pera, Uovo)")
            .sortParams(List.of("name")).language(IT)
            .expectedOrder(List.of("P-400", "P-300", "P-500", "P-100", "P-200", "P-600"))
            .build(),

        SortCase.builder()
            .name("Sort by name ascending in FR, missing translation sorts last "
                + "(Carotte, Lait, Oignon, Poire, Pomme, <null>)")
            .sortParams(List.of("name")).language(FR)
            .expectedOrder(List.of("P-400", "P-500", "P-300", "P-200", "P-100", "P-600"))
            .build(),

        SortCase.builder()
            .name("Sort by name descending in DE")
            .sortParams(List.of("-name")).language(DE)
            .expectedOrder(List.of("P-300", "P-500", "P-400", "P-600", "P-200", "P-100"))
            .build(),

        SortCase.builder()
            .name("Sort by simple field via whitelist still works")
            .sortParams(List.of("-code")).language(DE)
            .expectedOrder(List.of("P-600", "P-500", "P-400", "P-300", "P-200", "P-100"))
            .build()
    );
  }

  @Test
  @DisplayName("Unknown sort key is rejected (whitelist)")
  void testUnknownSortKeyIsRejected() {
    var query = ResourceQueryDto.builder()
        .page(0)
        .size(20)
        .sortParams(List.of("createdBy; drop table users"))
        .language(DE.code())
        .build();

    assertThatIllegalArgumentException()
        .isThrownBy(() -> repository.search(query, null));
  }

  // --- Pagination with multilingual sort -------------------------------------------------------

  @Nested
  @RequiredArgsConstructor
  class Pagination {

    @Test
    @DisplayName("First page with language-specific sort")
    void testFirstPage() {
      var query = ResourceQueryDto.builder()
          .page(0)
          .size(3)
          .sortParams(List.of("name"))
          .language(DE.code())
          .build();

      var result = repository.search(query, null);

      assertThat(result.items()).extracting(e -> e.code)
          .containsExactly("P-100", "P-200", "P-600");
      assertThat(result.totalItems()).isEqualTo(6);
      assertThat(result.totalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("Second page continues the language-specific order")
    void testSecondPage() {
      var query = ResourceQueryDto.builder()
          .page(1)
          .size(3)
          .sortParams(List.of("name"))
          .language(DE.code())
          .build();

      var result = repository.search(query, null);

      assertThat(result.items()).extracting(e -> e.code)
          .containsExactly("P-400", "P-500", "P-300");
    }
  }

  // --- Test case records ------------------------------------------------------------------------

  @Builder
  static class SearchCase {
    String name;
    String searchTerm;
    SupportedLanguage language;
    String category;
    List<String> expectedCodes;

    @Override
    public String toString() {
      return name;
    }
  }

  @Builder
  static class SortCase {
    String name;
    List<String> sortParams;
    SupportedLanguage language;
    List<String> expectedOrder;

    @Override
    public String toString() {
      return name;
    }
  }
}
