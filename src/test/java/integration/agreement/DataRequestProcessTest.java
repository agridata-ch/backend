package integration.agreement;

import static integration.agreement.DataRequestTestFactory.createDataRequestAs;
import static integration.agreement.DataRequestTestFactory.getDataRequestDto;
import static integration.agreement.DataRequestTestFactory.getPartialDataRequestUpdateDtoBuilder;
import static integration.agreement.DataRequestTestFactory.setStatusAs;
import static integration.agreement.DataRequestTestFactory.signContractRevision;
import static integration.agreement.DataRequestTestFactory.updateDataRequestAs;
import static integration.agreement.DataRequestTestFactory.updateLogoAs;
import static integration.testutils.AuthTestUtils.requestAs;
import static integration.testutils.TestUserEnum.ADMIN;
import static integration.testutils.TestUserEnum.CONSUMER_BLV_1;
import static integration.testutils.TestUserEnum.CONSUMER_BLV_2;
import static integration.testutils.TestUserEnum.PRODUCER_B;
import static integration.testutils.TestUserEnum.PROVIDER_1;
import static integration.testutils.TestUserEnum.PROVIDER_2;
import static io.restassured.http.ContentType.JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import ch.agridata.agreement.controller.ConsentRequestController;
import ch.agridata.agreement.controller.ContractRevisionController;
import ch.agridata.agreement.dto.ConsentRequestCreatedDto;
import ch.agridata.agreement.dto.ContractRevisionDto;
import ch.agridata.agreement.dto.CreateConsentRequestDto;
import ch.agridata.agreement.dto.DataRequestDescriptionDto;
import ch.agridata.agreement.dto.DataRequestDto;
import ch.agridata.agreement.dto.DataRequestStateEnum;
import ch.agridata.agreement.dto.DataRequestUpdateDto;
import ch.agridata.agreement.dto.SealAttemptStateEnum;
import ch.agridata.agreement.dto.SignatureSlotCodeEnum;
import ch.agridata.agreement.dto.SignatureTypeEnum;
import ch.agridata.auditing.api.ActionEnum;
import ch.agridata.auditing.api.EntityTypeEnum;
import ch.agridata.product.controller.DataProductController;
import ch.agridata.product.dto.DataProductDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import integration.auditing.utils.AuditLogTestUtils;
import integration.testutils.TestDataIdentifiers.Uid;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.specification.RequestSpecification;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
@Slf4j
class DataRequestProcessTest {
  private final ObjectMapper mapper = new ObjectMapper();
  private final AuditLogTestUtils auditLogTestUtils;

  private static final String ADMIN_GLOBAL_ID = "test-admin-global-id";
  private static final String PDF_PATH = ContractRevisionController.PATH + "/{id}/pdf";
  private static final String SEALS_PATH = ContractRevisionController.PATH + "/{id}/seals";
  private static final String SEAL_STATUS_PATH = ContractRevisionController.PATH + "/{id}/seals/status";

