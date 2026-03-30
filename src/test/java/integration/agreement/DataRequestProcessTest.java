package integration.agreement;

import static integration.agreement.DataRequestTestFactory.createDataRequestAs;
import static integration.agreement.DataRequestTestFactory.getDataRequestDto;
import static integration.agreement.DataRequestTestFactory.getPartialDataRequestUpdateDtoBuilder;
import static integration.agreement.DataRequestTestFactory.requestOtpChallengeAs;
import static integration.agreement.DataRequestTestFactory.setStatusAs;
import static integration.agreement.DataRequestTestFactory.signContractRevisionAs;
import static integration.agreement.DataRequestTestFactory.updateDataRequestAs;
import static integration.agreement.DataRequestTestFactory.updateLogoAs;
import static integration.testutils.TestDataIdentifiers.Uid;
import static integration.testutils.TestUserEnum.ADMIN;
import static integration.testutils.TestUserEnum.CONSUMER_BLV_1;
import static integration.testutils.TestUserEnum.CONSUMER_BLV_2;
import static integration.testutils.TestUserEnum.PRODUCER_B;
import static io.restassured.http.ContentType.JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import ch.agridata.agreement.controller.ConsentRequestController;
import ch.agridata.agreement.dto.ConsentRequestCreatedDto;
import ch.agridata.agreement.dto.CreateConsentRequestDto;
import ch.agridata.agreement.dto.DataRequestDescriptionDto;
import ch.agridata.agreement.dto.DataRequestStateEnum;
import ch.agridata.agreement.dto.DataRequestUpdateDto;
import ch.agridata.agreement.dto.OtpChallengeDto;
import ch.agridata.agreement.dto.SignatureSlotCodeEnum;
import ch.agridata.product.controller.DataProductController;
import ch.agridata.product.dto.DataProductDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import integration.testutils.AuthTestUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.specification.RequestSpecification;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
@Slf4j
class DataRequestProcessTest {
  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void givenValidRequestFlow_whenDraftUpdatedAndSubmitted_thenAllStepsSucceed() throws JsonProcessingException {
    RequestSpecification consumer1 = AuthTestUtils.requestAs(CONSUMER_BLV_1);
    // Load a valid product ID via API
    List<DataProductDto> products = consumer1
        .when().get(DataProductController.PATH)
        .then().statusCode(200)
        .extract().as(new TypeRef<>() {
        });

    UUID productId = products.getFirst().id();

    // Step 1: Create draft
    String dataRequestId = createDataRequestAs(CONSUMER_BLV_1).then()
        .statusCode(201)
        .body("stateCode", equalTo(DataRequestStateEnum.DRAFT.name()))
        .extract().path("id");

    // Step 2: Update draft
    DataRequestUpdateDto updateDto = getPartialDataRequestUpdateDtoBuilder()
        .description(DataRequestDescriptionDto.builder().de("test de").build())
        .products(List.of(productId))
        .build();
    updateDataRequestAs(dataRequestId, updateDto, CONSUMER_BLV_1)
        .then()
        .statusCode(200)
        .body("description.de", equalTo(updateDto.description().de()))
        .body("products", hasItem(productId.toString()));

    // Step 3: Add logo
    updateLogoAs(dataRequestId, "test-logo-95kB.png", CONSUMER_BLV_1)
        .then()
        .statusCode(204);

    // Step 4: Set status to IN_REVIEW with invalid data
    setStatusAs(dataRequestId, DataRequestStateEnum.IN_REVIEW, CONSUMER_BLV_1)
        .then()
        .statusCode(400)
        .body("requestId", not(nullValue()));

    // Step 5: Update request with complete data
    updateDataRequestAs(dataRequestId, getDataRequestDto().build(), CONSUMER_BLV_1)
        .then()
        .statusCode(200);

    // Step 6: Set status to in review
    setStatusAs(dataRequestId, DataRequestStateEnum.IN_REVIEW, CONSUMER_BLV_1)
        .then()
        .statusCode(200)
        .body("stateCode", equalTo(DataRequestStateEnum.IN_REVIEW.name()));

    // Step 7: Try to update again (should fail)
    updateDataRequestAs(dataRequestId, getDataRequestDto().build(), CONSUMER_BLV_1)
        .then()
        .statusCode(400);

    // Step 8: As Admin set status to DRAFT
    setStatusAs(dataRequestId, DataRequestStateEnum.DRAFT, ADMIN)
        .then()
        .statusCode(200)
        .body("stateCode", equalTo(DataRequestStateEnum.DRAFT.name()));

    // Step 9: Set status to in review after fixing the request
    setStatusAs(dataRequestId, DataRequestStateEnum.IN_REVIEW, CONSUMER_BLV_1)
        .then()
        .statusCode(200)
        .body("stateCode", equalTo(DataRequestStateEnum.IN_REVIEW.name()));

    // Step 10: As Admin set status to to be signed
    var revisionId1 = setStatusAs(dataRequestId, DataRequestStateEnum.TO_BE_SIGNED_BY_CONSUMER, ADMIN)
        .then()
        .statusCode(200)
        .body("stateCode", equalTo(DataRequestStateEnum.TO_BE_SIGNED_BY_CONSUMER.name()))
        .extract().path("currentContractRevisionId");

    // Step 11: As Consumer 1 request an otp challenge
    OtpChallengeDto challenge1 = requestOtpChallengeAs(revisionId1.toString(), SignatureSlotCodeEnum.DATA_CONSUMER_01, CONSUMER_BLV_1)
        .as(OtpChallengeDto.class);

    // Step 12: As Consumer 1 sign the contract revision
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

    // Step 13: As Consumer 2 request an otp challenge

    OtpChallengeDto challenge2 = requestOtpChallengeAs(revisionId2.toString(), SignatureSlotCodeEnum.DATA_CONSUMER_02, CONSUMER_BLV_2)
        .as(OtpChallengeDto.class);

    // Step 14: As Consumer 2 sign the contract revision

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

    // Step 15: As Consumer set status to toBeSignedByProvider
    setStatusAs(dataRequestId, DataRequestStateEnum.TO_BE_SIGNED_BY_PROVIDER, CONSUMER_BLV_2)
        .then()
        .statusCode(200)
        .body("stateCode", equalTo(DataRequestStateEnum.TO_BE_SIGNED_BY_PROVIDER.name()));

    // Step 16: As Admin set status to active
    setStatusAs(dataRequestId, DataRequestStateEnum.ACTIVE, ADMIN)
        .then()
        .statusCode(200)
        .body("stateCode", equalTo(DataRequestStateEnum.ACTIVE.name()));

    // Step 16: As producer use consent request creation link
    var createDtos = PRODUCER_B.getCompanyUids().stream()
        .map(uid -> CreateConsentRequestDto.builder().dataRequestId(UUID.fromString(dataRequestId)).uid(uid.name())
            .build())
        .toList();
    List<ConsentRequestCreatedDto> createdConsentRequests = AuthTestUtils.requestAs(PRODUCER_B)
        .body(mapper.writeValueAsString(createDtos))
        .contentType(JSON)
        .when().post(ConsentRequestController.PATH)
        .then().statusCode(201)
        .extract().as(new TypeRef<>() {
        });
    assertThat(createdConsentRequests).hasSize(2).extracting(ConsentRequestCreatedDto::dataProducerUid)
        .containsExactlyInAnyOrderElementsOf(PRODUCER_B.getCompanyUids().stream().map(Uid::name).toList());
  }

}
