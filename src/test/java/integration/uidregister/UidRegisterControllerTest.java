package integration.uidregister;

import static integration.testutils.AuthTestUtils.requestAs;
import static integration.testutils.TestUserEnum.ADMIN;
import static integration.testutils.TestUserEnum.CONSUMER_BLV_1;
import static org.hamcrest.Matchers.notNullValue;

import ch.agridata.uidregister.controller.UidRegisterController;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class UidRegisterControllerTest {

  @Test
  void givenValidRequest_whenGetByUid_thenValidResponse() {
    requestAs(ADMIN).when()
        .get(UidRegisterController.PATH + "/search/403244345")
        .then()
        .statusCode(200)
        .body("legalName", notNullValue())
        .body("address", notNullValue());
  }

  @Test
  void givenValidRequest_whenGetByUidOfCurrentUser_thenValidResponse() {
    requestAs(CONSUMER_BLV_1).when()
        .get(UidRegisterController.PATH + "/search")
        .then()
        .statusCode(200)
        .body("legalName", notNullValue())
        .body("address", notNullValue());
  }
}
