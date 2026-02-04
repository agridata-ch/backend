package integration.datatransferv2;

import static integration.testutils.TestUserEnum.CONSUMER_BIO_SUISSE;
import static org.hamcrest.Matchers.notNullValue;

import ch.agridata.datatransferv2.controller.DataTransferController;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestDataIdentifiers.DataProduct;
import integration.testutils.TestDataIdentifiers.Uid;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class DataTransferV2Test {

  private final Flyway flyway;

  @BeforeEach
  void setUp() {
    // will make sure testdata prior to executing each test
    flyway.migrate();
  }

  @Test
  void givenAcceptedConsentRequest_whenProductRequested_thenProductReturned() {
    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .pathParam("productId", DataProduct.UUID_085E4B72.uuid())
        .queryParam("uid", Uid.CHE103000001.name())
        .queryParam("year", 2024)
        .when().get(DataTransferController.PATH + "/product/{productId}/data")
        .then().statusCode(200)
        .header("AGRIDATA-REQUEST-ID", notNullValue());
  }

  @Test
  void givenOpenConsentRequest_whenProductRequested_thenForbiddenReturned() {
    // CHE102000002 has an open consent request for BIO_SUISSE_02, not granted
    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .pathParam("productId", DataProduct.UUID_085E4B72.uuid())
        .queryParam("uid", Uid.CHE102000002.name())
        .queryParam("year", 2024)
        .when().get(DataTransferController.PATH + "/product/{productId}/data")
        .then()
        .statusCode(403);
  }

  @Test
  void givenDeclinedConsentRequest_whenProductRequested_thenForbiddenReturned() {
    // CHE102000001 has a declined consent request for BIO_SUISSE_02
    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .pathParam("productId", DataProduct.UUID_085E4B72.uuid())
        .queryParam("uid", Uid.CHE102000001.name())
        .queryParam("year", 2024)
        .when().get(DataTransferController.PATH + "/product/{productId}/data")
        .then()
        .statusCode(403);
  }

  @Test
  void givenNoExistingConsentRequest_whenProductRequested_thenForbiddenReturned() {
    // CHE101000001 has no consent request for BIO_SUISSE data requests
    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .pathParam("productId", DataProduct.UUID_085E4B72.uuid())
        .queryParam("uid", Uid.CHE101000001.name())
        .queryParam("year", 2024)
        .when().get(DataTransferController.PATH + "/product/{productId}/data")
        .then()
        .statusCode(403);
  }

  @Test
  void givenNoUidParameter_whenProductRequested_thenBadRequest() {
    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .pathParam("productId", DataProduct.UUID_085E4B72.uuid())
        .queryParam("year", 2024)
        .when().get(DataTransferController.PATH + "/product/{productId}/data")
        .then()
        .statusCode(400);
  }

  @Test
  void givenEmptyUidParameter_whenProductRequested_thenBadRequest() {
    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .pathParam("productId", DataProduct.UUID_085E4B72.uuid())
        .queryParam("uid", "")
        .queryParam("year", 2024)
        .when().get(DataTransferController.PATH + "/product/{productId}/data")
        .then()
        .statusCode(400);
  }
}
