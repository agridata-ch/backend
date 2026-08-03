package integration.agreement;

import static integration.testutils.TestUserEnum.PRODUCER_A;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

import ch.agridata.agreement.controller.ConsentRequestAggregationController;
import ch.agridata.agreement.dto.ConsentRequestAggregationProducerView;
import ch.agridata.agreement.dto.ConsentRequestProducerViewDto;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestDataIdentifiers;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import java.util.List;
import java.util.UUID;
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
        .body("size()", equalTo(7))
        .extract().as(new TypeRef<>() {
        });
  }

  @Test
  void givenProducer_whenGetConsentRequestAggregations_thenDataProducerBurIsSetForBurAndNullForUid() {
    List<ConsentRequestAggregationProducerView> aggregations = AuthTestUtils.requestAs(PRODUCER_A)
        .when().get(ConsentRequestAggregationController.PATH + "?dataProducerUid=" + TestDataIdentifiers.Uid.CHE101000001)
        .then().statusCode(200)
        .extract().as(new TypeRef<>() {
        });

    var bioSuisse01 = aggregations.stream()
        .filter(aggregation -> aggregation.id().equals(TestDataIdentifiers.DataRequest.BIO_SUISSE_01.uuid()))
        .findFirst()
        .orElseThrow();

    var uidConsentRequest = findConsentRequest(
        bioSuisse01.consentRequests(),
        TestDataIdentifiers.ConsentRequest.BIO_SUISSE_01_CHE101000001.uuid()
    );
    var burConsentRequest = findConsentRequest(
        bioSuisse01.consentRequests(),
        TestDataIdentifiers.ConsentRequest.BIO_SUISSE_01_CHE101000001_99910003.uuid()
    );

    assertThat(uidConsentRequest.dataProducerBur()).isNull();
    assertThat(burConsentRequest.dataProducerBur()).isEqualTo(TestDataIdentifiers.Bur.CODE_99910003.getCode());
  }

  private static ConsentRequestProducerViewDto findConsentRequest(List<ConsentRequestProducerViewDto> consentRequests, UUID id) {
    return consentRequests.stream()
        .filter(consentRequest -> consentRequest.id().equals(id))
        .findFirst()
        .orElseThrow();
  }
}
