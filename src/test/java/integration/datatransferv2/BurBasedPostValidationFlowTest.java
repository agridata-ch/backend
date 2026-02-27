package integration.datatransferv2;

import static integration.testutils.TestUserEnum.CONSUMER_BLV_1;
import static integration.testutils.TestUserEnum.CONSUMER_BLV_WITHOUT_UID;
import static org.hamcrest.Matchers.notNullValue;

import ch.agridata.datatransferv2.controller.DataTransferController;
import ch.agridata.product.persistence.DataProductEntity;
import com.github.tomakehurst.wiremock.client.WireMock;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestDataIdentifiers.Bur;
import integration.testutils.TestDataIdentifiers.DataProduct;
import integration.testutils.TestDataIdentifiers.Identifier;
import io.quarkiverse.wiremock.devservice.ConnectWireMock;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
@ConnectWireMock
class BurBasedPostValidationFlowTest {

  private static final Identifier<DataProductEntity> PRODUCT_BUR_BASED_POST_VALIDATION = DataProduct.UUID_E08AF9D2;

  private final Flyway flyway;

  WireMock wireMock;

  @BeforeEach
  void setUp() {
    // will make sure testdata prior to executing each test
    flyway.migrate();
    wireMock.resetToDefaultMappings();
  }

  @Test
  void givenAcceptedConsentRequest_whenProductRequested_thenProductReturned() {
    AuthTestUtils.requestAs(CONSUMER_BLV_1)
        .pathParam("productId", PRODUCT_BUR_BASED_POST_VALIDATION.uuid())
        .queryParam("bur", Bur._99910002.getCode())
        .queryParam("recipientUid", "CHE123456789")
        .when().get(DataTransferController.PATH + "/product/{productId}/data")
        .then().statusCode(200)
        .header("AGRIDATA-REQUEST-ID", notNullValue());

    wireMock.verifyThat(1, WireMock.getRequestedFor(
        WireMock.urlEqualTo(
            "/tvd/animal-tracing/v1.0/customer/shared-data/localunits/" + Bur._99910002.getCode()
                + "?dataPackage=TVD_FarmDataV1&recipientUid=CHE123456789")));
  }

  @Test
  void givenNoExistingConsentRequest_whenProductRequested_thenForbiddenReturned() {
    AuthTestUtils.requestAs(CONSUMER_BLV_1)
        .pathParam("productId", PRODUCT_BUR_BASED_POST_VALIDATION.uuid())
        .queryParam("bur", Bur._99910004.getCode())
        .queryParam("recipientUid", "CHE123456789")
        .when().get(DataTransferController.PATH + "/product/{productId}/data")
        .then()
        .statusCode(403);

    wireMock.verifyThat(0, WireMock.anyRequestedFor(
        WireMock.urlPathMatching(".*/tvd/.*")));
  }

  @Test
  void givenGrantedButOutdatedConsentRequest_whenProductWithHistoricDateRequested_thenForbiddenReturned() {
    AuthTestUtils.requestAs(CONSUMER_BLV_1)
        .pathParam("productId", PRODUCT_BUR_BASED_POST_VALIDATION.uuid())
        .queryParam("bur", Bur._99910004.getCode())
        .queryParam("recipientUid", "CHE123456789")
        .queryParam("date", "2000-01-01")
        .when().get(DataTransferController.PATH + "/product/{productId}/data")
        .then()
        .statusCode(403);

    wireMock.verifyThat(0, WireMock.anyRequestedFor(
        WireMock.urlPathMatching(".*/tvd/.*")));
  }

  @Test
  void givenGrantedButOutdatedConsentRequest_whenProductWithHistoricDateFromRequested_thenForbiddenReturned() {
    AuthTestUtils.requestAs(CONSUMER_BLV_1)
        .pathParam("productId", PRODUCT_BUR_BASED_POST_VALIDATION.uuid())
        .queryParam("bur", Bur._99910004.getCode())
        .queryParam("recipientUid", "CHE123456789")
        .queryParam("dateFrom", "2000-01-01")
        .when().get(DataTransferController.PATH + "/product/{productId}/data")
        .then()
        .statusCode(403);

    wireMock.verifyThat(0, WireMock.anyRequestedFor(
        WireMock.urlPathMatching(".*/tvd/.*")));
  }

  @Test
  void givenNoBurParameter_whenProductRequested_thenBadRequest() {
    AuthTestUtils.requestAs(CONSUMER_BLV_1)
        .pathParam("productId", PRODUCT_BUR_BASED_POST_VALIDATION.uuid())
        .queryParam("recipientUid", "CHE123456789")
        .when().get(DataTransferController.PATH + "/product/{productId}/data")
        .then()
        .statusCode(400);

    wireMock.verifyThat(0, WireMock.anyRequestedFor(
        WireMock.urlPathMatching(".*/tvd/.*")));
  }

  @Test
  void givenEmptyBurParameter_whenProductRequested_thenBadRequest() {
    AuthTestUtils.requestAs(CONSUMER_BLV_1)
        .pathParam("productId", PRODUCT_BUR_BASED_POST_VALIDATION.uuid())
        .queryParam("bur", "")
        .queryParam("recipientUid", "CHE123456789")
        .when().get(DataTransferController.PATH + "/product/{productId}/data")
        .then()
        .statusCode(400);

    wireMock.verifyThat(0, WireMock.anyRequestedFor(
        WireMock.urlPathMatching(".*/tvd/.*")));
  }

  @Test
  void givenNoConsumerUidInTokenButInResponseHeader_whenProductRequested_thenProductReturned() {
    AuthTestUtils.requestAs(CONSUMER_BLV_WITHOUT_UID)
        .pathParam("productId", PRODUCT_BUR_BASED_POST_VALIDATION.uuid())
        .queryParam("bur", Bur._99910002.getCode())
        .queryParam("recipientUid", "CHE123456789")
        .when().get(DataTransferController.PATH + "/product/{productId}/data")
        .then()
        .statusCode(200);

    wireMock.verifyThat(1, WireMock.getRequestedFor(
        WireMock.urlEqualTo(
            "/tvd/animal-tracing/v1.0/customer/shared-data/localunits/" + Bur._99910002.getCode()
                + "?dataPackage=TVD_FarmDataV1&recipientUid=CHE123456789")));
  }

  @Test
  void givenAdditionalQueryParametersInRequest_whenProductRequested_thenParametersAreForwardedToProvider() {
    AuthTestUtils.requestAs(CONSUMER_BLV_1)
        .pathParam("productId", PRODUCT_BUR_BASED_POST_VALIDATION.uuid())
        .queryParam("bur", Bur._99910002.getCode())
        .queryParam("recipientUid", "CHE123456789")
        .queryParam("additionalParam", "dummyParam")
        .when().get(DataTransferController.PATH + "/product/{productId}/data")
        .then()
        .statusCode(200);

    wireMock.verifyThat(1, WireMock.getRequestedFor(
        WireMock.urlEqualTo(
            "/tvd/animal-tracing/v1.0/customer/shared-data/localunits/" + Bur._99910002.getCode()
                + "?dataPackage=TVD_FarmDataV1&recipientUid=CHE123456789&additionalParam=dummyParam")));
  }
}
