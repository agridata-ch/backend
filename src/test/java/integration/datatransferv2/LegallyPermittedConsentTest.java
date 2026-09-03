package integration.datatransferv2;

import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.LEGALLY_PERMITTED;
import static integration.testutils.TestUserEnum.CONSUMER_BLV_1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import ch.agridata.agreement.persistence.ConsentRequestFundamentalViewEntity;
import ch.agridata.agreement.service.ConsentRequestLegallyPermittedService;
import ch.agridata.datatransferv2.controller.DataTransferController;
import ch.agridata.product.persistence.DataProductEntity;
import com.github.tomakehurst.wiremock.client.WireMock;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestDataIdentifiers;
import integration.testutils.TestDataIdentifiers.DataProduct;
import integration.testutils.TestDataIdentifiers.DataRequest;
import integration.testutils.TestDataIdentifiers.Identifier;
import integration.testutils.TestDataIdentifiers.Uid;
import io.quarkiverse.wiremock.devservice.ConnectWireMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
@ConnectWireMock
class LegallyPermittedConsentTest {

  private static final Identifier<DataProductEntity> BUR_BASED_CONSENT_FREE_PRODUCT = DataProduct.UUID_4176737B;
  private static final Identifier<DataProductEntity> UID_BASED_CONSENT_FREE_PRODUCT = DataProduct.UUID_481372C6;
  private static final Uid PRODUCER_UID = Uid.CHE102000001;
  private static final Uid UID_BASED_PRODUCER_UID = Uid.CHE102000002;
  private static final String PRODUCER_BUR = TestDataIdentifiers.Bur.CODE_99920004.getCode();
  /**
   * Direct farm-to-person relation start date of CHE102000001/99920004 in the AGIS stubs
   */
  private static final LocalDateTime RELATION_SINCE = LocalDateTime.of(2025, 12, 11, 8, 23, 31);

  private final EntityManager entityManager;
  private final ConsentRequestLegallyPermittedService legallyPermittedService;

  WireMock wireMock;

  @Test
  void givenConsentFreeProductWithoutConsent_whenFetchedTwice_thenOkAndSingleLegallyPermittedConsentRequestCreated() {
    stubAgisFarmForBur();

    assertThat(legallyPermittedConsentRequestRows(DataRequest.BLV_ZO_CONSENT_FREE.uuid(), PRODUCER_UID)).isEmpty();

    fetchProduct(DataRequest.BLV_ZO_CONSENT_FREE.uuid()).then().statusCode(200);
    legallyPermittedService.awaitAllProcessed();

    assertThat(legallyPermittedConsentRequestRows(DataRequest.BLV_ZO_CONSENT_FREE.uuid(), PRODUCER_UID))
        .hasSize(2)
        .extracting(
            ConsentRequestFundamentalViewEntity::getDataProducerUid,
            ConsentRequestFundamentalViewEntity::getDataProducerBur,
            ConsentRequestFundamentalViewEntity::getGrantedDataPeriodFrom,
            ConsentRequestFundamentalViewEntity::getStateCode,
            ConsentRequestFundamentalViewEntity::getCreatedBy)
        .containsExactlyInAnyOrder(
            tuple(
                PRODUCER_UID.name(),
                PRODUCER_BUR,
                RELATION_SINCE.toLocalDate(),
                LEGALLY_PERMITTED,
                ConsentRequestLegallyPermittedService.USER_ID_LEGALLY_PERMITTED_CONSENT),
            tuple(
                PRODUCER_UID.name(),
                null,
                LocalDate.of(1970, 1, 1),
                LEGALLY_PERMITTED,
                ConsentRequestLegallyPermittedService.USER_ID_LEGALLY_PERMITTED_CONSENT));

    // A repeated fetch must not create a duplicate
    fetchProduct(DataRequest.BLV_ZO_CONSENT_FREE.uuid()).then().statusCode(200);
    legallyPermittedService.awaitAllProcessed();

    assertThat(legallyPermittedConsentRequestRows(DataRequest.BLV_ZO_CONSENT_FREE.uuid(), PRODUCER_UID)).hasSize(2);
  }

  @Test
  void givenUidBasedConsentFreeProductWithoutConsent_whenFetched_thenOkAndUidLevelLegallyPermittedConsentRequestCreated() {
    assertThat(legallyPermittedConsentRequestRows(DataRequest.BLV_ZO_CONSENT_FREE.uuid(), UID_BASED_PRODUCER_UID)).isEmpty();

    fetchUidBasedProduct(DataRequest.BLV_ZO_CONSENT_FREE.uuid()).then().statusCode(200);
    legallyPermittedService.awaitAllProcessed();

    // UID-based flow: producer identity is the supplied UID, so no BUR resolution is recorded
    assertThat(legallyPermittedConsentRequestRows(DataRequest.BLV_ZO_CONSENT_FREE.uuid(), UID_BASED_PRODUCER_UID))
        .hasSize(1)
        .extracting(
            ConsentRequestFundamentalViewEntity::getDataProducerBur,
            ConsentRequestFundamentalViewEntity::getGrantedDataPeriodFrom,
            ConsentRequestFundamentalViewEntity::getStateCode,
            ConsentRequestFundamentalViewEntity::getCreatedBy)
        .containsExactly(tuple(
            null,
            LocalDate.of(1970, 1, 1),
            LEGALLY_PERMITTED,
            ConsentRequestLegallyPermittedService.USER_ID_LEGALLY_PERMITTED_CONSENT));
  }

