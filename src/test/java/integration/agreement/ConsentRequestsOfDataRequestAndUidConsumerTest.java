package integration.agreement;

import static integration.testutils.TestDataIdentifiers.Uid.CHE101000001;
import static integration.testutils.TestDataIdentifiers.Uid.CHE102000001;
import static integration.testutils.TestUserEnum.CONSUMER_BIO_SUISSE;
import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.agreement.controller.DataRequestController;
import ch.agridata.agreement.dto.ConsentRequestFundamentalViewDto;
import ch.agridata.agreement.mapper.ConsentRequestMapper;
import ch.agridata.agreement.persistence.ConsentRequestFundamentalViewRepository;
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
class ConsentRequestsOfDataRequestAndUidConsumerTest {

  private final ConsentRequestFundamentalViewRepository consentRequestFundamentalViewRepository;
  private final ConsentRequestMapper consentRequestMapper;

  @Test
  void givenConsumer_whenRequestingConsentRequestsOfOwnDataRequestAndUid_thenUidAndBurRowsReturned() {
    // BIO_SUISSE_01 belongs to CONSUMER_BIO_SUISSE; producer CHE101000001 has a UID row and a BUR row.
    List<ConsentRequestFundamentalViewDto> response = AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .when().get(consentRequestsUrl(TestDataIdentifiers.DataRequest.BIO_SUISSE_01.toString(), CHE101000001.name()))
        .then().statusCode(200)
        .extract().as(new TypeRef<>() {
        });

    var expectedItems = consentRequestFundamentalViewRepository.findByIds(List.of(
            ConsentRequest.BIO_SUISSE_01_CHE101000001.uuid(),
            ConsentRequest.BIO_SUISSE_01_CHE101000001_99910003.uuid())).stream()
        .map(consentRequestMapper::toConsentRequestFundamentalViewDto)
        .toList();

    assertThat(response).usingRecursiveComparison().ignoringCollectionOrder().isEqualTo(expectedItems);
  }

  @Test
  void givenConsumer_whenRequestingConsentRequestsOfOwnDataRequestAndUid_thenTerminatedRelationRowExcluded() {
    // CHE101000001 has a terminated BUR relation (uidBurRelationUntil set) under BIO_SUISSE_01 that must be hidden.
    List<ConsentRequestFundamentalViewDto> response = AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .when().get(consentRequestsUrl(TestDataIdentifiers.DataRequest.BIO_SUISSE_01.toString(), CHE101000001.name()))
        .then().statusCode(200)
        .extract().as(new TypeRef<>() {
        });

    assertThat(response)
        .extracting(ConsentRequestFundamentalViewDto::id)
        .isNotEmpty()
        .doesNotContain(ConsentRequest.BIO_SUISSE_01_CHE101000001_99910006_TERMINATED.uuid());
  }

  @Test
  void givenConsumer_whenRequestingConsentRequestsOfOwnDataRequestAndUidWithoutBur_thenOnlyUidRowReturned() {
    // Producer CHE102000001 has only a UID row under BIO_SUISSE_01.
    List<ConsentRequestFundamentalViewDto> response = AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .when().get(consentRequestsUrl(TestDataIdentifiers.DataRequest.BIO_SUISSE_01.toString(), CHE102000001.name()))
        .then().statusCode(200)
        .extract().as(new TypeRef<>() {
        });

    var expectedItems = consentRequestFundamentalViewRepository.findByIds(List.of(
            ConsentRequest.BIO_SUISSE_01_CHE102000001.uuid())).stream()
        .map(consentRequestMapper::toConsentRequestFundamentalViewDto)
        .toList();

    assertThat(response).usingRecursiveComparison().ignoringCollectionOrder().isEqualTo(expectedItems);
  }

  @Test
  void givenConsumer_whenRequestingOwnDataRequestAndUidWithoutConsentRequests_thenEmptyResultReturned() {
    List<ConsentRequestFundamentalViewDto> response = AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .when().get(consentRequestsUrl(TestDataIdentifiers.DataRequest.BIO_SUISSE_01.toString(), "CHE999999999"))
        .then().statusCode(200)
        .extract().as(new TypeRef<>() {
        });

    assertThat(response).isEmpty();
  }

  @Test
  void givenConsumer_whenRequestingDataRequestOfDifferentConsumer_thenNotFound() {
    // IP_SUISSE_01 does not belong to the CONSUMER_BIO_SUISSE test user.
    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .when().get(consentRequestsUrl(TestDataIdentifiers.DataRequest.IP_SUISSE_01.toString(), CHE101000001.name()))
        .then().statusCode(404);
  }

  private static String consentRequestsUrl(String dataRequestId, String uid) {
    return DataRequestController.PATH_V1 + "/" + dataRequestId + "/uids/" + uid + "/consent-requests";
  }
}
