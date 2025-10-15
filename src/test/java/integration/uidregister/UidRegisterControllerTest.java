package integration.uidregister;

import static integration.testutils.TestUserEnum.CONSUMER_BIO_SUISSE;
import static org.hamcrest.Matchers.notNullValue;

import ch.agridata.uidregister.controller.UidRegisterController;
import integration.testutils.AuthTestUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;

@QuarkusTest
class UidRegisterControllerTest {

  @Test
  void givenValidRequest_whenGetByUid_thenValidResponse() {
    RequestSpecification consumer = AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE);

    consumer.when()
        .get(UidRegisterController.PATH + "/search")
        .then()
        .statusCode(200)
        .body("legalName", notNullValue())
        .body("address", notNullValue());
  }
}
