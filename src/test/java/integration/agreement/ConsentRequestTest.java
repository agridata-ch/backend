package integration.agreement;

import static integration.testutils.TestDataIdentifiers.ConsentRequest.IP_SUISSE_01_CHE101000001;
import static integration.testutils.TestDataIdentifiers.ConsentRequest.IP_SUISSE_01_CHE102000002;
import static integration.testutils.TestUserEnum.PRODUCER_B;
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
import integration.testutils.TestDataIdentifiers.Uid;
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
  void givenProducer_whenGetConsentRequest_thenConsentRequestReturned() {
    AuthTestUtils.requestAs(PRODUCER_B)
        .when().get(ConsentRequestController.PATH + "/" + IP_SUISSE_01_CHE102000002)
        .then().statusCode(200)
        .body("id", equalTo(IP_SUISSE_01_CHE102000002.toString()))
        .extract().as(new TypeRef<>() {
        });
  }

  @Test
  void givenProducer_whenGetConsentRequestOfDifferentUser_thenShouldReturnNotFound() {
    AuthTestUtils.requestAs(PRODUCER_B)
        .when().get(ConsentRequestController.PATH + "/" + IP_SUISSE_01_CHE101000001)
        .then().statusCode(404);

  }

  @Test
  void givenProducer_whenConsentRequestAreRequestedWithoutExplicitUid_thenAllConsentRequestsReturned() {
    AuthTestUtils.requestAs(PRODUCER_B)
        .when().get(ConsentRequestController.PATH)
        .then().statusCode(200)
        .body("size()", equalTo(7))
        .extract().as(new TypeRef<>() {
        });
  }

  @Test
  void givenProducer_whenConsentRequestAreRequestedWithExplicitUid_thenFilteredConsentRequestsReturned() {
    AuthTestUtils.requestAs(PRODUCER_B)
        .when().get(ConsentRequestController.PATH + "?dataProducerUid=" + Uid.CHE102000001)
        .then().statusCode(200)
        .body("size()", equalTo(3))
        .extract().as(new TypeRef<>() {
        });
  }

  @Test
  void givenProducer_whenConsentRequestAreRequestedWithUncorrelatedUid_thenNoConsentRequestsReturned() {
    AuthTestUtils.requestAs(PRODUCER_B)
        .when().get(ConsentRequestController.PATH + "?dataProducerUid=" + Uid.CHE101000001)
        .then().statusCode(200)
        .body("size()", equalTo(0))
        .extract().as(new TypeRef<>() {
        });
  }

  @Test
  void givenProducer_whenCreateConsentRequests_thenCreatedConsentRequestReturned() throws JsonProcessingException {
    var createDtos = PRODUCER_B.getCompanyUids().stream()
        .map(uid -> CreateConsentRequestDto.builder().dataRequestId(TestDataIdentifiers.DataRequest.BIO_SUISSE_01.uuid()).uid(uid.name())
            .build())
        .toList();
    List<ConsentRequestCreatedDto> createdConsentRequests = AuthTestUtils.requestAs(PRODUCER_B)
        .contentType(JSON)
        .body(MAPPER.writeValueAsString(createDtos))
        .when().post(ConsentRequestController.PATH)
        .then().statusCode(201)
        .extract().as(new TypeRef<>() {
        });
    assertThat(createdConsentRequests).hasSize(4).extracting(ConsentRequestCreatedDto::dataProducerUid)
        .containsExactlyInAnyOrderElementsOf(PRODUCER_B.getCompanyUids().stream().map(Uid::name).toList());

  }
}
