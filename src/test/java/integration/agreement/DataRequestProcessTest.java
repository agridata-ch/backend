package integration.agreement;

import static integration.agreement.DataRequestTestFactory.createDataRequest;
import static integration.agreement.DataRequestTestFactory.getDataRequestDto;
import static integration.agreement.DataRequestTestFactory.getPartialDataRequestUpdateDtoBuilder;
import static integration.agreement.DataRequestTestFactory.setStatusAs;
import static integration.agreement.DataRequestTestFactory.updateDataRequest;
import static integration.agreement.DataRequestTestFactory.updateLogo;
import static integration.testutils.TestUserEnum.ADMIN;
import static integration.testutils.TestUserEnum.CONSUMER_BIO_SUISSE;
import static integration.testutils.TestUserEnum.PRODUCER_032;
import static io.restassured.http.ContentType.JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import ch.agridata.agreement.controller.ConsentRequestController;
import ch.agridata.agreement.dto.ConsentRequestCreatedDto;
import ch.agridata.agreement.dto.DataRequestDescriptionDto;
import ch.agridata.agreement.dto.DataRequestStateEnum;
import ch.agridata.agreement.dto.DataRequestUpdateDto;
import ch.agridata.product.controller.DataProductController;
import ch.agridata.product.dto.DataProductDto;
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

  @Test
  void givenValidRequestFlow_whenDraftUpdatedAndSubmitted_thenAllStepsSucceed() {
    RequestSpecification consumer = AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE);

    // Load a valid product ID via API
    List<DataProductDto> products = consumer
        .when().get(DataProductController.PATH)
        .then().statusCode(200)
        .extract().as(new TypeRef<>() {
        });

    UUID productId = products.getFirst().id();

    // Step 1: Create draft
    String dataRequestId = createDataRequest().then()
        .statusCode(201)
        .body("stateCode", equalTo(DataRequestStateEnum.DRAFT.name()))
        .extract().path("id");

    // Step 2: Update draft
    DataRequestUpdateDto updateDto = getPartialDataRequestUpdateDtoBuilder()
        .description(DataRequestDescriptionDto.builder().de("test de").build())
        .products(List.of(productId))
        .build();
    updateDataRequest(dataRequestId, updateDto)
        .then()
        .statusCode(200)
        .body("description.de", equalTo(updateDto.description().de()))
        .body("products", hasItem(productId.toString()));

    // Step 3: Add logo
    updateLogo(dataRequestId, "test-logo-95kB.png")
        .then()
        .statusCode(204);

    // Step 4: set status to IN_REVIEW with invalid data
    setStatusAs(dataRequestId, DataRequestStateEnum.IN_REVIEW, CONSUMER_BIO_SUISSE)
        .then()
        .statusCode(400)
        .body("requestId", not(nullValue()));

    // Step 4: update request with complete data
    updateDataRequest(dataRequestId, getDataRequestDto().build())
        .then()
        .statusCode(200);

    // Step 4: Set status to in review
    setStatusAs(dataRequestId, DataRequestStateEnum.IN_REVIEW, CONSUMER_BIO_SUISSE)
        .then()
        .statusCode(200)
        .body("stateCode", equalTo(DataRequestStateEnum.IN_REVIEW.name()));

    // Step 4: try to update again (should fail)
    updateDataRequest(dataRequestId, getDataRequestDto().build())
        .then()
        .statusCode(400);

    // Step 5: as Admin set status to DRAFT
    setStatusAs(dataRequestId, DataRequestStateEnum.DRAFT, ADMIN)
        .then()
        .statusCode(200)
        .body("stateCode", equalTo(DataRequestStateEnum.DRAFT.name()));

    // Step 5: Set status to in review after fixing the request
    setStatusAs(dataRequestId, DataRequestStateEnum.IN_REVIEW, CONSUMER_BIO_SUISSE)
        .then()
        .statusCode(200)
        .body("stateCode", equalTo(DataRequestStateEnum.IN_REVIEW.name()));

    // Step 5: as Admin set status to to be signed
    setStatusAs(dataRequestId, DataRequestStateEnum.TO_BE_SIGNED, ADMIN)
        .then()
        .statusCode(200)
        .body("stateCode", equalTo(DataRequestStateEnum.TO_BE_SIGNED.name()));

    // Step 5: as Admin set status to active
    setStatusAs(dataRequestId, DataRequestStateEnum.ACTIVE, ADMIN)
        .then()
        .statusCode(200)
        .body("stateCode", equalTo(DataRequestStateEnum.ACTIVE.name()));

    // As producer use consent request creation link
    List<ConsentRequestCreatedDto> createdConsentRequests = AuthTestUtils.requestAs(PRODUCER_032)
        .contentType(JSON)
        .when().post(ConsentRequestController.PATH + "/" + dataRequestId + "/create")
        .then().statusCode(201)
        .extract().as(new TypeRef<>() {
        });
    assertThat(createdConsentRequests).hasSize(2).extracting(ConsentRequestCreatedDto::dataProducerUid)
        .containsExactlyInAnyOrderElementsOf(PRODUCER_032.getCompanyUids());


  }

}
