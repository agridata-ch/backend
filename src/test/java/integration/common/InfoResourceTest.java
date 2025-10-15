package integration.common;

import static ch.agridata.common.actuators.InfoResource.PATH;
import static io.restassured.RestAssured.given;
import static org.hamcrest.text.MatchesPattern.matchesPattern;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class InfoResourceTest {
  @Test
  void givenApplicationRunning_whenInfoResource_thenReturnVersion() {
    given()
        .when().get(PATH).then()
        .statusCode(200)
        .body("version", matchesPattern("^(dev|[0-9]+\\.[0-9]+\\.[0-9]+.*)$"));
  }

}