  @Test
  void givenConsentFreeProductWithoutDataRequestIdParameter_whenFetched_thenBadRequestAndProviderNotCalled() {
    fetchProduct(null).then().statusCode(400);

    wireMock.verifyThat(0, WireMock.anyRequestedFor(WireMock.urlPathMatching(".*/api-zo/.*")));
  }

  @Test
  void givenConsentFreeProductWithForeignDataRequestId_whenFetched_thenForbiddenAndNoConsentRequestCreated() {
    // BLV_ZO belongs to the consumer but does not cover the consent-free product
    fetchProduct(DataRequest.BLV_ZO.uuid()).then().statusCode(403);
    legallyPermittedService.awaitAllProcessed();

    assertThat(legallyPermittedConsentRequestRows(DataRequest.BLV_ZO.uuid(), PRODUCER_UID)).isEmpty();
    wireMock.verifyThat(0, WireMock.anyRequestedFor(WireMock.urlPathMatching(".*/api-zo/.*")));
  }

  @Test
  void givenAgisFailureDuringBurResolution_whenConsentFreeProductFetched_thenOkAndNoConsentRequestCreated() {
    wireMock.register(WireMock.post(WireMock.urlEqualTo("/agis/register-data/1/register"))
        .withRequestBody(WireMock.matchingJsonPath("$.farmSearchParameters.ber"))
        .atPriority(1)
        .willReturn(WireMock.aResponse().withStatus(500)));

    // The transfer response is unaffected by the failing asynchronous BUR resolution
    fetchProduct(DataRequest.BLV_ZO_CONSENT_FREE.uuid()).then().statusCode(200);
    legallyPermittedService.awaitAllProcessed();

    assertThat(legallyPermittedConsentRequestRows(DataRequest.BLV_ZO_CONSENT_FREE.uuid(), PRODUCER_UID)).isEmpty();
  }

  private void stubAgisFarmForBur() {
    wireMock.register(WireMock.post(WireMock.urlEqualTo("/agis/register-data/1/register"))
        .withRequestBody(WireMock.matchingJsonPath("$.farmSearchParameters.ber", WireMock.equalTo(PRODUCER_BUR)))
        .atPriority(1)
        .willReturn(WireMock.okJson("""
            {
              "personFarmTree": {
                "relevantPersons": {},
                "personRelations": {},
                "farmRelations": {},
                "relevantFarms": {
                  "farm": [ { "ktIdB": "FLTEST0001", "agisIdB": 1, "ber": "%s", "uid": "%s" } ]
                }
              }
            }""".formatted(PRODUCER_BUR, PRODUCER_UID.name()))));
  }

  private Response fetchProduct(UUID dataRequestId) {
    var request = AuthTestUtils.requestAs(CONSUMER_BLV_1)
        .pathParam("productId", BUR_BASED_CONSENT_FREE_PRODUCT.uuid())
        .queryParam("eartagNumber", "CH120030812345")
        .queryParam("dateFrom", "2026-09-01")
        .queryParam("dateTo", "2026-09-01")
        .queryParam("recipientUid", "CHE123456789");
    if (dataRequestId != null) {
      request.queryParam("dataRequestId", dataRequestId.toString());
    }
    return request.when().get(DataTransferController.PATH + "/product/{productId}/data");
  }

  private Response fetchUidBasedProduct(UUID dataRequestId) {
    return AuthTestUtils.requestAs(CONSUMER_BLV_1)
        .pathParam("productId", UID_BASED_CONSENT_FREE_PRODUCT.uuid())
        .queryParam("uid", UID_BASED_PRODUCER_UID.name())
        .queryParam("eartagNumber", "CH120030812345")
        .queryParam("recipientUid", "CHE123456789")
        .queryParam("dataRequestId", dataRequestId.toString())
        .when().get(DataTransferController.PATH + "/product/{productId}/data");
  }

  private List<ConsentRequestFundamentalViewEntity> legallyPermittedConsentRequestRows(UUID dataRequestId, Uid producerUid) {
    return entityManager.createQuery("""
            SELECT c FROM ConsentRequestFundamentalViewEntity c
            WHERE c.dataRequestId = :dataRequestId AND c.dataProducerUid = :uid AND c.stateCode = :stateCode
            """, ConsentRequestFundamentalViewEntity.class)
        .setParameter("dataRequestId", dataRequestId)
        .setParameter("uid", producerUid.name())
        .setParameter("stateCode", LEGALLY_PERMITTED)
        .getResultList();
  }
}
