package integration.user;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsNot.not;

import ch.agridata.user.controller.AgbRevisionController;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestUserEnum;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;

@QuarkusTest
class AgbRevisionControllerTest {

  private static final String AGB_REVISION_PATH = AgbRevisionController.PATH + "/current-agb-revision";

  @Test
  void givenUnauthenticatedRequest_whenGetCurrentAgbRevision_thenReturnCurrentAgbRevision() {
    given()
        .when()
        .get(AGB_REVISION_PATH)
        .then()
        .statusCode(200)
        .body("id", notNullValue())
        .body("agbText.de", not(emptyOrNullString()))
        .body("validFrom", notNullValue());
  }

  @Test
  void givenAuthenticatedRequest_whenGetCurrentAgbRevision_thenReturnCurrentAgbRevision() {
    RequestSpecification consumer = AuthTestUtils.requestAs(TestUserEnum.CONSUMER_BIO_SUISSE);

    consumer.when()
        .get(AGB_REVISION_PATH)
        .then()
        .statusCode(200)
        .body("id", notNullValue())
        .body("agbText.de", not(emptyOrNullString()))
        .body("validFrom", notNullValue());
  }
}
