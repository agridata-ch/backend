package integration.product;

import static integration.testutils.TestUserEnum.CONSUMER_BIO_SUISSE;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import ch.agridata.product.controller.DataProductController;
import integration.testutils.AuthTestUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;

@QuarkusTest
class DataProductControllerTest {

  @Test
  void givenValidRequest_whenGetProducts_thenValidProducts() {
    RequestSpecification consumer = AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE);

    consumer.when()
        .get(DataProductController.PATH)
        .then()
        .statusCode(200)
        .body("size()", greaterThan(0)) // ensure at least one item is returned
        .body("every { it.id?.toString().length() > 0 }", is(true))
        .body("every { it.name?.toString().length() > 0 }", is(true))
        .body("every { it.description?.toString().length() > 0 }", is(true));
  }

}
