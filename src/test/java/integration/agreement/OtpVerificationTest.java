package integration.agreement;

import static integration.agreement.DataRequestTestFactory.createReadyForSigningByConsumerDataRequestFor;
import static integration.agreement.DataRequestTestFactory.requestOtpChallengeAs;
import static integration.agreement.DataRequestTestFactory.verifyOtpChallenge;
import static integration.testutils.TestUserEnum.CONSUMER_BLV_1;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import ch.agridata.agreement.dto.DataRequestDto;
import ch.agridata.agreement.dto.OtpChallengeDto;
import ch.agridata.agreement.dto.SignatureSlotCodeEnum;
import io.quarkus.test.junit.QuarkusTest;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for the OTP verification flow during contract-revision signing.
 *
 * @CommentLastReviewed 2026-05-08
 */
@QuarkusTest
@RequiredArgsConstructor
class OtpVerificationTest {

  private final OtpTestHelper otpTestHelper;

  @Test
  void givenThreeWrongCodes_whenVerify_thenThirdAttemptReturnsOtpLockedAndCounterPersists() {
    DataRequestDto dataRequest = createReadyForSigningByConsumerDataRequestFor(CONSUMER_BLV_1);
    UUID revisionId = dataRequest.currentContractRevisionId();

    OtpChallengeDto challenge =
        requestOtpChallengeAs(revisionId.toString(), SignatureSlotCodeEnum.DATA_CONSUMER_01, CONSUMER_BLV_1).as(OtpChallengeDto.class);

    verifyOtpChallenge(revisionId.toString(), SignatureSlotCodeEnum.DATA_CONSUMER_01, challenge.challengeId(), "000001", CONSUMER_BLV_1)
        .then()
        .statusCode(400)
        .body("type", equalTo("OTP_INVALID"));

    verifyOtpChallenge(revisionId.toString(), SignatureSlotCodeEnum.DATA_CONSUMER_01, challenge.challengeId(), "000002", CONSUMER_BLV_1)
        .then()
        .statusCode(400)
        .body("type", equalTo("OTP_INVALID"));

    verifyOtpChallenge(revisionId.toString(), SignatureSlotCodeEnum.DATA_CONSUMER_01, challenge.challengeId(), "000003", CONSUMER_BLV_1)
        .then()
        .statusCode(400)
        .body("type", equalTo("OTP_LOCKED"));

    // The persisted attempt counter is at the limit, so any further verify call (even with the
    // correct code, which we now seed) responds with OTP_LOCKED — proving the counter survived
    // each rolled-back signing transaction.
    otpTestHelper.overrideOtpHashForCode(challenge.challengeId(), OtpTestHelper.FIXED_OTP_CODE);
    verifyOtpChallenge(revisionId.toString(), SignatureSlotCodeEnum.DATA_CONSUMER_01, challenge.challengeId(),
        OtpTestHelper.FIXED_OTP_CODE, CONSUMER_BLV_1)
        .then()
        .statusCode(400)
        .body("type", equalTo("OTP_LOCKED"));
  }

  @Test
  void givenCorrectCode_whenVerify_thenAccepted() {
    DataRequestDto dataRequest = createReadyForSigningByConsumerDataRequestFor(CONSUMER_BLV_1);
    UUID revisionId = dataRequest.currentContractRevisionId();

    OtpChallengeDto challenge =
        requestOtpChallengeAs(revisionId.toString(), SignatureSlotCodeEnum.DATA_CONSUMER_01, CONSUMER_BLV_1).as(OtpChallengeDto.class);
    otpTestHelper.overrideOtpHashForCode(challenge.challengeId(), OtpTestHelper.FIXED_OTP_CODE);

    verifyOtpChallenge(revisionId.toString(), SignatureSlotCodeEnum.DATA_CONSUMER_01, challenge.challengeId(),
        OtpTestHelper.FIXED_OTP_CODE, CONSUMER_BLV_1)
        .then()
        .statusCode(200)
        .body("id", notNullValue());
  }

  @Test
  void givenConsumedChallenge_whenVerify_thenOtpInvalid() {
    DataRequestDto dataRequest = createReadyForSigningByConsumerDataRequestFor(CONSUMER_BLV_1);
    UUID revisionId = dataRequest.currentContractRevisionId();

    OtpChallengeDto challenge =
        requestOtpChallengeAs(revisionId.toString(), SignatureSlotCodeEnum.DATA_CONSUMER_01, CONSUMER_BLV_1).as(OtpChallengeDto.class);
    otpTestHelper.overrideOtpHashForCode(challenge.challengeId(), OtpTestHelper.FIXED_OTP_CODE);
    otpTestHelper.consumeChallenge(challenge.challengeId());

    verifyOtpChallenge(revisionId.toString(), SignatureSlotCodeEnum.DATA_CONSUMER_01, challenge.challengeId(),
        OtpTestHelper.FIXED_OTP_CODE, CONSUMER_BLV_1)
        .then()
        .statusCode(400)
        .body("type", equalTo("OTP_INVALID"));
  }

  @Test
  void givenExpiredChallenge_whenVerify_thenOtpExpired() {
    DataRequestDto dataRequest = createReadyForSigningByConsumerDataRequestFor(CONSUMER_BLV_1);
    UUID revisionId = dataRequest.currentContractRevisionId();

    OtpChallengeDto challenge =
        requestOtpChallengeAs(revisionId.toString(), SignatureSlotCodeEnum.DATA_CONSUMER_01, CONSUMER_BLV_1).as(OtpChallengeDto.class);
    otpTestHelper.overrideOtpHashForCode(challenge.challengeId(), OtpTestHelper.FIXED_OTP_CODE);
    otpTestHelper.expireChallenge(challenge.challengeId());

    verifyOtpChallenge(revisionId.toString(), SignatureSlotCodeEnum.DATA_CONSUMER_01, challenge.challengeId(),
        OtpTestHelper.FIXED_OTP_CODE, CONSUMER_BLV_1)
        .then()
        .statusCode(400)
        .body("type", equalTo("OTP_EXPIRED"));
  }

  @Test
  void givenSecondChallengeWithinCooldown_whenInitiate_thenOtpResendCooldown() {
    DataRequestDto dataRequest = createReadyForSigningByConsumerDataRequestFor(CONSUMER_BLV_1);
    UUID revisionId = dataRequest.currentContractRevisionId();

    OtpChallengeDto firstChallenge =
        requestOtpChallengeAs(revisionId.toString(), SignatureSlotCodeEnum.DATA_CONSUMER_01, CONSUMER_BLV_1)
            .then().statusCode(200).extract().as(OtpChallengeDto.class);

    requestOtpChallengeAs(revisionId.toString(), SignatureSlotCodeEnum.DATA_CONSUMER_01, CONSUMER_BLV_1)
        .then()
        .statusCode(400)
        .body("type", equalTo("OTP_RESEND_COOLDOWN"));

    // Backdate the first challenge so it sits outside the cooldown window — the next request must
    // succeed, proving the cooldown actually ends rather than blocking forever.
    otpTestHelper.backdateCreatedAtBeyondCooldown(firstChallenge.challengeId());

    requestOtpChallengeAs(revisionId.toString(), SignatureSlotCodeEnum.DATA_CONSUMER_01, CONSUMER_BLV_1)
        .then()
        .statusCode(200);
  }
}