  @Test
  @SneakyThrows
  void givenValidRequestFlow_whenDraftUpdatedAndSubmitted_thenAllStepsSucceed() throws JsonProcessingException {
    RequestSpecification consumer1 = requestAs(CONSUMER_BLV_1);
    // Load a valid product ID via API
    List<DataProductDto> products = consumer1
        .when().get(DataProductController.PATH)
        .then().statusCode(200)
        .extract().as(new TypeRef<>() {
        });

    UUID productId = products.stream()
        .filter(p -> p.dataSourceSystemCode().equals("AGIS"))
        .findFirst()
        .orElseThrow()
        .id();

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

    // Step 4: Set the status to IN_REVIEW with invalid data
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

    // Step 10: As Admin set status to be signed — creates the initial contract revision
    var revisionId1 = setStatusAs(dataRequestId, DataRequestStateEnum.TO_BE_SIGNED_BY_CONSUMER, ADMIN)
        .then()
        .statusCode(200)
        .body("stateCode", equalTo(DataRequestStateEnum.TO_BE_SIGNED_BY_CONSUMER.name()))
        .extract().as(DataRequestDto.class).currentContractRevisionId();

    // Step 11: Verify initial contract revision
    requestAs(CONSUMER_BLV_1).given()
        .when()
        .get(ContractRevisionController.PATH + "/" + revisionId1)
        .then()
        .statusCode(200)
        .body("dataConsumerName", equalTo("Bundesamt für Lebensmittelsicherheit und Veterinärwesen BLV"))
        .body("dataConsumerCity", equalTo("Bern"))
        .body("dataProviderName", equalTo("Bundesamt für Landwirtschaft (BLW)"))
        .body("dataProviderCity", equalTo("Liebefeld"))
        .body("consumerSignatures", empty());

    // Step 12: Verify PDF is available for download immediately after revision creation
    byte[] initialPdf = requestAs(CONSUMER_BLV_1)
        .pathParam("id", revisionId1)
        .when().get(PDF_PATH)
        .then()
        .statusCode(200)
        .contentType("application/pdf")
        .extract().asByteArray();
    assertThat(initialPdf).isNotEmpty();
    assertThat(new String(initialPdf, 0, 4)).isEqualTo("%PDF");

    // Step 13: As Consumer 1 sign Contract
    var revisionId2 = signContractRevision(revisionId1, CONSUMER_BLV_1, SignatureSlotCodeEnum.DATA_CONSUMER_01)
        .then()
        .statusCode(200)
        .body("id", notNullValue())
        .body("id", org.hamcrest.Matchers.not(revisionId1.toString()))
        .body(
            "consumerSignatures.last().name", equalTo(
                CONSUMER_BLV_1.getGivenName() + " " + CONSUMER_BLV_1.getFamilyName()
            )
        )
        .body("consumerSignatureType", equalTo(SignatureTypeEnum.COLLECTIVE_SIGNATURE.toString()))
        .extract().as(ContractRevisionDto.class).id();

    // Step 14: As Consumer 2 sign Contract
    var revisionId3 = signContractRevision(revisionId2, CONSUMER_BLV_2, SignatureSlotCodeEnum.DATA_CONSUMER_02)
        .then()
        .statusCode(200)
        .body("id", notNullValue())
        .body("id", org.hamcrest.Matchers.not(revisionId2.toString()))
        .body(
            "consumerSignatures.last().name", equalTo(
                CONSUMER_BLV_2.getGivenName() + " " + CONSUMER_BLV_2.getFamilyName()
            )
        )
        .extract().as(ContractRevisionDto.class).id();

    // Step 15: As Consumer set status to toBeSignedByProvider
    setStatusAs(dataRequestId, DataRequestStateEnum.TO_BE_SIGNED_BY_PROVIDER, CONSUMER_BLV_2)
        .then()
        .statusCode(200)
        .body("stateCode", equalTo(DataRequestStateEnum.TO_BE_SIGNED_BY_PROVIDER.name()));

    // Step 16: As Provider 1 sign Contract
    var revisionId4 = signContractRevision(
        revisionId3, PROVIDER_1,
        SignatureSlotCodeEnum.DATA_PROVIDER_01
    )
        .then()
        .statusCode(200)
        .body("id", notNullValue())
        .body("id", org.hamcrest.Matchers.not(revisionId3.toString()))
        .body(
            "providerSignatures.last().name", equalTo(
                PROVIDER_1.getGivenName() + " " + PROVIDER_1.getFamilyName()
            )
        )
        .body("providerSignatureType", equalTo(SignatureTypeEnum.COLLECTIVE_SIGNATURE.toString()))
        .extract().as(ContractRevisionDto.class).id();

    // Step 17: As Provider 2 sign Contract — captures the final revision ID for sealing
    UUID revisionId5 = signContractRevision(
        revisionId4, PROVIDER_2,
        SignatureSlotCodeEnum.DATA_PROVIDER_02
    )
        .then()
        .statusCode(200)
        .body("id", notNullValue())
        .body("id", org.hamcrest.Matchers.not(revisionId4.toString()))
        .body(
            "providerSignatures.last().name", equalTo(
                PROVIDER_2.getGivenName() + " " + PROVIDER_2.getFamilyName()
            )
        )
        .extract().as(ContractRevisionDto.class).id();

    // Step 18: As Provider set status to toBeActivated
    setStatusAs(dataRequestId, DataRequestStateEnum.TO_BE_ACTIVATED, PROVIDER_1)
        .then()
        .statusCode(200)
        .body("stateCode", equalTo(DataRequestStateEnum.TO_BE_ACTIVATED.name()));

    // Step 19: Verify seal status is NOT_STARTED before seal is initiated
    SealAttemptStateEnum statusBeforeSeal = requestAs(ADMIN)
        .pathParam("id", revisionId5)
        .when().get(SEAL_STATUS_PATH)
        .then().statusCode(200)
        .extract().as(SealAttemptStateEnum.class);
    assertThat(statusBeforeSeal).isEqualTo(SealAttemptStateEnum.NOT_STARTED);

    // Step 20: As Admin initiate the seal process
    requestAs(ADMIN)
        .pathParam("id", revisionId5)
        .queryParam("adminGlobalId", ADMIN_GLOBAL_ID)
        .when().post(SEALS_PATH)
        .then().statusCode(202);

    // Step 21: Poll seal status until completed
    SealAttemptStateEnum sealResult = requestAs(ADMIN)
        .pathParam("id", revisionId5)
        .queryParam("longPolling", true)
        .when().get(SEAL_STATUS_PATH)
        .then().statusCode(200)
        .extract().as(SealAttemptStateEnum.class);
    assertThat(sealResult).isEqualTo(SealAttemptStateEnum.COMPLETED);

    // Step 22: Verify sealed PDF has an embedded digital signature
    byte[] sealedPdf = requestAs(ADMIN)
        .pathParam("id", revisionId5)
        .when().get(PDF_PATH)
        .then().statusCode(200)
        .contentType("application/pdf")
        .extract().asByteArray();
    assertThat(sealedPdf).isNotEmpty();
    try (PDDocument doc = Loader.loadPDF(sealedPdf)) {
      assertThat(doc.getSignatureDictionaries()).isNotEmpty();
    }

    // Step 23: As Admin set status to active
    setStatusAs(dataRequestId, DataRequestStateEnum.ACTIVE, ADMIN)
        .then()
        .statusCode(200)
        .body("stateCode", equalTo(DataRequestStateEnum.ACTIVE.name()));

    // Step 24: As producer use consent request creation link
    var createDtos = PRODUCER_B.getCompanyUids().stream()
        .map(uid -> CreateConsentRequestDto.builder().dataRequestId(UUID.fromString(dataRequestId)).uid(uid.name())
            .build())
        .toList();
    List<ConsentRequestCreatedDto> createdConsentRequests = requestAs(PRODUCER_B)
        .body(mapper.writeValueAsString(createDtos))
        .contentType(JSON)
        .when().post(ConsentRequestController.PATH)
        .then().statusCode(201)
        .extract().as(new TypeRef<>() {
        });
    assertThat(createdConsentRequests).hasSize(2).extracting(ConsentRequestCreatedDto::dataProducerUid)
        .containsExactlyInAnyOrderElementsOf(PRODUCER_B.getCompanyUids().stream().map(Uid::name).toList());

    // Verify the last 11 audit log entries
    verifyAuditEntry(
        0, EntityTypeEnum.DATA_REQUEST, dataRequestId, ActionEnum.DATA_REQUEST_ACTIVATED,
        "Step 23: As Admin set status to active"
    );
    verifyAuditEntry(
        1, EntityTypeEnum.CONTRACT_REVISION, revisionId5, ActionEnum.CONTRACT_PDF_ELECTRONICALLY_SIGNED,
        "Step 20/21: Contract PDF electronically signed (seal)"
    );
    verifyAuditEntry(
        2, EntityTypeEnum.DATA_REQUEST, dataRequestId, ActionEnum.DATA_REQUEST_RELEASED_BY_PROVIDER,
        "Step 18: As Provider set status to toBeActivated"
    );
    verifyAuditEntry(
        3, EntityTypeEnum.CONTRACT_REVISION, revisionId4, ActionEnum.CONTRACT_SECOND_PROVIDER_SLOT_SIGNED,
        "Step 17: Provider 2 signed contract"
    );
    verifyAuditEntry(
        4, EntityTypeEnum.CONTRACT_REVISION, revisionId3, ActionEnum.CONTRACT_FIRST_PROVIDER_SLOT_SIGNED,
        "Step 16: Provider 1 signed contract"
    );
    verifyAuditEntry(
        5, EntityTypeEnum.DATA_REQUEST, dataRequestId, ActionEnum.DATA_REQUEST_RELEASED_BY_CONSUMER,
        "Step 15: Consumer set status to toBeSignedByProvider"
    );
    verifyAuditEntry(
        6, EntityTypeEnum.CONTRACT_REVISION, revisionId2, ActionEnum.CONTRACT_SECOND_CONSUMER_SLOT_SIGNED,
        "Step 14: Consumer 2 signed contract"
    );
    verifyAuditEntry(
        7, EntityTypeEnum.CONTRACT_REVISION, revisionId1, ActionEnum.CONTRACT_FIRST_CONSUMER_SLOT_SIGNED,
        "Step 13: Consumer 1 signed contract"
    );
    verifyAuditEntry(
        8, EntityTypeEnum.DATA_REQUEST, dataRequestId, ActionEnum.DATA_REQUEST_COLLECTIVE_SIGNATURE_SET_FOR_PROVIDER,
        "Step 10: Admin approved data request, collective signature set for provider"
    );
    verifyAuditEntry(
        9, EntityTypeEnum.DATA_REQUEST, dataRequestId, ActionEnum.DATA_REQUEST_COLLECTIVE_SIGNATURE_SET_FOR_CONSUMER,
        "Step 10: Admin approved data request, collective signature set for consumer"
    );
    verifyAuditEntry(
        10, EntityTypeEnum.DATA_REQUEST, dataRequestId, ActionEnum.DATA_REQUEST_APPROVED,
        "Step 10: Admin approved data request (TO_BE_SIGNED_BY_CONSUMER)"
    );
    verifyAuditEntry(
        11, EntityTypeEnum.DATA_REQUEST, dataRequestId, ActionEnum.DATA_REQUEST_SUBMITTED,
        "Step 6: Consumer submitted data request (IN_REVIEW)"
    );
    verifyAuditEntry(
        12, EntityTypeEnum.DATA_REQUEST, dataRequestId, ActionEnum.DATA_REQUEST_REJECTED,
        "Step 4: Consumer attempted to submit with invalid data"
    );
  }

  private void verifyAuditEntry(
      int offset, EntityTypeEnum expectedEntityType, Object expectedEntityId, ActionEnum expectedAction,
      String stepDescription
  ) {
    assertThat(auditLogTestUtils.getLatestAuditLogEntry(offset))
        .as(stepDescription)
        .satisfies(log -> {
          assertThat(log.getEntityTypeCode()).isEqualTo(expectedEntityType.name());
          assertThat(log.getEntityId()).isEqualTo(
              expectedEntityId instanceof String ? UUID.fromString((String) expectedEntityId) : expectedEntityId);
          assertThat(log.getActionCode()).isEqualTo(expectedAction.name());
        });
  }

}
