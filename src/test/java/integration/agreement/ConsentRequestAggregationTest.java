package integration.agreement;

import static integration.testutils.TestUserEnum.PRODUCER_A;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

import ch.agridata.agreement.controller.ConsentRequestAggregationController;
import ch.agridata.agreement.dto.ConsentRequestAggregationDto;
import ch.agridata.agreement.dto.ConsentRequestAggregationStateEnum;
import ch.agridata.agreement.dto.ConsentRequestAggregationSummaryDto;
import ch.agridata.agreement.dto.ConsentRequestProducerViewV2Dto;
import ch.agridata.agreement.dto.ConsentRequestStateEnum;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestDataIdentifiers;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
  void givenProducer_whenGetConsentRequestAggregations_thenSummaryHasAggregatedFieldsAndOrderedConsentRequests() {
    List<ConsentRequestAggregationSummaryDto> aggregations = AuthTestUtils.requestAs(PRODUCER_A)
        .when().get(ConsentRequestAggregationController.PATH + "?dataProducerUid=" + TestDataIdentifiers.Uid.CHE101000001)
        .then().statusCode(200)
        .extract().as(new TypeRef<>() {
        });

    var bioSuisse01 = aggregations.stream()
        .filter(aggregation -> aggregation.id().equals(TestDataIdentifiers.DataRequest.BIO_SUISSE_01.uuid()))
        .findFirst()
        .orElseThrow();

    assertThat(bioSuisse01.stateCode()).isEqualTo(ConsentRequestAggregationStateEnum.DECLINED);
    assertThat(bioSuisse01.requestDate()).isEqualTo(LocalDate.of(2025, 3, 14));
    assertThat(bioSuisse01.showStateAsMigrated()).isTrue();

    assertThat(bioSuisse01.consentRequests())
        .extracting(ConsentRequestAggregationSummaryDto.ConsentRequestStateDto::id)
        .containsExactly(
            TestDataIdentifiers.ConsentRequest.BIO_SUISSE_01_CHE101000001.uuid(),
            TestDataIdentifiers.ConsentRequest.BIO_SUISSE_01_CHE101000001_99910003.uuid()
        );
  }

  @Test
  void givenProducer_whenGetConsentRequestAggregationWithUnknownDataProducerUid_thenReturn404() {
    AuthTestUtils.requestAs(PRODUCER_A)
        .when().get(
            ConsentRequestAggregationController.PATH + "/" + TestDataIdentifiers.DataRequest.BIO_SUISSE_01.uuid()
                + "?dataProducerUid=" + TestDataIdentifiers.Uid.CHE102000001
        )
        .then().statusCode(404);
  }

  @Test
  void givenProducer_whenGetConsentRequestAggregation_thenReturn200AndFullDetailsWithDataProducerBurSetForBurAndNullForUid() {
    ConsentRequestAggregationDto bioSuisse01 = AuthTestUtils.requestAs(PRODUCER_A)
        .when().get(
            ConsentRequestAggregationController.PATH + "/" + TestDataIdentifiers.DataRequest.BIO_SUISSE_01.uuid()
                + "?dataProducerUid=" + TestDataIdentifiers.Uid.CHE101000001
        )
        .then().statusCode(200)
        .extract().as(ConsentRequestAggregationDto.class);

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

    assertThat(bioSuisse01.consentRequests())
        .extracting(ConsentRequestProducerViewV2Dto::id)
        .containsExactlyInAnyOrder(
            TestDataIdentifiers.ConsentRequest.BIO_SUISSE_01_CHE101000001.uuid(),
            TestDataIdentifiers.ConsentRequest.BIO_SUISSE_01_CHE101000001_99910003.uuid()
        );
    assertThat(bioSuisse01.consentRequests())
        .extracting(ConsentRequestProducerViewV2Dto::stateCode)
        .containsOnly(ConsentRequestStateEnum.DECLINED);

    assertThat(bioSuisse01.stateCode()).isEqualTo(ConsentRequestAggregationStateEnum.DECLINED);
    assertThat(bioSuisse01.requestDate()).isEqualTo(LocalDate.of(2025, 3, 14));
    assertThat(bioSuisse01.showStateAsMigrated()).isTrue();
    assertThat(bioSuisse01.lastStateChangeDate()).isEqualTo(LocalDateTime.of(2025, 3, 20, 14, 25));
  }

  private static ConsentRequestProducerViewV2Dto findConsentRequest(List<ConsentRequestProducerViewV2Dto> consentRequests, UUID id) {
    return consentRequests.stream()
        .filter(consentRequest -> consentRequest.id().equals(id))
        .findFirst()
        .orElseThrow();
  }
}
