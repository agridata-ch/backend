package integration.agreement;

import static integration.agreement.DataRequestTestFactory.requestOtpChallengeAs;
import static integration.agreement.DataRequestTestFactory.signContractRevisionAs;
import static integration.testutils.TestUserEnum.CONSUMER_BIO_SUISSE;
import static integration.testutils.TestUserEnum.CONSUMER_BLV_1;
import static integration.testutils.TestUserEnum.CONSUMER_BLV_2;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import ch.agridata.agreement.controller.ContractRevisionController;
import ch.agridata.agreement.dto.DataRequestDto;
import ch.agridata.agreement.dto.OtpChallengeDto;
import ch.agridata.agreement.dto.SignatureSlotCodeEnum;
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
    DataRequestDto dataRequest = DataRequestTestFactory.createReadyForSigningDataRequestFor(CONSUMER_BIO_SUISSE);
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
    DataRequestDto dataRequest = DataRequestTestFactory.createReadyForSigningDataRequestFor(CONSUMER_BIO_SUISSE);
    UUID revisionId = dataRequest.currentContractRevisionId();

    requestOtpChallengeAs(revisionId.toString(), SignatureSlotCodeEnum.DATA_CONSUMER_01, CONSUMER_BIO_SUISSE)
        .then()
        .statusCode(200)
        .body("challengeId", notNullValue())
        .body("maskedPhoneNumber", notNullValue())
        .body("retryAfterSeconds", equalTo(30));
  }

  @Test
  void givenActiveChallenge_whenVerifyTwoSignatures_thenReturnUpdatedContractRevision() {
    DataRequestDto dataRequest = DataRequestTestFactory.createReadyForSigningDataRequestFor(CONSUMER_BLV_1);
    UUID revisionId1 = dataRequest.currentContractRevisionId();

    var challenge1 = requestOtpChallengeAs(revisionId1.toString(), SignatureSlotCodeEnum.DATA_CONSUMER_01, CONSUMER_BLV_1)
        .as(OtpChallengeDto.class);

    var response = signContractRevisionAs(
        revisionId1.toString(),
        SignatureSlotCodeEnum.DATA_CONSUMER_01,
        challenge1.challengeId(),
        "123456",
        CONSUMER_BLV_1
    )
        .then()
        .statusCode(200)
        .body("id", notNullValue())
        .body("id", org.hamcrest.Matchers.not(revisionId1.toString()))
        .body("consumerSignatures.last().name", equalTo(
            CONSUMER_BLV_1.getGivenName() + " " + CONSUMER_BLV_1.getFamilyName()
        ));

    var revisionId2 = response.extract().path("id");

    var challenge2 = requestOtpChallengeAs(revisionId2.toString(), SignatureSlotCodeEnum.DATA_CONSUMER_02, CONSUMER_BLV_2)
        .as(OtpChallengeDto.class);

    signContractRevisionAs(
        revisionId2.toString(),
        SignatureSlotCodeEnum.DATA_CONSUMER_02,
        challenge2.challengeId(),
        "123456",
        CONSUMER_BLV_2
    )
        .then()
        .statusCode(200)
        .body("id", notNullValue())
        .body("id", org.hamcrest.Matchers.not(revisionId2.toString()))
        .body("consumerSignatures.last().name", equalTo(
            CONSUMER_BLV_2.getGivenName() + " " + CONSUMER_BLV_2.getFamilyName()
        ));
  }

  @Test
  void givenInvalidSlotId_whenInitiateChallenge_thenReturn400() {
    DataRequestDto dataRequest = DataRequestTestFactory.createReadyForSigningDataRequestFor(CONSUMER_BIO_SUISSE);
    UUID revisionId = dataRequest.currentContractRevisionId();

    requestOtpChallengeAs(revisionId.toString(), SignatureSlotCodeEnum.DATA_PROVIDER_01, CONSUMER_BIO_SUISSE)
        .then()
        .statusCode(400);
  }
}
