package integration.agreement;

import static integration.agreement.DataRequestTestFactory.createDataRequest;
import static integration.agreement.DataRequestTestFactory.updateLogo;
import static org.hamcrest.Matchers.equalTo;

import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class DataRequestLogoTest {

  @Test
  void givenMissingLogo_whenValidLogoIsSubmitted_thenStatusOk() {
    String id = createDataRequest().then()
        .statusCode(201).extract().path("id");

    updateLogo(id, "test-logo-95kB.png")
        .then()
        .statusCode(204);
  }

  @Test
  void givenMissingLogo_whenTooLargeLogoIsSubmitted_thenReturnBadRequest() {
    String id = createDataRequest().then()
        .statusCode(201).extract().path("id");

    updateLogo(id, "test-logo-110kB.png")
        .then()
        .statusCode(400)
        .body("debugMessage", equalTo("File too large"));
  }

  @Test
  void givenMissingLogo_whenUnsupportedFileTypeIsSubmitted_thenReturnBadRequest() {
    String id = createDataRequest().then()
        .statusCode(201).extract().path("id");

    updateLogo(id, "test-logo-50kB.bmp")
        .then()
        .statusCode(400)
        .body("debugMessage", equalTo("File type not supported: image/bmp"));
  }
}
