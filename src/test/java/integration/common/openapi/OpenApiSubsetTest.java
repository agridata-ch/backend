package integration.common.openapi;

import static ch.agridata.common.openapi.ApiSubsetConstants.DATA_CONSUMER;
import static ch.agridata.common.openapi.ApiSubsetConstants.DATA_PROVIDER;
import static ch.agridata.common.openapi.ApiSubsetConstants.MOBILE_APP;
import static ch.agridata.common.openapi.ApiSubsetConstants.WEB_APP;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.collection.IsEmptyCollection.empty;

import io.quarkus.test.junit.QuarkusTest;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
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

  @ParameterizedTest
  @MethodSource("allSubsets")
  void subset_should_be_valid_openapi_document(String subset) {
    String json = given()
        .when().get("/q/openapi/subsets/{subset}?format=json", subset)
        .then()
        .statusCode(200)
        .extract().asString();

    ParseOptions options = new ParseOptions();
    options.setResolve(false);
    SwaggerParseResult result = new OpenAPIParser().readContents(json, null, options);

    assertThat(result.getOpenAPI()).as("parsed OpenAPI model for subset '%s'", subset).isNotNull();
    assertThat(result.getMessages()).as("validation errors for subset '%s'", subset).isEmpty();
  }
}
