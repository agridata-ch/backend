package integration.datatransfer;

import static integration.testutils.TestDataIdentifiers.Uid.CHE142;
import static integration.testutils.TestDataIdentifiers.Uid.CHE278;
import static integration.testutils.TestDataIdentifiers.Uid.CHE299;
import static integration.testutils.TestDataIdentifiers.Uid.CHE860;
import static integration.testutils.TestUserEnum.CONSUMER_BIO_SUISSE;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import ch.agridata.datatransfer.controller.DataTransferController;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestDataIdentifiers;
import integration.testutils.TestDataIdentifiers.ConsentRequest;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class DataTransferTest {

  @Test
  void givenAcceptedConsentRequest_whenProductRequested_thenProductReturned() {
    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .pathParam("productId", TestDataIdentifiers.DataProduct.UUID_085E4B72.uuid())
        .queryParam("uid", CHE278)
        .queryParam("year", 2024)
        .when().get(DataTransferController.PATH + "/product/{productId}/data")
        .then().statusCode(200)
        .body("dataTransferRequestId", notNullValue())
        .body("consentRequestId", equalTo(ConsentRequest.BIO_SUISSE_02_CHE_278.toString()))
        .body("data.encryptedDataOfProduct", equalTo(TestDataIdentifiers.DataProduct.UUID_085E4B72.toString()));
  }

  @Test
  void givenOpenConsentRequest_whenProductRequested_thenExceptionThrown() {
    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .pathParam("productId", TestDataIdentifiers.DataProduct.UUID_085E4B72.uuid())
        .queryParam("uid", CHE860)
        .queryParam("year", 2024)
        .when().get(DataTransferController.PATH + "/product/{productId}/data")
        .then()
        .statusCode(400)
        .body("debugMessage", equalTo(String.format("no consent for uid: %s and productId: %s found",
            CHE860,
            TestDataIdentifiers.DataProduct.UUID_085E4B72.uuid())));
  }

  @Test
  void givenDeclinedConsentRequest_whenProductRequested_thenExceptionThrown() {
    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .pathParam("productId", TestDataIdentifiers.DataProduct.UUID_085E4B72.uuid())
        .queryParam("uid", CHE142)
        .queryParam("year", 2024)
        .when().get(DataTransferController.PATH + "/product/{productId}/data")
        .then()
        .statusCode(400)
        .body("debugMessage", equalTo(String.format("no consent for uid: %s and productId: %s found",
            CHE142,
            TestDataIdentifiers.DataProduct.UUID_085E4B72.uuid())));
  }


  @Test
  void givenNoExistingConsentRequest_whenProductRequested_thenExceptionThrown() {
    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .pathParam("productId", TestDataIdentifiers.DataProduct.UUID_085E4B72.uuid())
        .queryParam("uid", CHE299)
        .queryParam("year", 2024)
        .when().get(DataTransferController.PATH + "/product/{productId}/data")
        .then()
        .statusCode(400)
        .body("debugMessage", equalTo(String.format("no consent for uid: %s and productId: %s found",
            CHE299,
            TestDataIdentifiers.DataProduct.UUID_085E4B72.uuid())));
  }

  @Test
  void givenNoUid_whenProductRequested_thenBadRequest() {
    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .pathParam("productId", TestDataIdentifiers.DataProduct.UUID_085E4B72.uuid())
        .queryParam("year", 2024)
        .when().get(DataTransferController.PATH + "/product/{productId}/data")
        .then().statusCode(400);
  }

  @Test
  void givenNoYear_whenProductRequested_thenBadRequest() {
    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .pathParam("productId", TestDataIdentifiers.DataProduct.UUID_085E4B72.uuid())
        .queryParam("uid", CHE278)
        .when().get(DataTransferController.PATH + "/product/{productId}/data")
        .then().statusCode(400);
  }

}
