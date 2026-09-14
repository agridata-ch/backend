package integration.agreement;

import static integration.testutils.TestUserEnum.CONSUMER_BIO_SUISSE;
import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.agreement.controller.DataRequestController;
import ch.agridata.agreement.dto.ConsentRequestStatusSummaryDto;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestDataIdentifiers;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class ConsentRequestStatusSummaryOfDataRequestTest {

  private final EntityManager entityManager;

  @Test
  void givenDataRequestWithUidAndBurBasedConsentRequests_whenRequestingStatusSummary_thenCountsAreSplitByMode() {
    // BIO_SUISSE_01 has: CHE101000001 (uid, DECLINED), CHE101000001/99910003 (bur, DECLINED),
    // CHE102000001 (uid, OPENED), CHE102000002 (uid, GRANTED).
    ConsentRequestStatusSummaryDto summary = AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .when().get(DataRequestController.PATH_V1 + "/" + TestDataIdentifiers.DataRequest.BIO_SUISSE_01 + "/consent-requests/status-summary")
        .then().statusCode(200)
        .extract().as(ConsentRequestStatusSummaryDto.class);

    assertThat(summary.uid()).isEqualTo(new ConsentRequestStatusSummaryDto.StateCountsDto(3, 1, 1, 1));
    assertThat(summary.bur()).isEqualTo(new ConsentRequestStatusSummaryDto.StateCountsDto(1, 0, 0, 1));
  }

  @Test
  void givenDataRequestWithOnlyUidBasedConsentRequests_whenRequestingStatusSummary_thenBurIsNull() {
    // BIO_SUISSE_02 has only uid-based consent requests: DECLINED, OPENED, GRANTED, OPENED.
    ConsentRequestStatusSummaryDto summary = AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .when().get(DataRequestController.PATH_V1 + "/" + TestDataIdentifiers.DataRequest.BIO_SUISSE_02 + "/consent-requests/status-summary")
        .then().statusCode(200)
        .extract().as(ConsentRequestStatusSummaryDto.class);

    assertThat(summary.uid()).isEqualTo(new ConsentRequestStatusSummaryDto.StateCountsDto(4, 2, 1, 1));
    assertThat(summary.bur()).isNull();
  }

  @Test
  void givenConsumer_whenRequestingStatusSummaryOfDataRequestOfDifferentConsumer_thenNotFound() {
    // IP_SUISSE_01 does not belong to the CONSUMER_BIO_SUISSE test user.
    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .when().get(DataRequestController.PATH_V1 + "/" + TestDataIdentifiers.DataRequest.IP_SUISSE_01 + "/consent-requests/status-summary")
        .then().statusCode(404);
  }

  @Test
  void givenConsentRequestStateChanges_whenRequestingStatusSummaryAgain_thenCountsReflectTheChange() {
    setConsentRequestState(TestDataIdentifiers.ConsentRequest.BIO_SUISSE_01_CHE102000001, "GRANTED");

    ConsentRequestStatusSummaryDto summary = AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .when().get(DataRequestController.PATH_V1 + "/" + TestDataIdentifiers.DataRequest.BIO_SUISSE_01 + "/consent-requests/status-summary")
        .then().statusCode(200)
        .extract().as(ConsentRequestStatusSummaryDto.class);

    assertThat(summary.uid().open()).isZero();
    assertThat(summary.uid().granted()).isEqualTo(2);
  }

  private void setConsentRequestState(TestDataIdentifiers.Identifier<?> consentRequestId, String stateCode) {
    QuarkusTransaction.requiringNew().run(() ->
        entityManager.createNativeQuery("UPDATE consent_request SET state_code = :stateCode WHERE id = :id")
            .setParameter("stateCode", stateCode)
            .setParameter("id", consentRequestId.uuid())
            .executeUpdate());
  }
}
