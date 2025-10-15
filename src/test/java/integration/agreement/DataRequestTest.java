package integration.agreement;

import static ch.agridata.agreement.dto.DataRequestStateEnum.IN_REVIEW;
import static integration.agreement.DataRequestTestFactory.createDataRequest;
import static integration.agreement.DataRequestTestFactory.getDataRequestDto;
import static integration.agreement.DataRequestTestFactory.getPartialDataRequestUpdateDtoBuilder;
import static integration.agreement.DataRequestTestFactory.setStatusAs;
import static integration.agreement.DataRequestTestFactory.updateDataRequest;
import static integration.testutils.TestDataConstants.UID_BIO_SUISSE_WITHOUT_PREFIX;
import static integration.testutils.TestUserEnum.ADMIN;
import static integration.testutils.TestUserEnum.CONSUMER_BIO_SUISSE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.notNullValue;

import ch.agridata.agreement.controller.DataRequestController;
import ch.agridata.agreement.dto.DataRequestDescriptionDto;
import ch.agridata.agreement.dto.DataRequestDto;
import ch.agridata.agreement.dto.DataRequestPurposeDto;
import ch.agridata.agreement.dto.DataRequestStateEnum;
import ch.agridata.agreement.dto.DataRequestTitleDto;
import ch.agridata.agreement.dto.DataRequestUpdateDto;
import ch.agridata.agreement.persistence.DataRequestDataProductEntity;
import ch.agridata.agreement.persistence.DataRequestRepository;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestDataIdentifiers;
import integration.testutils.TestDataIdentifiers.DataRequest;
import integration.testutils.TestDataLoader;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class DataRequestTest {
  private final Flyway flyway;
  private final DataRequestRepository dataRequestRepository;

  @BeforeEach
  void setUp() {
    // will make sure testdata is reset between tests
    flyway.migrate();
  }

  @Test
  void givenConsumer_whenGetDataRequests_thenOnlyConsumerDataRequestsReturned() {
    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE).when().get(DataRequestController.PATH).then()
        .statusCode(200)
        .body("size()", equalTo(3));
  }

  @Test
  void givenAdmin_whenGetDataRequests_thenAllDataRequestsReturned() {
    AuthTestUtils.requestAs(ADMIN).when().get(DataRequestController.PATH).then()
        .statusCode(200)
        .body("size()", equalTo(8));
  }

  @Test
  void givenExistingDataRequestWithDifferentUID_whenGetDataRequest_thenReturnNotFound() {
    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE).when()
        .get(DataRequestController.PATH + "/" + DataRequest.IP_SUISSE_01)
        .then()
        .statusCode(404);
  }

  @Test
  void givenExistingDataRequestAndAdmin__whenGetDataRequest_thenReturnFound() {
    AuthTestUtils.requestAs(ADMIN).when()
        .get(DataRequestController.PATH + "/" + DataRequest.IP_SUISSE_01)
        .then()
        .statusCode(200);
  }

  @Test
  void givenExistingId_whenGetDataRequest_thenReturnRequest() {

    DataRequestDto dataRequestDto = AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE).when()
        .get(DataRequestController.PATH + "/" + DataRequest.BIO_SUISSE_01)
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
  void givenToLongFields_whenUpdateDraft_thenReturnBadRequest() {
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
  }

  @Test
  void givenMissingSubmitFields_whenSubmit_thenReturnBadRequest() {
    String id = createDataRequest().then()
        .statusCode(201).extract().path("id");

    setStatusAs(id, DataRequestStateEnum.IN_REVIEW, CONSUMER_BIO_SUISSE)
        .then()
        .statusCode(400);
  }

  @Test
  void givenValidSubmitFields_whenSubmit_thenReturnUpdatedRequest() {
    String id = createDataRequest().then()
        .statusCode(201).extract().path("id");
    updateDataRequest(id, getDataRequestDto().build())
        .then()
        .statusCode(200);

    setStatusAs(id, DataRequestStateEnum.IN_REVIEW, CONSUMER_BIO_SUISSE)
        .then()
        .statusCode(200)
        .body("stateCode", equalTo(IN_REVIEW.name()));
  }

}
