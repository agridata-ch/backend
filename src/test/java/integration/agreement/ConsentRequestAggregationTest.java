package integration.agreement;

import static integration.testutils.TestUserEnum.PRODUCER_A;
import static org.hamcrest.Matchers.equalTo;

import ch.agridata.agreement.controller.ConsentRequestAggregationController;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestDataIdentifiers;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class ConsentRequestAggregationTest {
  @Test
  void givenProducer_whenGetConsentRequestAggregationsWithoutUid_thenReturn400() {
    AuthTestUtils.requestAs(PRODUCER_A)
        .when().get(ConsentRequestAggregationController.PATH)
        .then().statusCode(400)
        .extract().as(new TypeRef<>() {
        });
  }

  @Test
  void givenProducer_whenGetConsentRequestAggregationsWithUnauthorizedUid_thenReturn200AndEmptyList() {
    AuthTestUtils.requestAs(PRODUCER_A)
        .when().get(ConsentRequestAggregationController.PATH + "?dataProducerUid=" + TestDataIdentifiers.Uid.CHE102000001)
        .then().statusCode(200)
        .body("size()", equalTo(0))
        .extract().as(new TypeRef<>() {
        });
  }

  @Test
  void givenProducer_whenGetConsentRequestAggregationsWithAuthorizedUid_thenReturn200AndAggregations() {
    AuthTestUtils.requestAs(PRODUCER_A)
        .when().get(ConsentRequestAggregationController.PATH + "?dataProducerUid=" + TestDataIdentifiers.Uid.CHE101000001)
        .then().statusCode(200)
        .body("size()", equalTo(5))
        .extract().as(new TypeRef<>() {
        });
  }
}
