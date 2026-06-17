package integration.agreement;

import static ch.agridata.agreement.dto.DataRequestStateEnum.IN_REVIEW;
import static ch.agridata.agreement.dto.DataRequestStateEnum.TO_BE_RELEASED_BY_CONSUMER;
import static ch.agridata.agreement.dto.DataRequestStateEnum.TO_BE_RELEASED_BY_PROVIDER;
import static ch.agridata.auditing.api.ActionEnum.DATA_REQUEST_SUBMITTED;
import static ch.agridata.auditing.api.EntityTypeEnum.DATA_REQUEST;
import static integration.agreement.DataRequestTestFactory.createDataRequest;
import static integration.agreement.DataRequestTestFactory.createDataRequestAs;
import static integration.agreement.DataRequestTestFactory.createReadyForActivatingDataRequest;
import static integration.agreement.DataRequestTestFactory.createReadyForSigningByConsumerDataRequestFor;
import static integration.agreement.DataRequestTestFactory.createReadyForSigningByProviderDataRequest;
import static integration.agreement.DataRequestTestFactory.getDataRequestDtoBuilder;
import static integration.agreement.DataRequestTestFactory.getPartialDataRequestUpdateDtoBuilder;
import static integration.agreement.DataRequestTestFactory.setSignatureType;
import static integration.agreement.DataRequestTestFactory.setStatusAs;
import static integration.agreement.DataRequestTestFactory.signContractRevision;
import static integration.agreement.DataRequestTestFactory.updateDataRequest;
import static integration.agreement.DataRequestTestFactory.updateValidRedirectUriRegex;
import static integration.testutils.TestDataConstants.UID_BIO_SUISSE_WITHOUT_PREFIX;
import static integration.testutils.TestDataIdentifiers.DataProduct.UUID_147E8C40;
import static integration.testutils.TestDataIdentifiers.DataProduct.UUID_46F8A883;
import static integration.testutils.TestDataIdentifiers.DataProduct.UUID_6319423C;
import static integration.testutils.TestUserEnum.ADMIN;
import static integration.testutils.TestUserEnum.CONSUMER_BIO_SUISSE;
import static integration.testutils.TestUserEnum.CONSUMER_BLV_1;
import static integration.testutils.TestUserEnum.CONSUMER_BLV_2;
import static integration.testutils.TestUserEnum.CONSUMER_IP_SUISSE;
import static integration.testutils.TestUserEnum.PROVIDER_1;
import static integration.testutils.TestUserEnum.PROVIDER_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

