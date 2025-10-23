package integration.agreement;

import static integration.testutils.TestUserEnum.PRODUCER_032;
import static io.restassured.http.ContentType.JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

import ch.agridata.agreement.controller.ConsentRequestController;
import ch.agridata.agreement.dto.ConsentRequestCreatedDto;
import ch.agridata.agreement.dto.CreateConsentRequestDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestDataIdentifiers;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class ConsentRequestTest {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Test
  void givenProducer_whenConsentRequestAreRequestedWithoutExplicitUid_thenAllConsentRequestsReturned() {
    AuthTestUtils.requestAs(PRODUCER_032)
        .when().get(ConsentRequestController.PATH)
        .then().statusCode(200)
        .body("size()", equalTo(7))
        .extract().as(new TypeRef<>() {
        });
  }

  @Test
  void givenProducer_whenConsentRequestAreRequestedWithExplicitUid_thenFilteredConsentRequestsReturned() {
    AuthTestUtils.requestAs(PRODUCER_032)
        .when().get(ConsentRequestController.PATH + "?dataProducerUid=CHE***435")
        .then().statusCode(200)
        .body("size()", equalTo(3))
        .extract().as(new TypeRef<>() {
        });
  }

  @Test
  void givenProducer_whenConsentRequestAreRequestedWithUncorrelatedUid_thenNoConsentRequestsReturned() {
    AuthTestUtils.requestAs(PRODUCER_032)
        .when().get(ConsentRequestController.PATH + "?dataProducerUid=CHE***280")
        .then().statusCode(200)
        .body("size()", equalTo(0))
        .extract().as(new TypeRef<>() {
        });
  }

  @Test
  void givenProducer_whenCreateConsentRequests_thenCreatedConsentRequestReturned() throws JsonProcessingException {
    var createDtos = PRODUCER_032.getCompanyUids().stream()
        .map(uid -> CreateConsentRequestDto.builder().dataRequestId(TestDataIdentifiers.DataRequest.BIO_SUISSE_01.uuid()).uid(uid).build())
        .toList();
    List<ConsentRequestCreatedDto> createdConsentRequests = AuthTestUtils.requestAs(PRODUCER_032)
        .contentType(JSON)
        .body(MAPPER.writeValueAsString(createDtos))
        .when().post(ConsentRequestController.PATH)
        .then().statusCode(201)
        .extract().as(new TypeRef<>() {
        });
    assertThat(createdConsentRequests).hasSize(2).extracting(ConsentRequestCreatedDto::dataProducerUid)
        .containsExactlyInAnyOrderElementsOf(PRODUCER_032.getCompanyUids());

  }
}
