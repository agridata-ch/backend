package integration.datatransferv2;

import static integration.testutils.TestUserEnum.CONSUMER_BLV_1;
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
class UidBasedPostValidationFlowTest {

  private static final Identifier<DataProductEntity> PRODUCT_UID_BASED_POST_VALIDATION = DataProduct.UUID_298B653C;

  private final Flyway flyway;

  WireMock wireMock;

  @BeforeEach
  void setUp() {
    // will make sure testdata prior to executing each test
    flyway.migrate();
    wireMock.resetRequests();
  }

  @Test
  void givenAcceptedConsentRequest_whenProductRequested_thenProductReturned() {
    AuthTestUtils.requestAs(CONSUMER_BLV_1)
        .pathParam("productId", PRODUCT_UID_BASED_POST_VALIDATION.uuid())
        .queryParam("uid", Uid.ZZZ199984051.name())
        .when().get(DataTransferController.PATH + "/product/{productId}/data")
        .then().statusCode(200)
        .header("AGRIDATA-REQUEST-ID", notNullValue());

    wireMock.verifyThat(1, WireMock.getRequestedFor(
        WireMock.urlEqualTo(
            "/tvd/animal-tracing/v1.0/equid/shared-data/legalunits/" + Uid.ZZZ199984051.name()
                + "/ownership?dataPackage=TVD_EquidOwnershipListV1")));
  }

  @Test
  void givenNoExistingConsentRequest_whenProductRequested_thenForbiddenReturned() {
    AuthTestUtils.requestAs(CONSUMER_BLV_1)
        .pathParam("productId", PRODUCT_UID_BASED_POST_VALIDATION.uuid())
        .queryParam("uid", Uid.CHE101000001.name())
        .when().get(DataTransferController.PATH + "/product/{productId}/data")
        .then()
        .statusCode(403);

    wireMock.verifyThat(0, WireMock.anyRequestedFor(
        WireMock.urlPathMatching(".*/tvd/.*")));
  }

  @Test
  void givenNoUidParameter_whenProductRequested_thenBadRequest() {
    AuthTestUtils.requestAs(CONSUMER_BLV_1)
        .pathParam("productId", PRODUCT_UID_BASED_POST_VALIDATION.uuid())
        .when().get(DataTransferController.PATH + "/product/{productId}/data")
        .then()
        .statusCode(400);

    wireMock.verifyThat(0, WireMock.anyRequestedFor(
        WireMock.urlPathMatching(".*/tvd/.*")));
  }

  @Test
  void givenEmptyUidParameter_whenProductRequested_thenBadRequest() {
    AuthTestUtils.requestAs(CONSUMER_BLV_1)
        .pathParam("productId", PRODUCT_UID_BASED_POST_VALIDATION.uuid())
        .queryParam("uid", "")
        .when().get(DataTransferController.PATH + "/product/{productId}/data")
        .then()
        .statusCode(400);

    wireMock.verifyThat(0, WireMock.anyRequestedFor(
        WireMock.urlPathMatching(".*/tvd/.*")));
  }

  @Test
  void givenNoConsumerUidInTokenButInResponseHeader_whenProductRequested_thenProductReturned() {
    AuthTestUtils.requestAs(CONSUMER_BLV_WITHOUT_UID)
        .pathParam("productId", PRODUCT_UID_BASED_POST_VALIDATION.uuid())
        .queryParam("uid", Uid.ZZZ199984051.name())
        .queryParam("year", 2024)
        .when().get(DataTransferController.PATH + "/product/{productId}/data")
        .then()
        .statusCode(200);

    wireMock.verifyThat(1, WireMock.getRequestedFor(
        WireMock.urlEqualTo(
            "/tvd/animal-tracing/v1.0/equid/shared-data/legalunits/" + Uid.ZZZ199984051.name()
                + "/ownership?dataPackage=TVD_EquidOwnershipListV1&year=2024")));
  }
}
