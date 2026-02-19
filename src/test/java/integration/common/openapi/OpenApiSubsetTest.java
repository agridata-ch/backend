package integration.common.openapi;

import static ch.agridata.common.openapi.ApiSubsetConstants.DATA_CONSUMER;
import static ch.agridata.common.openapi.ApiSubsetConstants.DATA_PROVIDER;
import static ch.agridata.common.openapi.ApiSubsetConstants.MOBILE_APP;
import static ch.agridata.common.openapi.ApiSubsetConstants.WEB_APP;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.collection.IsEmptyCollection.empty;

import io.quarkus.test.junit.QuarkusTest;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Verifies that each defined API subset contains at least one endpoint.
 */

@QuarkusTest
class OpenApiSubsetTest {

  static Stream<String> allSubsets() {
    return Stream.of(MOBILE_APP, WEB_APP, DATA_CONSUMER, DATA_PROVIDER);
  }

  @ParameterizedTest
  @MethodSource("allSubsets")
  void subset_should_contain_at_least_one_endpoint(String subset) {
    given()
        .when().get("/q/openapi/subsets/{subset}?format=json", subset)
        .then()
        .statusCode(200)
        .body("paths.keySet()", not(empty()));
  }
}
