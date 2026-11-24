package integration.agreement;

import static integration.testutils.TestUserEnum.ADMIN;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;

import ch.agridata.agreement.controller.ContractTemplateAdminController;
import ch.agridata.agreement.service.ContractTemplateVersionEnum;
import integration.testutils.AuthTestUtils;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

/**
 * Verifies that the admin endpoint lists all contract template versions, newest first, with the current version open-ended.
 *
 * @CommentLastReviewed 2026-11-24
 */
@QuarkusTest
class ContractTemplateAdminControllerTest {

  @Test
  void givenAdmin_whenGetAll_thenReturnVersionsNewestFirstWithCurrentOpenEnded() {
    var versions = ContractTemplateVersionEnum.values();
    var newest = versions[versions.length - 1];

    AuthTestUtils.requestAs(ADMIN).given()
        .when()
        .get(ContractTemplateAdminController.PATH)
        .then()
        .statusCode(200)
        .body("$", hasSize(versions.length))
        .body("[0].version", equalTo(newest.getVersion()))
        .body("[0].validFrom", equalTo(newest.getValidFrom().toString()))
        .body("[0].validTo", nullValue());
  }
}
