package integration.common.security;

import static integration.testutils.TestUserEnum.ADMIN;
import static integration.testutils.TestUserEnum.CONSUMER_BLV_1;
import static integration.testutils.TestUserEnum.CONSUMER_PROVIDER;
import static integration.testutils.TestUserEnum.PRODUCER_A;
import static integration.testutils.TestUserEnum.PROVIDER_1;
import static integration.testutils.TestUserEnum.SUPPORT;
import static org.hamcrest.Matchers.containsString;

import integration.testutils.AuthTestUtils;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

/**
 * End-to-end checks that the {@code actingRole} resolution filter behaves correctly on a representative migrated
 * endpoint ({@code GET /api/agreement/v1/data-requests}). The multi-role branches are exercised via the
 * {@code consumer-provider} realm user which holds both CONSUMER and PROVIDER roles in a single token.
 */
@QuarkusTest
class ActingRoleResolutionTest {

  private static final String DATA_REQUESTS = "/api/agreement/v1/data-requests";
  private static final String AUTHORIZED_UIDS = "/api/user/v1/authorized-uids";

  @Test
  void givenSingleRoleConsumer_whenRequestWithoutActingRoleParam_then200() {
    AuthTestUtils.requestAs(CONSUMER_BLV_1)
        .when().get(DATA_REQUESTS)
        .then().statusCode(200);
  }

  @Test
  void givenSingleRoleConsumer_whenRequestWithMatchingActingRoleParam_then200() {
    AuthTestUtils.requestAs(CONSUMER_BLV_1)
        .queryParam("actingRole", "CONSUMER")
        .when().get(DATA_REQUESTS)
        .then().statusCode(200);
  }

  @Test
  void givenSingleRoleProvider_whenRequestWithoutActingRoleParam_then200() {
    AuthTestUtils.requestAs(PROVIDER_1)
        .when().get(DATA_REQUESTS)
        .then().statusCode(200);
  }

  @Test
  void givenSingleRoleAdmin_whenRequestWithoutActingRoleParam_then200() {
    AuthTestUtils.requestAs(ADMIN)
        .when().get(DATA_REQUESTS)
        .then().statusCode(200);
  }

  @Test
  void givenSingleRoleConsumer_whenRequestWithActingRoleProvider_then403WithFilterMessage() {
    // CONSUMER does not hold the PROVIDER role → filter rejects.
    AuthTestUtils.requestAs(CONSUMER_BLV_1)
        .queryParam("actingRole", "PROVIDER")
        .when().get(DATA_REQUESTS)
        .then()
        .statusCode(403)
        .body(containsString("does not have the role required for actingRole=PROVIDER"));
  }

  @Test
  void givenUnknownActingRoleValue_whenRequest_then400() {
    AuthTestUtils.requestAs(CONSUMER_BLV_1)
        .queryParam("actingRole", "FOO")
        .when().get(DATA_REQUESTS)
        .then()
        .statusCode(400)
        .body(containsString("actingRole=FOO is not a valid value"));
  }

  @Test
  void givenActingRoleNotInEndpointAllowedList_whenRequest_then400() {
    // setSignatureType endpoint allows only CONSUMER/PROVIDER (per @RolesAllowed). Admin is not in the derived
    // allowed set there → 400.
    AuthTestUtils.requestAs(CONSUMER_BLV_1)
        .queryParam("actingRole", "ADMIN")
        .contentType("application/json").body("\"INDIVIDUAL_SIGNATURE\"")
        .when().put("/api/agreement/v1/data-requests/00000000-0000-0000-0000-000000000000/signature-type")
        .then()
        .statusCode(400)
        .body(containsString("actingRole=ADMIN is not allowed for this endpoint"));
  }

  @Test
  void givenSupportImpersonatingProducer_whenRequestWithActingRoleProducer_then403WithFilterMessage() {
    // /authorized-uids allows SUPPORT, so the request passes @RolesAllowed and reaches the filter.
    // The SUPPORT user does not hold the PRODUCER role — impersonation does not widen acting roles → filter 403.
    AuthTestUtils.requestAs(SUPPORT)
        .header("X-Impersonated-AgateLoginId", PRODUCER_A.getAgateLoginId())
        .queryParam("actingRole", "PRODUCER")
        .when().get(AUTHORIZED_UIDS)
        .then()
        .statusCode(403)
        .body(containsString("does not have the role required for actingRole=PRODUCER"));
  }

  @Test
  void givenMultiRoleUser_whenRequestWithoutActingRoleParam_then400() {
    // Ambiguous: the user holds both CONSUMER and PROVIDER, so the filter cannot auto-resolve.
    AuthTestUtils.requestAs(CONSUMER_PROVIDER)
        .when().get(DATA_REQUESTS)
        .then()
        .statusCode(400)
        .body(containsString("actingRole query parameter is required"));
  }

  @Test
  void givenMultiRoleUser_whenRequestWithActingRoleConsumer_then200() {
    AuthTestUtils.requestAs(CONSUMER_PROVIDER)
        .queryParam("actingRole", "CONSUMER")
        .when().get(DATA_REQUESTS)
        .then().statusCode(200);
  }

  @Test
  void givenMultiRoleUser_whenRequestWithActingRoleProvider_then200() {
    AuthTestUtils.requestAs(CONSUMER_PROVIDER)
        .queryParam("actingRole", "PROVIDER")
        .when().get(DATA_REQUESTS)
        .then().statusCode(200);
  }

  @Test
  void givenMultiRoleUser_whenRequestWithActingRoleAdmin_then403WithFilterMessage() {
    // ADMIN is in the endpoint's allowed set, but the user does not hold the admin role → filter 403.
    AuthTestUtils.requestAs(CONSUMER_PROVIDER)
        .queryParam("actingRole", "ADMIN")
        .when().get(DATA_REQUESTS)
        .then()
        .statusCode(403)
        .body(containsString("does not have the role required for actingRole=ADMIN"));
  }

}
