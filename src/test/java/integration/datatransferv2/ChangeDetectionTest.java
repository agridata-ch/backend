package integration.datatransferv2;

import static integration.testutils.TestDataIdentifiers.ConsentRequest.BIO_SUISSE_01_CHE101000001;
import static integration.testutils.TestDataIdentifiers.ConsentRequest.BIO_SUISSE_01_CHE102000001;
import static integration.testutils.TestDataIdentifiers.DataProduct.UUID_C661EA48;
import static integration.testutils.TestDataIdentifiers.Uid.CHE101000001;
import static integration.testutils.TestDataIdentifiers.Uid.CHE102000001;
import static integration.testutils.TestDataIdentifiers.Uid.CHE103000001;
import static integration.testutils.TestUserEnum.CONSUMER_BIO_SUISSE;
import static integration.testutils.TestUserEnum.PRODUCER_A;
import static integration.testutils.TestUserEnum.PRODUCER_B;
import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.agreement.controller.ConsentRequestController;
import ch.agridata.agreement.dto.ConsentRequestStateEnum;
import ch.agridata.datatransferv2.controller.DataTransferController;
import ch.agridata.datatransferv2.dto.ProducerIdentifier;
import ch.agridata.product.persistence.DataProductEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestDataIdentifiers.Identifier;
import io.quarkiverse.wiremock.devservice.ConnectWireMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for the change detection endpoint. Tests the logic that returns producer IDs for which either a new
 * consent was granted or data has changed at the upstream provider.
 *
 * <p>Product under test: c661ea48 (R01 person data, UID_BASED_PRE_VALIDATION, AGIS change detection)
 * <p>Consumer: CONSUMER_BIO_SUISSE (data request BIO_SUISSE_01)
 * <p>Available consent requests in test data:
 * <ul>
 *   <li>BIO_SUISSE_01_CHE102000001 (OPENED) – PRODUCER_B can grant</li>
 *   <li>BIO_SUISSE_01_CHE101000001 (DECLINED) – PRODUCER_A can reopen and grant</li>
 * </ul>
 *
 * @CommentLastReviewed 2026-03-18
 */
@QuarkusTest
@RequiredArgsConstructor
@ConnectWireMock
class ChangeDetectionTest {

  private static final Identifier<DataProductEntity> PRODUCT = UUID_C661EA48;

  private final Flyway flyway;
  private final ObjectMapper objectMapper;

  WireMock wireMock;

  @BeforeEach
  void setUp() {
    flyway.migrate();
    wireMock.resetToDefaultMappings();
  }

  @Test
  void givenNoGrantedConsents_whenGetModifiedProducers_thenReturnsEmpty() {
    // Initial state: CHE102000001 is OPENED (lastStateChangeDate=null → excluded from "all granted")
    mockProviderReturning(List.of(CHE102000001.name()));

    var result = getModifiedProducers("1970-01-01");

    assertThat(result).isEmpty();
  }

  @Test
  void givenNewlyGrantedConsent_andNoProviderChanges_thenNewConsentIsReturned() {
    grantConsent(BIO_SUISSE_01_CHE102000001, PRODUCER_B);
    mockProviderReturning(List.of());

    var result = getModifiedProducers("1970-01-01");

    assertThat(result).containsExactly(new ProducerIdentifier(CHE102000001.name(), null));
  }

  @Test
  void givenGrantedConsentAndProviderChange_whenSinceInFuture_thenProviderChangeIsReturned() {
    // Grant CHE102000001 so it appears in "all granted since EPOCH"
    // Use a future since so it does NOT appear as "new consent"
    // → the UID must come from the provider-changes channel
    grantConsent(BIO_SUISSE_01_CHE102000001, PRODUCER_B);
    mockProviderReturning(List.of(CHE102000001.name()));

    var result = getModifiedProducers("2099-01-01");

    assertThat(result).containsExactly(new ProducerIdentifier(CHE102000001.name(), null));
  }

  @Test
  void givenProviderChangeForUidWithoutConsent_thenUidIsNotReturned() {
    // CHE103000001 has no consent for BIO_SUISSE_01, so even though the provider reports a change, it is excluded
    grantConsent(BIO_SUISSE_01_CHE102000001, PRODUCER_B);
    mockProviderReturning(List.of(CHE103000001.name()));

    var result = getModifiedProducers("2099-01-01");

    assertThat(result).isEmpty();
  }

  @Test
  void givenDeclinedConsent_andProviderReportsChange_thenUidIsNotReturned() {
    grantConsent(BIO_SUISSE_01_CHE102000001, PRODUCER_B);
    updateConsentStatus(BIO_SUISSE_01_CHE102000001, PRODUCER_B, ConsentRequestStateEnum.DECLINED);
    mockProviderReturning(List.of(CHE102000001.name()));

    var result = getModifiedProducers("1970-01-01");

    assertThat(result).isEmpty();
  }

  @Test
  void givenTwoGrantedConsents_andProviderReportsOnlyOne_thenBothChannelsMergedCorrectly() {
    // CHE101000001: currently DECLINED → GRANTED
    // CHE102000001: currently OPENED → GRANTED
    // Provider reports only CHE101000001 as changed data
    // → CHE101000001 comes from provider channel, CHE102000001 comes from new-consent channel
    grantConsent(BIO_SUISSE_01_CHE101000001, PRODUCER_A);
    grantConsent(BIO_SUISSE_01_CHE102000001, PRODUCER_B);
    mockProviderReturning(List.of(CHE101000001.name()));

    var result = getModifiedProducers("1970-01-01");

    assertThat(result).extracting(ProducerIdentifier::uid)
        .containsExactlyInAnyOrder(CHE101000001.name(), CHE102000001.name());
  }

  @Test
  void givenProviderReturnsAlreadyKnownUid_thenNoDuplicateInResult() {
    // CHE102000001 is both a new consent and reported by the provider → should appear exactly once
    grantConsent(BIO_SUISSE_01_CHE102000001, PRODUCER_B);
    mockProviderReturning(List.of(CHE102000001.name()));

    var result = getModifiedProducers("1970-01-01");

    assertThat(result).hasSize(1);
    assertThat(result).containsExactly(new ProducerIdentifier(CHE102000001.name(), null));
  }

  private List<ProducerIdentifier> getModifiedProducers(String since) {
    return AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .queryParam("since", since)
        .when().get(DataTransferController.PATH + "/product/" + PRODUCT + "/modified-producers")
        .then().statusCode(200)
        .extract().as(new TypeRef<>() {
        });
  }

  private void grantConsent(integration.testutils.TestDataIdentifiers.Identifier<?> consentId,
                            integration.testutils.TestUserEnum producer) {
    updateConsentStatus(consentId, producer, ConsentRequestStateEnum.GRANTED);
  }

  private void updateConsentStatus(integration.testutils.TestDataIdentifiers.Identifier<?> consentId,
                                   integration.testutils.TestUserEnum producer,
                                   ConsentRequestStateEnum state) {
    AuthTestUtils.requestAs(producer).contentType(ContentType.JSON)
        .body(String.format("\"%s\"", state))
        .when().put(ConsentRequestController.PATH + "/" + consentId + "/status")
        .then().statusCode(204);
  }

  @SneakyThrows
  private void mockProviderReturning(List<String> uids) {
    wireMock.register(WireMock.get(WireMock.urlPathMatching(".*/register-data/.*"))
        .willReturn(WireMock.aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(uids))));
  }
}
