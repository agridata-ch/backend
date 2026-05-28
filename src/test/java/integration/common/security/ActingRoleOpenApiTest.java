package integration.common.security;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

/**
 * Verifies the OpenAPI document and the per-subset OpenAPI documents render the {@code actingRole} query parameter
 * for migrated endpoints and only for those.
 */
@QuarkusTest
class ActingRoleOpenApiTest {

  @Test
  void migratedEndpoint_listsActingRoleQueryParameter() {
    given()
        .when().get("/q/openapi?format=json")
        .then()
        .statusCode(200)
        .body("paths.'/api/agreement/v1/data-requests'.get.parameters.findAll { it.name == 'actingRole' }.size()", is(1))
        .body("paths.'/api/agreement/v1/data-requests'.get.parameters.find { it.name == 'actingRole' }.in",
            is("query"))
        .body("paths.'/api/agreement/v1/data-requests'.get.parameters.find { it.name == 'actingRole' }.required",
            is(false))
        .body("paths.'/api/agreement/v1/data-requests'.get.parameters.find { it.name == 'actingRole' }.schema.enum",
            hasItems("CONSUMER", "PROVIDER", "ADMIN"));
  }

  @Test
  void nonMigratedEndpoint_doesNotListActingRoleQueryParameter() {
    given()
        .when().get("/q/openapi?format=json")
        .then()
        .statusCode(200)
        // createDataRequestDraft (POST) is not @EnableActingRoleHolder-annotated; either no parameters or none named actingRole.
        .body("paths.'/api/agreement/v1/data-requests'.post.parameters.findAll { it.name == 'actingRole' }", empty());
  }

  @Test
  void perSubsetOpenApi_includesActingRoleOnMigratedEndpoint() {
    given()
        .when().get("/q/openapi/subsets/{subset}?format=json", "agridata.ch Web App")
        .then()
        .statusCode(200)
        .body("paths.'/api/agreement/v1/data-requests'.get.parameters.findAll { it.name == 'actingRole' }.size()",
            is(1));
  }
}
