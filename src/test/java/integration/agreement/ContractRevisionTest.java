package integration.agreement;

import static integration.testutils.TestUserEnum.CONSUMER_BIO_SUISSE;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import ch.agridata.agreement.controller.ContractRevisionController;
import ch.agridata.agreement.dto.DataRequestDto;
import ch.agridata.agreement.dto.OtpChallengeDto;
import ch.agridata.agreement.dto.SignatureSlotCodeEnum;
import ch.agridata.agreement.dto.VerifyOtpRequestDto;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestUserEnum;
import io.quarkus.test.junit.QuarkusTest;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class ContractRevisionTest {

  private final Flyway flyway;

  @BeforeEach
  void setUp() {
    flyway.migrate();
  }

  @Test
  void givenExistingContractRevisionOfCurrentConsumer_whenGetById_thenReturnContractRevision() {
    DataRequestDto dataRequest = DataRequestTestFactory.createReadyForSigningDataRequest();
    UUID contractRevisionId = dataRequest.currentContractRevisionId();

    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE).given()
        .when()
        .get(ContractRevisionController.PATH + "/" + contractRevisionId)
        .then()
        .statusCode(200)
        .body("id", equalTo(contractRevisionId.toString()))
        .body("dataRequestId", equalTo(dataRequest.id().toString()));
  }

  @Test
  void givenContractRevisionOfAnotherConsumer_whenGetById_thenReturn404() {
    UUID contractRevisionId = DataRequestTestFactory.createContractRevisionAndReturnId();

    AuthTestUtils.requestAs(TestUserEnum.CONSUMER_IP_SUISSE).given()
        .when()
        .get(ContractRevisionController.PATH + "/" + contractRevisionId)
        .then()
        .statusCode(404);
  }

  @Test
  void givenNonExistingContractRevision_whenGetById_thenReturn404() {
    UUID unknownId = UUID.randomUUID();

    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE).given()
        .when()
        .get(ContractRevisionController.PATH + "/" + unknownId)
        .then()
        .statusCode(404);
  }

  @Test
  void givenValidRevision_whenInitiateChallenge_thenReturnOtpChallengeDto() {
    DataRequestDto dataRequest = DataRequestTestFactory.createReadyForSigningDataRequest();
    UUID revisionId = dataRequest.currentContractRevisionId();

    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE).given()
        .pathParam("id", revisionId)
        .pathParam("slotCode", SignatureSlotCodeEnum.DATA_CONSUMER_01.name())
        .when()
        .post(ContractRevisionController.PATH + "/{id}/signatures/{slotCode}/otp-challenges")
        .then()
        .statusCode(200)
        .body("challengeId", notNullValue())
        .body("maskedPhoneNumber", notNullValue())
        .body("retryAfterSeconds", equalTo(30));
  }

  @Test
  void givenActiveChallenge_whenVerifySignature_thenReturnUpdatedContractRevision() {
    DataRequestDto dataRequest = DataRequestTestFactory.createReadyForSigningDataRequest();
    UUID revisionId = dataRequest.currentContractRevisionId();

    OtpChallengeDto challenge = AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE).given()
        .pathParam("id", revisionId)
        .pathParam("slotCode", SignatureSlotCodeEnum.DATA_CONSUMER_01.name())
        .post(ContractRevisionController.PATH + "/{id}/signatures/{slotCode}/otp-challenges")
        .as(OtpChallengeDto.class);

    VerifyOtpRequestDto verifyRequest = new VerifyOtpRequestDto("123456");

    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE).given()
        .contentType("application/json")
        .pathParam("id", revisionId)
        .pathParam("slotCode", SignatureSlotCodeEnum.DATA_CONSUMER_01.name())
        .pathParam("challengeId", challenge.challengeId())
        .body(verifyRequest)
        .when()
        .post(ContractRevisionController.PATH + "/{id}/signatures/{slotCode}/otp-challenges/{challengeId}/verification")
        .then()
        .statusCode(200)
        .body("id", notNullValue())
        .body("id", org.hamcrest.Matchers.not(revisionId.toString()));
  }

  @Test
  void givenInvalidSlotId_whenInitiateChallenge_thenReturn400() {
    DataRequestDto dataRequest = DataRequestTestFactory.createReadyForSigningDataRequest();
    UUID revisionId = dataRequest.currentContractRevisionId();

    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE).given()
        .pathParam("id", revisionId)
        .pathParam("slotCode", SignatureSlotCodeEnum.DATA_PROVIDER_01.name())
        .when()
        .post(ContractRevisionController.PATH + "/{id}/signatures/{slotCode}/otp-challenges")
        .then()
        .statusCode(400);
  }
}
