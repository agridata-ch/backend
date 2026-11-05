package integration.agreement;

import static integration.agreement.DataRequestTestFactory.setStatusAs;
import static integration.testutils.TestUserEnum.ADMIN;
import static integration.testutils.TestUserEnum.CONSUMER_BIO_SUISSE;
import static integration.testutils.TestUserEnum.CONSUMER_IP_SUISSE;
import static integration.testutils.TestUserEnum.CONSUMER_PROVIDER;
import static integration.testutils.TestUserEnum.PROVIDER_1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import ch.agridata.agreement.dto.DataRequestStateEnum;
import ch.agridata.auditing.api.ActionEnum;
import ch.agridata.auditing.api.EntityTypeEnum;
import ch.agridata.datatransferv2.controller.DataTransferController;
import com.github.tomakehurst.wiremock.client.WireMock;
import integration.auditing.utils.AuditLogTestUtils;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestDataIdentifiers.DataProduct;
import integration.testutils.TestDataIdentifiers.DataRequest;
import integration.testutils.TestDataIdentifiers.Uid;
import io.quarkiverse.wiremock.devservice.ConnectWireMock;
import io.quarkus.test.junit.QuarkusTest;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
@ConnectWireMock
class DataRequestTerminationTest {

  private final Flyway flyway;
  private final AuditLogTestUtils auditLogTestUtils;

  WireMock wireMock;

  @BeforeEach
  void setUp() {
    flyway.migrate();
    wireMock.resetToDefaultMappings();
  }

  @Test
  void givenActiveDataRequest_whenConsumerTerminates_thenPendingWithTerminationPeriodUntilAndAuditLogged() {
    var expectedTerminationPeriodUntil = LocalDate.now().plusMonths(3).toString();

    setStatusAs(DataRequest.BIO_SUISSE_01.toString(), DataRequestStateEnum.TERMINATION_PENDING, CONSUMER_BIO_SUISSE)
        .then().statusCode(200)
        .body("stateCode", equalTo(DataRequestStateEnum.TERMINATION_PENDING.name()))
        .body("terminationPeriodUntil", equalTo(expectedTerminationPeriodUntil));

    assertThat(auditLogTestUtils.getLatestAuditLogEntry()).satisfies(log -> {
      assertThat(log.getEntityTypeCode()).isEqualTo(EntityTypeEnum.DATA_REQUEST.name());
      assertThat(log.getEntityId()).isEqualTo(DataRequest.BIO_SUISSE_01.uuid());
      assertThat(log.getActionCode()).isEqualTo(ActionEnum.DATA_REQUEST_TERMINATION_PERIOD_STARTED.name());
    });
  }

  @Test
  void givenActiveDataRequest_whenProviderTerminates_thenPendingWithTerminationPeriodUntilAndAuditLogged() {
    setStatusAs(DataRequest.IP_SUISSE_01.toString(), DataRequestStateEnum.TERMINATION_PENDING, PROVIDER_1)
        .then().statusCode(200)
        .body("stateCode", equalTo(DataRequestStateEnum.TERMINATION_PENDING.name()))
        .body("terminationPeriodUntil", notNullValue());

    assertThat(auditLogTestUtils.getLatestAuditLogEntry()).satisfies(log -> {
      assertThat(log.getEntityId()).isEqualTo(DataRequest.IP_SUISSE_01.uuid());
      assertThat(log.getActionCode()).isEqualTo(ActionEnum.DATA_REQUEST_TERMINATION_PERIOD_STARTED.name());
    });
  }

  @Test
  void givenTerminationPendingDataRequest_whenProductRequested_thenDataExchangeStillAllowed() {
    // CHE103000001 has granted consent only for BIO_SUISSE_02
    setStatusAs(DataRequest.BIO_SUISSE_02.toString(), DataRequestStateEnum.TERMINATION_PENDING, CONSUMER_BIO_SUISSE)
        .then().statusCode(200);

    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .pathParam("productId", DataProduct.UUID_085E4B72.uuid())
        .queryParam("uid", Uid.CHE103000001.name())
        .queryParam("year", 2024)
        .when().get(DataTransferController.PATH + "/product/{productId}/data")
        .then().statusCode(200);
  }

  @Test
  void givenTerminationPendingDataRequest_whenProviderTerminatesAgain_thenRejected() {
    setStatusAs(DataRequest.IP_SUISSE_01.toString(), DataRequestStateEnum.TERMINATION_PENDING, PROVIDER_1).then().statusCode(200);

    var statusCode = setStatusAs(DataRequest.IP_SUISSE_01.toString(), DataRequestStateEnum.TERMINATION_PENDING, PROVIDER_1)
        .then().extract().statusCode();

    assertThat(statusCode).isBetween(400, 499);
  }

  @Test
  void givenTerminationPendingDataRequest_whenConsumerReactivates_thenRejected() {
    setStatusAs(DataRequest.BIO_SUISSE_01.toString(), DataRequestStateEnum.TERMINATION_PENDING, CONSUMER_BIO_SUISSE)
        .then().statusCode(200);

    var statusCode = setStatusAs(DataRequest.BIO_SUISSE_01.toString(), DataRequestStateEnum.ACTIVE, CONSUMER_BIO_SUISSE)
        .then().extract().statusCode();

    assertThat(statusCode).isBetween(400, 499);
  }

  @Test
  void givenTerminationPendingDataRequest_whenAdminReactivates_thenRejected() {
    setStatusAs(DataRequest.BIO_SUISSE_01.toString(), DataRequestStateEnum.TERMINATION_PENDING, CONSUMER_BIO_SUISSE)
        .then().statusCode(200);

    var statusCode = setStatusAs(DataRequest.BIO_SUISSE_01.toString(), DataRequestStateEnum.ACTIVE, ADMIN)
        .then().extract().statusCode();

    assertThat(statusCode).isBetween(400, 499);
  }

  @Test
  void givenActiveDataRequest_whenAdminTerminates_thenRejected() {
    var statusCode = setStatusAs(DataRequest.BIO_SUISSE_01.toString(), DataRequestStateEnum.TERMINATION_PENDING, ADMIN)
        .then().extract().statusCode();

    assertThat(statusCode).isBetween(400, 499);
  }

  @Test
  void givenActiveDataRequest_whenOtherConsumerTerminates_thenRejected() {
    var statusCode = setStatusAs(DataRequest.BIO_SUISSE_01.toString(), DataRequestStateEnum.TERMINATION_PENDING, CONSUMER_IP_SUISSE)
        .then().extract().statusCode();

    assertThat(statusCode).isBetween(400, 499);
  }

  @Test
  void givenActiveDataRequest_whenProviderOfOtherDataSourceSystemTerminates_thenRejected() {
    var statusCode = setStatusAs(DataRequest.BLV_1.toString(), DataRequestStateEnum.TERMINATION_PENDING, PROVIDER_1)
        .then().extract().statusCode();

    assertThat(statusCode).isEqualTo(404);
  }

  @Test
  void givenActiveDataRequest_whenOtherProviderTerminates_thenRejected() {
    var statusCode = setStatusAs(DataRequest.IP_SUISSE_01.toString(), DataRequestStateEnum.TERMINATION_PENDING, CONSUMER_PROVIDER)
        .then().extract().statusCode();

    assertThat(statusCode).isBetween(400, 499);
  }
}