import ch.agridata.agreement.controller.DataRequestController;
import ch.agridata.agreement.dto.DataRequestAdvantageDto;
import ch.agridata.agreement.dto.DataRequestDescriptionDto;
import ch.agridata.agreement.dto.DataRequestDto;
import ch.agridata.agreement.dto.DataRequestPurposeDto;
import ch.agridata.agreement.dto.DataRequestStateEnum;
import ch.agridata.agreement.dto.DataRequestTitleDto;
import ch.agridata.agreement.dto.DataRequestUpdateDto;
import ch.agridata.agreement.dto.SignatureSlotCodeEnum;
import ch.agridata.agreement.dto.SignatureTypeEnum;
import ch.agridata.agreement.persistence.DataRequestDataProductEntity;
import ch.agridata.agreement.persistence.DataRequestRepository;
import integration.auditing.utils.AuditLogTestUtils;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestDataIdentifiers;
import integration.testutils.TestDataIdentifiers.DataRequest;
import integration.testutils.TestDataLoader;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class DataRequestTest {
  private static final UUID NONEXISTENT_PRODUCT_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

  private final DataRequestRepository dataRequestRepository;
  private final AuditLogTestUtils auditLogTestUtils;

  @Test
  void givenConsumer_whenGetDataRequests_thenOnlyConsumerDataRequestsReturned() {
    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE).when().get(DataRequestController.PATH_V1).then()
        .statusCode(200)
        .body("size()", equalTo(3));
  }

  @Test
  void givenAdmin_whenGetDataRequests_thenOnlyNonDraftDataRequestsReturned() {
    createDataRequest().then()
        .statusCode(201).extract().path("id");
    AuthTestUtils.requestAs(ADMIN).when().get(DataRequestController.PATH_V1).then()
        .statusCode(200)
        .body("size()", equalTo(8));
  }

  @Test
  void givenProvider_whenGetActiveDataRequests_thenOnlyActiveRequestsForThatProviderAreReturned() {
    createDataRequest().then()
        .statusCode(201).extract().path("id");
    AuthTestUtils.requestAs(PROVIDER_1).when().get(DataRequestController.PATH_V1).then()
        .statusCode(200)
        .body("size()", equalTo(5));
  }

  @Test
  void givenExistingDataRequestWithDifferentUID_whenGetDataRequest_thenReturnNotFound() {
    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE).when()
        .get(DataRequestController.PATH_V1 + "/" + DataRequest.IP_SUISSE_01)
        .then()
        .statusCode(404);
  }

  @Test
  void givenExistingDataRequestAndAdmin_whenGetDataRequest_thenReturnFound() {
    AuthTestUtils.requestAs(ADMIN).when()
        .get(DataRequestController.PATH_V1 + "/" + DataRequest.IP_SUISSE_01)
        .then()
        .statusCode(200);
  }

  @Test
  void givenExistingDataRequestAndProvider_whenGetDataRequest_thenReturnFound() {
    AuthTestUtils.requestAs(PROVIDER_1).when()
        .get(DataRequestController.PATH_V1 + "/" + DataRequest.IP_SUISSE_01)
        .then()
        .statusCode(200);
  }

  @Test
  void givenToBeActivatedDataRequestAndProvider_whenGetDataRequest_thenReturnRequest() {
    DataRequestDto existingDataRequest = createReadyForActivatingDataRequest(CONSUMER_BLV_1, CONSUMER_BLV_2, PROVIDER_1, PROVIDER_2)
        .then().extract().as(DataRequestDto.class);

    AuthTestUtils.requestAs(PROVIDER_1).when()
        .get(DataRequestController.PATH_V1 + "/" + existingDataRequest.id().toString())
        .then()
        .statusCode(200)
        .extract().as(new TypeRef<>() {
        });
  }

  @Test
  void givenExistingDraftDataRequestAndConsumer_whenDeleteDataRequest_thenReturnValidResponse() {
    String id = createDataRequest().then()
        .statusCode(201).extract().path("id");
    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE).when()
        .delete(DataRequestController.PATH_V1 + "/" + id)
        .then()
        .statusCode(204);
  }

  @Test
  void givenExistingActiveDataRequestAndConsumer_whenDeleteDataRequest_thenReturnBadRequest() {
    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE).when()
        .delete(DataRequestController.PATH_V1 + "/" + DataRequest.BIO_SUISSE_01)
        .then()
        .statusCode(400);
  }

  @Test
  void givenExistingDraftDataRequestButDifferentConsumer_whenDeleteDataRequest_thenReturnNotFound() {
    String id = createDataRequest().then()
        .statusCode(201).extract().path("id");
    AuthTestUtils.requestAs(CONSUMER_IP_SUISSE).when()
        .delete(DataRequestController.PATH_V1 + "/" + id)
        .then()
        .statusCode(404);
  }

  @Test
  void givenNonexistentDraftDataRequestAndConsumer_whenDeleteDataRequest_thenReturnNotFound() {
    String nonexistentId = "00000000-0000-0000-0000-000000000001";
    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE).when()
        .delete(DataRequestController.PATH_V1 + "/" + nonexistentId)
        .then()
        .statusCode(404);
  }

  @Test
  void givenExistingDraftDataRequestAndAdmin_whenGetDataRequest_thenReturnNotFound() {
    AuthTestUtils.requestAs(ADMIN).when()
        .get(DataRequestController.PATH_V1 + "/" + DataRequest.BIO_SUISSE_DRAFT)
        .then()
        .statusCode(404);
  }

  @Test
  void givenExistingDraftDataRequestAndProvider_whenGetDataRequest_thenReturnNotFound() {
    String id = createDataRequest().then()
        .statusCode(201).extract().path("id");
    AuthTestUtils.requestAs(PROVIDER_1).when()
        .get(DataRequestController.PATH_V1 + "/" + id)
        .then()
        .statusCode(404);
  }

  @Test
  void givenExistingId_whenGetDataRequest_thenReturnRequest() {

    DataRequestDto dataRequestDto = AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE).when()
        .get(DataRequestController.PATH_V1 + "/" + DataRequest.BIO_SUISSE_01)
        .then()
        .statusCode(200)
        .extract().as(new TypeRef<>() {
        });

    var expectedDataRequest = TestDataLoader.of(dataRequestRepository).load(DataRequest.BIO_SUISSE_01);

    assertThat(dataRequestDto).usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(DataRequestDto.builder()
        .id(expectedDataRequest.getId())
        .title(DataRequestTitleDto.builder()
            .de(expectedDataRequest.getTitle().de())
            .fr(expectedDataRequest.getTitle().fr())
            .it(expectedDataRequest.getTitle().it()).build())
        .description(DataRequestDescriptionDto.builder()
            .de(expectedDataRequest.getDescription().de())
            .fr(expectedDataRequest.getDescription().fr())
            .it(expectedDataRequest.getDescription().it()).build())
        .purpose(DataRequestPurposeDto.builder()
            .de(expectedDataRequest.getPurpose().de())
            .fr(expectedDataRequest.getPurpose().fr())
            .it(expectedDataRequest.getPurpose().it()).build())
        .stateCode(DataRequestStateEnum.valueOf(expectedDataRequest.getStateCode().toString()))
        .products(expectedDataRequest.getDataProducts().stream()
            .map(DataRequestDataProductEntity::getDataProductId)
            .toList())
        .targetGroup(expectedDataRequest.getTargetGroup())
        .advantages(List.of(
            DataRequestAdvantageDto.builder()
                .de(expectedDataRequest.getAdvantages().getFirst().de())
                .fr(expectedDataRequest.getAdvantages().getFirst().fr())
                .it(expectedDataRequest.getAdvantages().getFirst().it())
                .build(),
            DataRequestAdvantageDto.builder()
                .de(expectedDataRequest.getAdvantages().get(1).de())
                .fr(expectedDataRequest.getAdvantages().get(1).fr())
                .it(expectedDataRequest.getAdvantages().get(1).it())
                .build()
        ))
        .build());
  }

  @Test
  void givenValidDraft_whenPost_thenReturnCreated() {
    createDataRequest()
        .then()
        .statusCode(201)
        .body("stateCode", equalTo(DataRequestStateEnum.DRAFT.toString()))
        .body("dataConsumerUid", equalTo("CHE" + UID_BIO_SUISSE_WITHOUT_PREFIX))
        .body("dataConsumerLegalName", notNullValue());

    assertThat(auditLogTestUtils.getLatestAuditLogEntry()).isNull();
  }

  @Test
  void givenDraftWithProductsFromDifferentSystems_whenPost_thenReturnBadRequest() {
    DataRequestUpdateDto invalidDto = DataRequestTestFactory.getPartialDataRequestUpdateDtoBuilder()
        .products(List.of(UUID_46F8A883.uuid(), UUID_6319423C.uuid()))
        .build();
    createDataRequestAs(invalidDto, CONSUMER_BIO_SUISSE).then().statusCode(400);
  }

  @Test
  void givenDraftWithDeprecatedProduct_whenPost_thenReturnBadRequest() {
    DataRequestUpdateDto dto = DataRequestTestFactory.getPartialDataRequestUpdateDtoBuilder()
        .products(List.of(UUID_147E8C40.uuid()))
        .build();
    createDataRequestAs(dto, CONSUMER_BIO_SUISSE).then().statusCode(400);
  }

  @Test
  void givenDraftWithInvalidProduct_whenPost_thenReturnBadRequest() {
    DataRequestUpdateDto invalidDto = DataRequestTestFactory.getPartialDataRequestUpdateDtoBuilder()
        .products(List.of(NONEXISTENT_PRODUCT_UUID))
        .build();
    createDataRequestAs(invalidDto, CONSUMER_BIO_SUISSE).then().statusCode(400);
  }

  @Test
  void givenInvalidProducts_whenUpdateDraft_thenReturnInvalidRequest() {
    String id = createDataRequest().then()
        .statusCode(201).extract().path("id");

    DataRequestUpdateDto update = getPartialDataRequestUpdateDtoBuilder()
        .products(List.of(NONEXISTENT_PRODUCT_UUID))
        .build();

    updateDataRequest(id, update)
        .then()
        .statusCode(400);
  }

  @Test
  void givenDeprecatedProducts_whenUpdateDraft_thenReturnInvalidRequest() {
    String id = createDataRequest().then()
        .statusCode(201).extract().path("id");

    DataRequestUpdateDto update = getPartialDataRequestUpdateDtoBuilder()
        .products(List.of(TestDataIdentifiers.DataProduct.UUID_147E8C40.uuid()))
        .build();

    updateDataRequest(id, update)
        .then()
        .statusCode(400);
  }

  @Test
  void givenChangingProducts_whenUpdateDraft_thenReturnUpdatedRequest() {
    String id = createDataRequest().then()
        .statusCode(201).extract().path("id");

    DataRequestUpdateDto firstUpdate = getPartialDataRequestUpdateDtoBuilder()
        .products(List.of(TestDataIdentifiers.DataProduct.UUID_085E4B72.uuid(), TestDataIdentifiers.DataProduct.UUID_A795D0B0.uuid()))
        .build();

    updateDataRequest(id, firstUpdate)
        .then()
        .statusCode(200)
        .body("products.size()", equalTo(2))
        .body("products", hasItems(
            TestDataIdentifiers.DataProduct.UUID_085E4B72.uuid().toString(),
            TestDataIdentifiers.DataProduct.UUID_A795D0B0.uuid().toString()
        ));

    DataRequestUpdateDto secondUpdate = getPartialDataRequestUpdateDtoBuilder()
        .products(List.of(TestDataIdentifiers.DataProduct.UUID_085E4B72.uuid(), TestDataIdentifiers.DataProduct.UUID_0A808700.uuid()))
        .build();

    updateDataRequest(id, secondUpdate)
        .then()
        .statusCode(200)
        .body("products.size()", equalTo(2))
        .body("products", hasItems(
            TestDataIdentifiers.DataProduct.UUID_085E4B72.uuid().toString(),
            TestDataIdentifiers.DataProduct.UUID_0A808700.uuid().toString()
        ));
  }

  @Test
  void givenTooLongFields_whenUpdateDraft_thenReturnBadRequest() {
    String id = createDataRequest().then()
        .statusCode(201).extract().path("id");

    DataRequestUpdateDto invalidDto = getPartialDataRequestUpdateDtoBuilder()
        .title(new DataRequestTitleDto(
            "test more than 255, test more than 255, test more than 255, test more than 255, test more than 255, test more than 255, test more than 255, test more than 255, test more than 255, test more than 255, test more than 255, test more than 255, test more than 255, ",
            "abc", "abc"))
        .products(List.of())
        .build();

    updateDataRequest(id, invalidDto)
        .then()
        .statusCode(400);

    assertThat(auditLogTestUtils.getLatestAuditLogEntry()).isNull();
  }

  @Test
  void givenTooLongFields_whenCreateDraft_thenReturnBadRequest() {
    DataRequestUpdateDto invalidDto = getPartialDataRequestUpdateDtoBuilder()
        .title(new DataRequestTitleDto(
            "test more than 255".repeat(100),
            "abc",
            "abc"))
        .products(List.of())
        .build();

    createDataRequestAs(invalidDto, CONSUMER_BIO_SUISSE).then()
        .statusCode(400);

    assertThat(auditLogTestUtils.getLatestAuditLogEntry()).isNull();
  }

  @Test
  void givenMissingSubmitFields_whenSubmit_thenReturnBadRequest() {
    String id = createDataRequest().then()
        .statusCode(201).extract().path("id");

    setStatusAs(id, DataRequestStateEnum.IN_REVIEW, CONSUMER_BIO_SUISSE)
        .then()
        .statusCode(400);

    assertThat(auditLogTestUtils.getLatestAuditLogEntry()).isNull();
  }

  @Test
  void givenTooManyAdvantages_whenSubmit_thenReturnBadRequest() {
    List<DataRequestAdvantageDto> advantages = Collections.nCopies(6, new DataRequestAdvantageDto("Test DE", "Test FR", "Test IT"));
    DataRequestUpdateDto invalidDto = getDataRequestDtoBuilder()
            .advantages(advantages).build();
    String id = createDataRequestAs(invalidDto, CONSUMER_BIO_SUISSE).then()
            .statusCode(201).extract().path("id");

    setStatusAs(id, DataRequestStateEnum.IN_REVIEW, CONSUMER_BIO_SUISSE)
            .then()
            .statusCode(400)
            .body(containsString("advantages: size must be between"));
  }

  @Test
  void givenAdvantagesWithTooFewLetters_whenSubmit_thenReturnBadRequest() {
    List<DataRequestAdvantageDto> advantages = Collections.nCopies(5, new DataRequestAdvantageDto("Test DE", "Tes", "Test IT"));
    DataRequestUpdateDto invalidDto = getDataRequestDtoBuilder()
            .advantages(advantages).build();
    String id = createDataRequestAs(invalidDto, CONSUMER_BIO_SUISSE).then()
            .statusCode(201).extract().path("id");

    setStatusAs(id, DataRequestStateEnum.IN_REVIEW, CONSUMER_BIO_SUISSE)
            .then()
            .statusCode(400)
            .body(containsString("advantages[0].fr"));
  }

  @Test
  void givenAdminUpdatesValidRedirectUriRegexWithInvalidPattern_thenBadRequest() {
    updateValidRedirectUriRegex(DataRequest.BIO_SUISSE_01.toString(), "(", ADMIN)
        .then()
        .statusCode(400);
  }

  @Test
  void givenAdminUpdatesValidRedirectUriRegex_whenConsumerReadsOwnDataRequest_thenRegexIsVisible() {
    String regex = "^https:\\/\\/consumer\\.example\\.ch(\\/.*)?$";

    updateValidRedirectUriRegex(DataRequest.BIO_SUISSE_01.toString(), regex, ADMIN)
        .then()
        .statusCode(200)
        .body("validRedirectUriRegex", equalTo(regex));

    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE).when()
        .get(DataRequestController.PATH_V1 + "/" + DataRequest.BIO_SUISSE_01)
        .then()
        .statusCode(200)
        .body("validRedirectUriRegex", equalTo(regex));
  }

  @Test
  void givenValidSubmitFields_whenSubmit_thenReturnUpdatedRequest() {
    String id = createDataRequest().then()
        .statusCode(201).extract().path("id");
    updateDataRequest(id, getDataRequestDtoBuilder().build())
        .then()
        .statusCode(200);

    setStatusAs(id, DataRequestStateEnum.IN_REVIEW, CONSUMER_BIO_SUISSE)
        .then()
        .statusCode(200)
        .body("stateCode", equalTo(IN_REVIEW.name()));

    assertThat(auditLogTestUtils.getLatestAuditLogEntry()).satisfies(log -> {
      assertThat(log.getEntityTypeCode()).isEqualTo(DATA_REQUEST.name());
      assertThat(log.getEntityId()).isEqualTo(UUID.fromString(id));
      assertThat(log.getActionCode()).isEqualTo(DATA_REQUEST_SUBMITTED.name());
    });
  }

  @Test
  void givenValidDraftAndAdmin_whenSubmitStateChange_thenReturnBadRequest() {
    String id = createDataRequest().then()
        .statusCode(201).extract().path("id");
    updateDataRequest(id, getDataRequestDtoBuilder().build())
        .then()
        .statusCode(200);

    setStatusAs(id, DataRequestStateEnum.IN_REVIEW, ADMIN)
        .then()
        .statusCode(400);

    assertThat(auditLogTestUtils.getLatestAuditLogEntry()).isNull();
  }

  @Test
  void givenDataRequestWithSignedContractRevision_whenSubmitSignatureTypeByConsumer_thenReturnBadRequest() {
    DataRequestDto dataRequest = createReadyForSigningByConsumerDataRequestFor(CONSUMER_BLV_1);
    signContractRevision(dataRequest.currentContractRevisionId(), CONSUMER_BLV_1, SignatureSlotCodeEnum.DATA_CONSUMER_01);
    setSignatureType(dataRequest.id(), SignatureTypeEnum.INDIVIDUAL_SIGNATURE, CONSUMER_BLV_1)
        .then()
        .statusCode(400);
  }

  @Test
  void givenDataRequestWithSignedContractRevision_whenSubmitSignatureTypeByProvider_thenReturnBadRequest() {
    DataRequestDto dataRequest = createReadyForSigningByProviderDataRequest(CONSUMER_BLV_1, CONSUMER_BLV_2);
    signContractRevision(dataRequest.currentContractRevisionId(), PROVIDER_1, SignatureSlotCodeEnum.DATA_PROVIDER_01);
    setSignatureType(dataRequest.id(), SignatureTypeEnum.INDIVIDUAL_SIGNATURE, PROVIDER_1)
        .then()
        .statusCode(400);
  }

  @Test
  void givenDataRequestInWrongState_whenSubmitSignatureTypeByConsumer_thenReturnBadRequest() {
    DataRequestDto dataRequest = createDataRequest().as(DataRequestDto.class);
    setSignatureType(dataRequest.id(), SignatureTypeEnum.INDIVIDUAL_SIGNATURE, CONSUMER_BIO_SUISSE)
        .then()
        .statusCode(400);
  }

  @Test
  void givenDataRequestInWrongState_whenSubmitSignatureTypeByProvider_thenReturnBadRequest() {
    DataRequestDto dataRequest =
        createReadyForActivatingDataRequest(CONSUMER_BLV_1, CONSUMER_BLV_2, PROVIDER_1, PROVIDER_2).as(DataRequestDto.class);
    setSignatureType(dataRequest.id(), SignatureTypeEnum.INDIVIDUAL_SIGNATURE, PROVIDER_1)
        .then()
        .statusCode(400);
  }

  @Test
  void givenIndividualSignatureType_whenSignByConsumer_thenProceedDataRequestState() {
    DataRequestDto dataRequest = createReadyForSigningByConsumerDataRequestFor(CONSUMER_BLV_1);
    setSignatureType(dataRequest.id(), SignatureTypeEnum.INDIVIDUAL_SIGNATURE, CONSUMER_BLV_1).as(DataRequestDto.class);
    signContractRevision(dataRequest.currentContractRevisionId(), CONSUMER_BLV_1, SignatureSlotCodeEnum.DATA_CONSUMER_01);

    AuthTestUtils.requestAs(CONSUMER_BLV_1).when()
        .get(DataRequestController.PATH_V1 + "/" + dataRequest.id())
        .then()
        .statusCode(200)
        .body("stateCode", equalTo(TO_BE_RELEASED_BY_CONSUMER.name()));
  }

  @Test
  void givenIndividualSignatureType_whenSignByProvider_thenProceedDataRequestState() {
    DataRequestDto dataRequest = createReadyForSigningByProviderDataRequest(CONSUMER_BLV_1, CONSUMER_BLV_2);
    setSignatureType(dataRequest.id(), SignatureTypeEnum.INDIVIDUAL_SIGNATURE, PROVIDER_1).as(DataRequestDto.class);
    signContractRevision(dataRequest.currentContractRevisionId(), PROVIDER_1, SignatureSlotCodeEnum.DATA_PROVIDER_01);

    AuthTestUtils.requestAs(PROVIDER_1).when()
        .get(DataRequestController.PATH_V1 + "/" + dataRequest.id())
        .then()
        .statusCode(200)
        .body("stateCode", equalTo(TO_BE_RELEASED_BY_PROVIDER.name()));
  }

}
