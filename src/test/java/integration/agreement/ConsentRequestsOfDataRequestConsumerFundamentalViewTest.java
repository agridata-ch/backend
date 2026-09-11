package integration.agreement;

import static integration.testutils.TestUserEnum.CONSUMER_BIO_SUISSE;
import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.agreement.controller.DataRequestController;
import ch.agridata.agreement.dto.ConsentRequestFundamentalViewDto;
import ch.agridata.agreement.mapper.ConsentRequestMapper;
import ch.agridata.agreement.persistence.ConsentRequestFundamentalViewRepository;
import ch.agridata.common.dto.PageResponseDto;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestDataIdentifiers;
import integration.testutils.TestDataIdentifiers.ConsentRequest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class ConsentRequestsOfDataRequestConsumerFundamentalViewTest {

  private final ConsentRequestFundamentalViewRepository consentRequestFundamentalViewRepository;
  private final ConsentRequestMapper consentRequestMapper;

  @Test
  void givenConsumer_whenRequestingConsentRequestsOfOwnDataRequest_thenAllConsentRequestsReturned() {
    // BIO_SUISSE_01 belongs to the CONSUMER_BIO_SUISSE test user and has 4 consent requests.
    PageResponseDto<ConsentRequestFundamentalViewDto> response = AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .when().get(DataRequestController.PATH_V1 + "/" + TestDataIdentifiers.DataRequest.BIO_SUISSE_01 + "/consent-requests")
        .then().statusCode(200)
        .extract().as(new TypeRef<>() {
        });

    var expectedItems = consentRequestFundamentalViewRepository.findByIds(List.of(
            ConsentRequest.BIO_SUISSE_01_CHE101000001.uuid(),
            ConsentRequest.BIO_SUISSE_01_CHE101000001_99910003.uuid(),
            ConsentRequest.BIO_SUISSE_01_CHE102000001.uuid(),
            ConsentRequest.BIO_SUISSE_01_CHE102000002.uuid())).stream()
        .map(consentRequestMapper::toConsentRequestFundamentalViewDto)
        .toList();

    assertThat(response.items()).usingRecursiveComparison().ignoringCollectionOrder().isEqualTo(expectedItems);
    assertThat(response.totalItems()).isEqualTo(4);
    assertThat(response.currentPage()).isZero();
  }

  @Test
  void givenConsumer_whenFilteringByLastModifiedFromFarInFuture_thenEmptyResultReturned() {
    // All test data has modifiedAt = NOW(), so a far-future filter must yield an empty result.
    PageResponseDto<ConsentRequestFundamentalViewDto> response = AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .queryParam("lastModifiedFrom", "9999-12-31T00:00:00")
        .when().get(DataRequestController.PATH_V1 + "/" + TestDataIdentifiers.DataRequest.BIO_SUISSE_01 + "/consent-requests")
        .then().statusCode(200)
        .extract().as(new TypeRef<>() {
        });

    assertThat(response.items()).isEmpty();
    assertThat(response.totalItems()).isZero();
  }

  @Test
  void givenConsumer_whenRequestingDataRequestOfDifferentConsumer_thenNotFound() {
    // IP_SUISSE_01 does not belong to the CONSUMER_BIO_SUISSE test user.
    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .when().get(DataRequestController.PATH_V1 + "/" + TestDataIdentifiers.DataRequest.IP_SUISSE_01 + "/consent-requests")
        .then().statusCode(404);
  }
}