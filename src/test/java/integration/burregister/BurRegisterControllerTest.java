package integration.burregister;

import static integration.testutils.AuthTestUtils.requestAs;
import static integration.testutils.TestUserEnum.ADMIN;
import static integration.testutils.TestUserEnum.CONSUMER_BLV_1;
import static org.hamcrest.Matchers.equalTo;

import ch.agridata.burregister.controller.BurRegisterController;
import integration.testutils.TestDataIdentifiers.Uid;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class BurRegisterControllerTest {

  @Test
  void givenValidRequest_whenGetByUid_thenValidResponse() {
    requestAs(ADMIN).when()
        .get(BurRegisterController.PATH + "/search/" + Uid.CHE113614519.name())
        .then()
        .statusCode(200)
        .body("uid", equalTo(Uid.CHE113614519.name()))
        .body("legalName", equalTo("Bundesamt für Landwirtschaft BLW"))
        .body("address.street", equalTo("Schwarzenburgstrasse 165"));
  }

  @Test
  void givenValidRequest_whenGetByUidOfCurrentUser_thenValidResponse() {
    requestAs(CONSUMER_BLV_1).when()
        .get(BurRegisterController.PATH + "/search")
        .then()
        .statusCode(200)
        .body("uid", equalTo(Uid.CHE403244345.name()))
        .body("address.city", equalTo("Liebefeld"));
  }

  @Test
  void givenInvalidUid_whenGetByUid_thenBadRequest() {
    requestAs(ADMIN).when()
        .get(BurRegisterController.PATH + "/search/113614519")
        .then()
        .statusCode(400);
  }

  @Test
  void givenUnknownUid_whenGetByUid_thenNotFound() {
    requestAs(ADMIN).when()
        .get(BurRegisterController.PATH + "/search/CHE000000000")
        .then()
        .statusCode(404);
  }
}
