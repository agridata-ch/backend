package integration.agreement;

import static integration.agreement.DataRequestTestFactory.setStatusAs;
import static integration.testutils.TestUserEnum.ADMIN;
import static integration.testutils.TestUserEnum.CONSUMER_BIO_SUISSE;
import static integration.testutils.TestUserEnum.PRODUCER_B;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

import ch.agridata.agreement.controller.ConsentRequestController;
import ch.agridata.agreement.dto.ConsentRequestStateEnum;
import ch.agridata.agreement.dto.DataRequestStateEnum;
import ch.agridata.auditing.api.ActionEnum;
import ch.agridata.auditing.api.EntityTypeEnum;
import ch.agridata.datatransferv2.controller.DataTransferController;
import com.github.tomakehurst.wiremock.client.WireMock;
import integration.auditing.utils.AuditLogTestUtils;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestDataIdentifiers.ConsentRequest;
import integration.testutils.TestDataIdentifiers.DataProduct;
import integration.testutils.TestDataIdentifiers.DataRequest;
import integration.testutils.TestDataIdentifiers.Uid;
import io.quarkiverse.wiremock.devservice.ConnectWireMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
@ConnectWireMock
class DataRequestPauseTest {

  private final Flyway flyway;
  private final AuditLogTestUtils auditLogTestUtils;

  WireMock wireMock;

  @BeforeEach
  void setUp() {
    flyway.migrate();
    wireMock.resetToDefaultMappings();
  }

  @Test
  void givenActiveDataRequest_whenAdminPauses_thenStateIsPausedAndAuditLogged() {
    setStatusAs(DataRequest.BIO_SUISSE_01.toString(), DataRequestStateEnum.PAUSED, ADMIN)
        .then().statusCode(200)
        .body("stateCode", equalTo(DataRequestStateEnum.PAUSED.name()));

    assertThat(auditLogTestUtils.getLatestAuditLogEntry()).satisfies(log -> {
      assertThat(log.getEntityTypeCode()).isEqualTo(EntityTypeEnum.DATA_REQUEST.name());
      assertThat(log.getEntityId()).isEqualTo(DataRequest.BIO_SUISSE_01.uuid());
      assertThat(log.getActionCode()).isEqualTo(ActionEnum.DATA_REQUEST_PAUSED.name());
    });
  }

  @Test
  void givenPausedDataRequest_whenAdminReactivates_thenStateIsActiveAndAuditLogged() {
    setStatusAs(DataRequest.BIO_SUISSE_01.toString(), DataRequestStateEnum.PAUSED, ADMIN).then().statusCode(200);

    setStatusAs(DataRequest.BIO_SUISSE_01.toString(), DataRequestStateEnum.ACTIVE, ADMIN)
        .then().statusCode(200)
        .body("stateCode", equalTo(DataRequestStateEnum.ACTIVE.name()));

    assertThat(auditLogTestUtils.getLatestAuditLogEntry()).satisfies(log -> {
      assertThat(log.getEntityTypeCode()).isEqualTo(EntityTypeEnum.DATA_REQUEST.name());
      assertThat(log.getEntityId()).isEqualTo(DataRequest.BIO_SUISSE_01.uuid());
      assertThat(log.getActionCode()).isEqualTo(ActionEnum.DATA_REQUEST_REACTIVATED.name());
    });
  }

  @Test
  void givenPausedDataRequest_whenConsumerReactivates_thenRejected() {
    setStatusAs(DataRequest.BIO_SUISSE_01.toString(), DataRequestStateEnum.PAUSED, ADMIN).then().statusCode(200);

    var statusCode = setStatusAs(DataRequest.BIO_SUISSE_01.toString(), DataRequestStateEnum.ACTIVE, CONSUMER_BIO_SUISSE)
        .then().extract().statusCode();

    assertThat(statusCode).isBetween(400, 499);
  }

  @Test
  void givenActiveDataRequest_whenAdminReactivates_thenRejected() {
    var statusCode = setStatusAs(DataRequest.BIO_SUISSE_01.toString(), DataRequestStateEnum.ACTIVE, ADMIN)
        .then().extract().statusCode();

    assertThat(statusCode).isBetween(400, 499);
  }

  @Test
  void givenActiveDataRequest_whenConsumerPauses_thenRejected() {
    var statusCode = setStatusAs(DataRequest.BIO_SUISSE_01.toString(), DataRequestStateEnum.PAUSED, CONSUMER_BIO_SUISSE)
        .then().extract().statusCode();

    assertThat(statusCode).isBetween(400, 499);
  }

  @Test
  void givenPausedDataRequest_whenAdminPausesAgain_thenRejected() {
    setStatusAs(DataRequest.BIO_SUISSE_01.toString(), DataRequestStateEnum.PAUSED, ADMIN).then().statusCode(200);

    var statusCode = setStatusAs(DataRequest.BIO_SUISSE_01.toString(), DataRequestStateEnum.PAUSED, ADMIN)
        .then().extract().statusCode();

    assertThat(statusCode).isBetween(400, 499);
  }

  @Test
  void givenPausedDataRequest_whenProductRequested_thenForbiddenAndNoUpstreamCall() {
    // CHE103000001 has granted consent only for BIO_SUISSE_02
    setStatusAs(DataRequest.BIO_SUISSE_02.toString(), DataRequestStateEnum.PAUSED, ADMIN).then().statusCode(200);

    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .pathParam("productId", DataProduct.UUID_085E4B72.uuid())
        .queryParam("uid", Uid.CHE103000001.name())
        .queryParam("year", 2024)
        .when().get(DataTransferController.PATH + "/product/{productId}/data")
        .then().statusCode(403);

    wireMock.verifyThat(0, WireMock.anyRequestedFor(WireMock.urlPathMatching(".*/agis/structure-data/.*")));
  }

  @Test
  void givenPausedDataRequest_whenProducerGrantsConsent_thenSucceeds() {
    setStatusAs(DataRequest.BIO_SUISSE_01.toString(), DataRequestStateEnum.PAUSED, ADMIN).then().statusCode(200);

    AuthTestUtils.requestAs(PRODUCER_B).contentType(ContentType.JSON)
        .body(String.format("\"%s\"", ConsentRequestStateEnum.GRANTED))
        .when().put(ConsentRequestController.PATH + "/" + ConsentRequest.BIO_SUISSE_01_CHE102000001.uuid() + "/status")
        .then().statusCode(204);
  }
}
