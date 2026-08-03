package integration.product;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import ch.agridata.product.controller.PublicDataProviderController;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class PublicDataProviderControllerTest {

  @Test
  void givenUnauthenticatedRequest_whenGetPublicDataProviders_thenReturnDataProviders() {
    given()
        .when()
        .get(PublicDataProviderController.PATH)
        .then()
        .statusCode(200)
        .body("size()", greaterThan(0))
        .body("every { it.id?.toString().length() > 0 }", is(true))
        .body("every { it.name?.toString().length() > 0 }", is(true))
        .body("every { it.code?.toString().length() > 0 }", is(true));
  }
}
