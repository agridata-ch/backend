package integration.agreement;

import static ch.agridata.agreement.dto.ConsentRequestStateEnum.NOT_CREATED;
import static integration.testutils.TestDataIdentifiers.Uid.CHE102000001;
import static integration.testutils.TestUserEnum.ADMIN;
import static integration.testutils.TestUserEnum.CONSUMER_BIO_SUISSE;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.agreement.controller.DataRequestController;
import ch.agridata.agreement.dto.ConsentRequestConsumerViewDto;
import ch.agridata.agreement.dto.ConsentRequestStateEnum;
import ch.agridata.agreement.persistence.ConsentRequestEntity;
import ch.agridata.agreement.persistence.ConsentRequestRepository;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestDataIdentifiers;
import integration.testutils.TestDataIdentifiers.ConsentRequest;
import integration.testutils.TestDataLoader;
import integration.testutils.TestUserEnum;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class ConsentRequestsOfDataRequestTest {

  private final ConsentRequestRepository consentRequestRepository;

  @Test
  void givenAdmin_whenAnyDataRequestIsRequested_thenConsentRequestsReturned() {
    List<ConsentRequestConsumerViewDto> consentRequests = AuthTestUtils.requestAs(ADMIN)
        .when().get(DataRequestController.PATH + "/" + TestDataIdentifiers.DataRequest.IP_SUISSE_01
            + "/kt-id-p/" + TestUserEnum.PRODUCER_032.getKtIdP()
            + "/consent-requests")
        .then().statusCode(200)
        .extract().as(new TypeRef<>() {
        });


    assertThat(consentRequests).usingRecursiveComparison().ignoringCollectionOrder().isEqualTo(
        List.of(
            buildDto(TestDataLoader.of(consentRequestRepository).load(
                ConsentRequest.IP_SUISSE_01_CHE102000002)),
            ConsentRequestConsumerViewDto.builder()
                .id(null)
                .stateCode(NOT_CREATED)
                .dataProducerUid(CHE102000001.name())
                .build()));
  }

  @Test
  void givenConsumer_whenMismatchingDataRequestIsRequested_thenNoConsentRequestsReturned() {
    List<ConsentRequestConsumerViewDto> consentRequests = AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .when().get(DataRequestController.PATH + "/" + TestDataIdentifiers.DataRequest.IP_SUISSE_01
            + "/kt-id-p/" + TestUserEnum.PRODUCER_032.getKtIdP()
            + "/consent-requests")
        .then().statusCode(200)
        .extract().as(new TypeRef<>() {
        });

    assertThat(consentRequests).usingRecursiveComparison().isEqualTo(emptyList());
  }

  @Test
  void givenConsumer_whenMatchingDataRequestIsRequested_thenConsentRequestsReturned() {
    List<ConsentRequestConsumerViewDto> consentRequests = AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .when().get(DataRequestController.PATH + "/" + TestDataIdentifiers.DataRequest.BIO_SUISSE_01
            + "/kt-id-p/" + TestUserEnum.PRODUCER_032.getKtIdP()
            + "/consent-requests")
        .then().statusCode(200)
        .extract().as(new TypeRef<>() {
        });

    assertThat(consentRequests).usingRecursiveComparison().ignoringCollectionOrder().isEqualTo(
        TestDataLoader.of(consentRequestRepository).load(
                ConsentRequest.BIO_SUISSE_01_CHE102000001,
                ConsentRequest.BIO_SUISSE_01_CHE102000002).stream()
            .map(this::buildDto)
            .toList());
  }

  private ConsentRequestConsumerViewDto buildDto(ConsentRequestEntity consentRequestEntity) {
    return ConsentRequestConsumerViewDto.builder()
        .id(consentRequestEntity.getId())
        .dataProducerUid(consentRequestEntity.getDataProducerUid())
        .stateCode(ConsentRequestStateEnum.valueOf(consentRequestEntity.getStateCode().name()))
        .build();
  }
}
