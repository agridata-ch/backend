package integration.common.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.common.dto.ResourceQueryDto;
import io.quarkus.test.junit.QuarkusTest;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@QuarkusTest
@RequiredArgsConstructor
class BaseSearchRepositoryTest {

  private final TestEntityRepository repository;

  @ParameterizedTest
  @MethodSource("findPageTestCases")
  @DisplayName("Test findPage with various search criteria")
  void testFindPage(TestCase testCase) {
    var queryDto = ResourceQueryDto.builder()
        .page(testCase.page != null ? testCase.page : 0)
        .size(testCase.size != null ? testCase.size : 20)
        .sortParams(testCase.sortParams)
        .searchTerm(testCase.searchTerm)
        .build();

    var results = repository.searchEntitiesByCategory(queryDto, testCase.category);

    assertThat(results).isNotNull();
    assertThat(results.items()).hasSize(testCase.expectedCount);
    assertThat(results.totalItems()).isEqualTo(testCase.expectedTotalElements);

    // Additional assertions for sorting if specified
    if (testCase.sortParams != null && !testCase.sortParams.isEmpty() && !results.items().isEmpty()) {
      var sortParam = testCase.sortParams.get(0);
      if (sortParam.equals("-description")) {
        // Verify descending sort by description
        var descriptions = results.items().stream()
            .map(entity -> entity.description)
            .collect(Collectors.toList());
        assertThat(descriptions).isSortedAccordingTo(Collections.reverseOrder());
      }
    }
  }

  static Stream<TestCase> findPageTestCases() {
    return Stream.of(
        // Basic search cases
        TestCase.builder()
            .name("Search for 'Jo do' in IT category")
            .category("IT")
            .searchTerm("Jo do")
            .expectedCount(1)
            .expectedTotalElements(1)
            .build(),

        TestCase.builder()
            .name("Search for 'John' in IT category")
            .category("IT")
            .searchTerm("John")
            .expectedCount(4)
            .expectedTotalElements(4)
            .build(),

        TestCase.builder()
            .name("Search for 'Johnson' in any category")
            .category(null)
            .searchTerm("Johnson")
            .expectedCount(3)
            .expectedTotalElements(3)
            .build(),

        // Category filtering cases
        TestCase.builder()
            .name("All IT entries")
            .category("IT")
            .searchTerm(null)
            .expectedCount(6)
            .expectedTotalElements(6)
            .build(),

        TestCase.builder()
            .name("All Management entries")
            .category("Management")
            .searchTerm(null)
            .expectedCount(2)
            .expectedTotalElements(2)
            .build(),

        // Pagination cases
        TestCase.builder()
            .name("First page with size 3")
            .category(null)
            .searchTerm(null)
            .page(0)
            .size(3)
            .expectedCount(3)
            .expectedTotalElements(13)
            .build(),

        TestCase.builder()
            .name("Second page with size 5")
            .category(null)
            .searchTerm(null)
            .page(1)
            .size(5)
            .expectedCount(5)
            .expectedTotalElements(13)
            .build(),

        TestCase.builder()
            .name("Last page with size 10")
            .category(null)
            .searchTerm(null)
            .page(1)
            .size(10)
            .expectedCount(3)
            .expectedTotalElements(13)
            .build(),

        // Sorting cases
        TestCase.builder()
            .name("Sort by description descending")
            .category("IT")
            .searchTerm(null)
            .sortParams(List.of("-description"))
            .expectedCount(6)
            .expectedTotalElements(6)
            .build(),

        TestCase.builder()
            .name("Sort by firstName ascending")
            .category(null)
            .searchTerm(null)
            .sortParams(List.of("firstName"))
            .expectedCount(13)
            .expectedTotalElements(13)
            .build(),

        // Combined search cases (firstName + name combinations)
        TestCase.builder()
            .name("Combined search 'jo Do' (should match John Doe)")
            .category(null)
            .searchTerm("jo Do")
            .expectedCount(1)
            .expectedTotalElements(1)
            .build(),

        TestCase.builder()
            .name("Combined search 'Do Jo' (should match John Doe)")
            .category(null)
            .searchTerm("Do Jo")
            .expectedCount(1)
            .expectedTotalElements(1)
            .build(),

        TestCase.builder()
            .name("Combined search 'John Smith' should not match because no such user with given first and last name")
            .category(null)
            .searchTerm("John Smith")
            .expectedCount(0)
            .expectedTotalElements(0)
            .build(),

        // Edge cases
        TestCase.builder()
            .name("Empty search term")
            .category(null)
            .searchTerm("")
            .expectedCount(13)
            .expectedTotalElements(13)
            .build(),

        TestCase.builder()
            .name("Non-existent category")
            .category("NonExistent")
            .searchTerm(null)
            .expectedCount(0)
            .expectedTotalElements(0)
            .build(),

        TestCase.builder()
            .name("Search term with no matches")
            .category(null)
            .searchTerm("XYZ")
            .expectedCount(0)
            .expectedTotalElements(0)
            .build(),

        // Default values testing
        TestCase.builder()
            .name("Default page and size values")
            .category("IT")
            .searchTerm(null)
            .page(null)
            .size(null)
            .expectedCount(6)
            .expectedTotalElements(6)
            .build()
    );
  }

  @Builder
  static class TestCase {
    String name;
    String category;
    String searchTerm;
    Integer page;
    Integer size;
    List<String> sortParams;
    int expectedCount;
    int expectedTotalElements;

    @Override
    public String toString() {
      return name;
    }
  }
}