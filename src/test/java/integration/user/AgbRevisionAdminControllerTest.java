package integration.user;

import static ch.agridata.common.utils.AuthenticationUtil.ADMIN_ROLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import ch.agridata.user.controller.AgbRevisionAdminController;
import integration.testutils.AccessTestUtils;
import integration.testutils.AccessTestUtils.HttpMethod;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestUserEnum;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.Comparator;
import java.util.List;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Integration tests for the {@link AgbRevisionAdminController}.
 *
 * @CommentLastReviewed 2026-10-06
 */
@QuarkusTest
class AgbRevisionAdminControllerTest {

  private static final String PATH = AgbRevisionAdminController.PATH;

  @Inject
  Flyway flyway;

  @BeforeEach
  void setUp() {
    flyway.migrate();
  }

  @Test
  void givenAdmin_whenGetAllAgbRevisions_thenReturnOverviewWithoutText() {
    AuthTestUtils.requestAs(TestUserEnum.ADMIN)
        .when()
        .get(PATH)
        .then()
        .statusCode(200)
        .body("items", not(empty()))
        .body("items[0].id", notNullValue())
        .body("items[0].version", notNullValue())
        .body("items[0].validFrom", notNullValue())
        .body("items[0]", not(hasKey("agbText")));
  }

  @Test
  void givenAdmin_whenPageSizeOne_thenReturnSingleItemAndTotalCount() {
    var total = getItems(null, "version", 100).size();

    AuthTestUtils.requestAs(TestUserEnum.ADMIN)
        .queryParam("page", 0)
        .queryParam("size", 1)
        .when()
        .get(PATH)
        .then()
        .statusCode(200)
        .body("items", hasSize(1))
        .body("totalItems", equalTo(total))
        .body("totalPages", equalTo(total));
  }

  @Test
  void givenAdmin_whenNoSortGiven_thenOrderedByValidFromDescending() {
    var validFrom = getItems(null, "validFrom", 100);

    assertThat(validFrom).isSortedAccordingTo(Comparator.reverseOrder());
  }

  @Test
  void givenAdmin_whenSortByVersionAscAndDesc_thenOrderIsReversed() {
    var asc = getItems("version", "version", 100);
    var desc = getItems("-version", "version", 100);

    assertThat(desc).containsExactlyElementsOf(asc.reversed());
  }

  @ParameterizedTest
  @ValueSource(strings = {"validTo", "-validTo", "enforceConsentFrom", "-enforceConsentFrom"})
  void givenAdmin_whenSortByOtherSortableField_thenAllRevisionsReturned(String sortBy) {
    var unsorted = getItems(null, "version", 100);

    assertThat(getItems(sortBy, "version", 100)).containsExactlyInAnyOrderElementsOf(unsorted);
  }

  @Test
  void givenAdmin_whenSortByUnknownField_thenBadRequest() {
    AuthTestUtils.requestAs(TestUserEnum.ADMIN)
        .queryParam("sortBy", "agbText")
        .when()
        .get(PATH)
        .then()
        .statusCode(400);
  }

  @Test
  void whenGetAllAgbRevisions_thenOnlyAdminAllowed() {
    AccessTestUtils.assertForbiddenForAllExcept(HttpMethod.GET, PATH, ADMIN_ROLE);
  }

  private List<String> getItems(String sortBy, String field, int size) {
    var request = AuthTestUtils.requestAs(TestUserEnum.ADMIN).queryParam("size", size);
    if (sortBy != null) {
      request = request.queryParam("sortBy", sortBy);
    }
    return request
        .when()
        .get(PATH)
        .then()
        .statusCode(200)
        .extract()
        .jsonPath()
        .getList("items." + field, String.class);
  }
}
