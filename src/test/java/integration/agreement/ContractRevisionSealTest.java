package integration.agreement;

import static integration.agreement.DataRequestTestFactory.createReadyForActivatingDataRequest;
import static integration.testutils.TestUserEnum.ADMIN;
import static integration.testutils.TestUserEnum.CONSUMER_BLV_1;
import static integration.testutils.TestUserEnum.CONSUMER_BLV_2;
import static integration.testutils.TestUserEnum.PROVIDER_1;
import static integration.testutils.TestUserEnum.PROVIDER_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

import ch.agridata.agreement.controller.ContractRevisionController;
import ch.agridata.agreement.dto.DataRequestDto;
import ch.agridata.agreement.dto.SealAttemptStateEnum;
import com.github.tomakehurst.wiremock.client.WireMock;
import integration.testutils.AuthTestUtils;
import io.quarkiverse.wiremock.devservice.ConnectWireMock;
import io.quarkus.test.junit.QuarkusTest;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for the seal endpoints.
 *
 * @CommentLastReviewed 2026-04-14
 */
@QuarkusTest
@ConnectWireMock
@RequiredArgsConstructor
class ContractRevisionSealTest {

  WireMock wireMock;

  private static final String ADMIN_GLOBAL_ID = "test-admin-global-id";
  private static final String SEALS_PATH = ContractRevisionController.PATH + "/{id}/seals";
  private static final String SEAL_STATUS_PATH = ContractRevisionController.PATH + "/{id}/seals/status";

  @Test
  void givenExistingContractRevision_whenSeal_thenReturn202() {
    UUID revisionId = createReadyForActivatingDataRequest(CONSUMER_BLV_1, CONSUMER_BLV_2, PROVIDER_1, PROVIDER_2)
        .then().extract().as(DataRequestDto.class).currentContractRevisionId();

    AuthTestUtils.requestAs(ADMIN)
        .pathParam("id", revisionId)
        .queryParam("adminGlobalId", ADMIN_GLOBAL_ID)
        .when().post(SEALS_PATH)
        .then().statusCode(202);
  }

  @Test
  void givenNonExistingContractRevision_whenSeal_thenReturn404() {
    AuthTestUtils.requestAs(ADMIN)
        .pathParam("id", UUID.randomUUID())
        .queryParam("adminGlobalId", ADMIN_GLOBAL_ID)
        .when().post(SEALS_PATH)
        .then().statusCode(404);
  }

  @Test
  @SneakyThrows
  void givenSealInProgress_whenSealAgain_thenReturn409() {
    wireMock.register(WireMock.post(WireMock.urlEqualTo("/bit-signature/secure/v1/checkSignState"))
        .willReturn(WireMock.aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"status\":\"OK\",\"logId\":\"test\",\"signState\":\"SIGN_RUNNING\"}")
            .withFixedDelay(5_000)));

    UUID revisionId = createReadyForActivatingDataRequest(CONSUMER_BLV_1, CONSUMER_BLV_2, PROVIDER_1, PROVIDER_2)
        .then().extract().as(DataRequestDto.class).currentContractRevisionId();

    AuthTestUtils.requestAs(ADMIN)
        .pathParam("id", revisionId)
        .queryParam("adminGlobalId", ADMIN_GLOBAL_ID)
        .when().post(SEALS_PATH)
        .then().statusCode(202);

    AuthTestUtils.requestAs(ADMIN)
        .pathParam("id", revisionId)
        .queryParam("adminGlobalId", ADMIN_GLOBAL_ID)
        .when().post(SEALS_PATH)
        .then().statusCode(400)
        .body("debugMessage", equalTo(String.format("seal process is already running for contractRevisionId=%s", revisionId)));
  }

  @Test
  void givenNoSealStarted_whenGetStatus_thenReturnNotStarted() {
    UUID revisionId = DataRequestTestFactory.createContractRevisionAndReturnId();

    SealAttemptStateEnum status = AuthTestUtils.requestAs(ADMIN)
        .pathParam("id", revisionId)
        .when().get(SEAL_STATUS_PATH)
        .then().statusCode(200)
        .extract().as(SealAttemptStateEnum.class);

    assertThat(status).isEqualTo(SealAttemptStateEnum.NOT_STARTED);
  }

  @Test
  void givenNonExistingContractRevision_whenGetStatus_thenReturn404() {
    AuthTestUtils.requestAs(ADMIN)
        .pathParam("id", UUID.randomUUID())
        .when().get(SEAL_STATUS_PATH)
        .then().statusCode(404);
  }

  @Test
  void givenBitSigningServiceFails_whenGetStatusWithLongPolling_thenReturnFailed() {
    wireMock.register(WireMock.post(WireMock.urlEqualTo("/bit-signature/secure/v1/initSign"))
        .willReturn(WireMock.aResponse()
            .withStatus(500)));

    UUID revisionId = createReadyForActivatingDataRequest(CONSUMER_BLV_1, CONSUMER_BLV_2, PROVIDER_1, PROVIDER_2)
        .then().extract().as(DataRequestDto.class).currentContractRevisionId();

    AuthTestUtils.requestAs(ADMIN)
        .pathParam("id", revisionId)
        .queryParam("adminGlobalId", ADMIN_GLOBAL_ID)
        .when().post(SEALS_PATH)
        .then().statusCode(202);

    SealAttemptStateEnum status = AuthTestUtils.requestAs(ADMIN)
        .pathParam("id", revisionId)
        .queryParam("longPolling", true)
        .when().get(SEAL_STATUS_PATH)
        .then().statusCode(200)
        .extract().as(SealAttemptStateEnum.class);

    assertThat(status).isEqualTo(SealAttemptStateEnum.FAILED);
  }

  @Test
  void givenSealStarted_whenGetStatusWithLongPolling_thenReturnCompleted() {
    UUID revisionId = createReadyForActivatingDataRequest(CONSUMER_BLV_1, CONSUMER_BLV_2, PROVIDER_1, PROVIDER_2)
        .then().extract().as(DataRequestDto.class).currentContractRevisionId();

    AuthTestUtils.requestAs(ADMIN)
        .pathParam("id", revisionId)
        .queryParam("adminGlobalId", ADMIN_GLOBAL_ID)
        .when().post(SEALS_PATH)
        .then().statusCode(202);

    SealAttemptStateEnum status = AuthTestUtils.requestAs(ADMIN)
        .pathParam("id", revisionId)
        .queryParam("longPolling", true)
        .when().get(SEAL_STATUS_PATH)
        .then().statusCode(200)
        .extract().as(SealAttemptStateEnum.class);

    assertThat(status).isEqualTo(SealAttemptStateEnum.COMPLETED);
  }
}
