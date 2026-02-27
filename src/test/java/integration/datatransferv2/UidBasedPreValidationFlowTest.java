package integration.datatransferv2;

import static integration.testutils.TestUserEnum.CONSUMER_BIO_SUISSE;
import static integration.testutils.TestUserEnum.CONSUMER_BLV_WITHOUT_UID;
import static org.hamcrest.Matchers.notNullValue;

import ch.agridata.datatransferv2.controller.DataTransferController;
import ch.agridata.product.persistence.DataProductEntity;
import com.github.tomakehurst.wiremock.client.WireMock;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestDataIdentifiers.DataProduct;
import integration.testutils.TestDataIdentifiers.Identifier;
import integration.testutils.TestDataIdentifiers.Uid;
import io.quarkiverse.wiremock.devservice.ConnectWireMock;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
@ConnectWireMock
class UidBasedPreValidationFlowTest {

  private static final Identifier<DataProductEntity> PRODUCT_UID_BASED_PRE_VALIDATION = DataProduct.UUID_085E4B72;

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
    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .pathParam("productId", PRODUCT_UID_BASED_PRE_VALIDATION.uuid())
        .queryParam("uid", Uid.CHE103000001.name())
        .queryParam("year", 2024)
        .when().get(DataTransferController.PATH + "/product/{productId}/data")
        .then().statusCode(200)
        .header("AGRIDATA-REQUEST-ID", notNullValue());

    wireMock.verifyThat(1, WireMock.postRequestedFor(
            WireMock.urlEqualTo("/agis/structure-data/1/structure"))
        .withRequestBody(WireMock.equalToJson("""
            {
              "surveyYear" : "2024",
              "ids" : {
                "uid" : "CHE103000001"
              },
              "dataTypes" : {
                "structureType" : [ "animalData" ]
              },
              "dataRequestType" : {
                "finalData" : false
              }
            }
            """)));
  }

  @Test
  void givenOpenConsentRequest_whenProductRequested_thenForbiddenReturned() {
    // CHE102000002 has an open consent request for BIO_SUISSE_02, not granted
    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .pathParam("productId", PRODUCT_UID_BASED_PRE_VALIDATION.uuid())
        .queryParam("uid", Uid.CHE102000002.name())
        .queryParam("year", 2024)
        .when().get(DataTransferController.PATH + "/product/{productId}/data")
        .then()
        .statusCode(403);

    wireMock.verifyThat(0, WireMock.anyRequestedFor(
        WireMock.urlPathMatching(".*/agis/structure-data/.*")));
  }

  @Test
  void givenDeclinedConsentRequest_whenProductRequested_thenForbiddenReturned() {
    // CHE102000001 has a declined consent request for BIO_SUISSE_02
    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .pathParam("productId", PRODUCT_UID_BASED_PRE_VALIDATION.uuid())
        .queryParam("uid", Uid.CHE102000001.name())
        .queryParam("year", 2024)
        .when().get(DataTransferController.PATH + "/product/{productId}/data")
        .then()
        .statusCode(403);

    wireMock.verifyThat(0, WireMock.anyRequestedFor(
        WireMock.urlPathMatching(".*/agis/structure-data/.*")));
  }

  @Test
  void givenNoExistingConsentRequest_whenProductRequested_thenForbiddenReturned() {
    // CHE101000001 has no consent request for BIO_SUISSE data requests
    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .pathParam("productId", PRODUCT_UID_BASED_PRE_VALIDATION.uuid())
        .queryParam("uid", Uid.CHE101000001.name())
        .queryParam("year", 2024)
        .when().get(DataTransferController.PATH + "/product/{productId}/data")
        .then()
        .statusCode(403);
    wireMock.verifyThat(0, WireMock.anyRequestedFor(
        WireMock.urlPathMatching(".*/agis/structure-data/.*")));
  }

  @Test
  void givenNoUidParameter_whenProductRequested_thenBadRequest() {
    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .pathParam("productId", PRODUCT_UID_BASED_PRE_VALIDATION.uuid())
        .queryParam("year", 2024)
        .when().get(DataTransferController.PATH + "/product/{productId}/data")
        .then()
        .statusCode(400);

    wireMock.verifyThat(0, WireMock.anyRequestedFor(
        WireMock.urlPathMatching(".*/agis/structure-data/.*")));
  }

  @Test
  void givenEmptyUidParameter_whenProductRequested_thenBadRequest() {
    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .pathParam("productId", PRODUCT_UID_BASED_PRE_VALIDATION.uuid())
        .queryParam("uid", "")
        .queryParam("year", 2024)
        .when().get(DataTransferController.PATH + "/product/{productId}/data")
        .then()
        .statusCode(400);

    wireMock.verifyThat(0, WireMock.anyRequestedFor(
        WireMock.urlPathMatching(".*/agis/structure-data/.*")));
  }

  @Test
  void givenNoConsumerUidInToken_whenProductRequested_thenForbiddenReturned() {
    AuthTestUtils.requestAs(CONSUMER_BLV_WITHOUT_UID)
        .pathParam("productId", PRODUCT_UID_BASED_PRE_VALIDATION.uuid())
        .queryParam("uid", Uid.CHE103000001.name())
        .queryParam("year", 2024)
        .when().get(DataTransferController.PATH + "/product/{productId}/data")
        .then()
        .statusCode(403);

    wireMock.verifyThat(0, WireMock.anyRequestedFor(
        WireMock.urlPathMatching(".*/agis/structure-data/.*")));
  }
}
