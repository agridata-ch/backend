package integration.agreement;

import static integration.agreement.DataRequestTestFactory.setStatusAs;
import static integration.testutils.TestUserEnum.ADMIN;
import static integration.testutils.TestUserEnum.CONSUMER_BIO_SUISSE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;

import ch.agridata.agreement.controller.DataRequestController;
import ch.agridata.agreement.dto.DataRequestStateEnum;
import ch.agridata.agreement.job.DataRequestTerminationJob;
import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.agreement.service.DataRequestStateService;
import ch.agridata.agreement.service.DataRequestTerminationRunner;
import ch.agridata.auditing.api.ActionEnum;
import ch.agridata.common.security.SystemActorEnum;
import com.github.tomakehurst.wiremock.client.WireMock;
import integration.auditing.utils.AuditLogTestUtils;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestDataIdentifiers.DataRequest;
import integration.testutils.TestDataIdentifiers.Identifier;
import io.quarkiverse.wiremock.devservice.ConnectWireMock;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import jakarta.persistence.EntityManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
@ConnectWireMock
class DataRequestTerminationRunTest {

  /** Data requests in the seed data (afterMigrate__testData.sql) that are already past their termination period. */
  private static final int SEEDED_EXPIRED = 1;

  private static final String TERMINATION_PATH = DataRequestController.PATH_V1 + "/termination";

  private final Flyway flyway;
  private final EntityManager entityManager;
  private final DataSource dataSource;
  private final AuditLogTestUtils auditLogTestUtils;
  private final DataRequestTerminationJob job;

  WireMock wireMock;

  @InjectSpy
  DataRequestStateService dataRequestStateService;

  @BeforeEach
  void setUp() {
    flyway.migrate();
    wireMock.resetToDefaultMappings();
  }

  @Test
  void givenExpiredTerminationPeriod_whenTriggered_thenTerminatedAndAuditedAsSystem() {
    terminationPendingUntil(DataRequest.BIO_SUISSE_01, LocalDate.now().minusDays(1));

    AuthTestUtils.requestAs(ADMIN).when().post(TERMINATION_PATH)
        .then().statusCode(200)
        .body("terminatedCount", equalTo(SEEDED_EXPIRED + 1))
        .body("failedCount", equalTo(0));

    assertThat(stateOf(DataRequest.BIO_SUISSE_01)).isEqualTo(DataRequestStateEnum.TERMINATED.name());
    // the seeded expired request is terminated in the same run, so the audit entry is one of the latest entries
    assertThat(List.of(auditLogTestUtils.getLatestAuditLogEntry(0), auditLogTestUtils.getLatestAuditLogEntry(1)))
        .filteredOn(log -> DataRequest.BIO_SUISSE_01.uuid().equals(log.getEntityId()))
        .singleElement()
        .satisfies(log -> {
          assertThat(log.getActionCode()).isEqualTo(ActionEnum.DATA_REQUEST_TERMINATED.name());
          assertThat(log.getActorId()).isEqualTo(SystemActorEnum.DATA_REQUEST_TERMINATION_JOB.getLoggingActorId());
        });
  }

  @Test
  void givenExpiredTerminationPeriod_whenScheduledJobRuns_thenTerminated() {
    terminationPendingUntil(DataRequest.BIO_SUISSE_01, LocalDate.now().minusDays(1));

    job.run();

    assertThat(stateOf(DataRequest.BIO_SUISSE_01)).isEqualTo(DataRequestStateEnum.TERMINATED.name());
  }

  @Test
  void givenTerminationPeriodEndingToday_whenTriggered_thenNotTerminatedBecauseLastDayIsInclusive() {
    terminationPendingUntil(DataRequest.BIO_SUISSE_01, LocalDate.now());

    AuthTestUtils.requestAs(ADMIN).when().post(TERMINATION_PATH)
        .then().statusCode(200)
        .body("terminatedCount", equalTo(SEEDED_EXPIRED));

    assertThat(stateOf(DataRequest.BIO_SUISSE_01)).isEqualTo(DataRequestStateEnum.TERMINATION_PENDING.name());
  }

  @Test
  void givenRunningTerminationPeriod_whenTriggered_thenNotTerminated() {
    terminationPendingUntil(DataRequest.BIO_SUISSE_01, LocalDate.now().plusDays(1));

    AuthTestUtils.requestAs(ADMIN).when().post(TERMINATION_PATH)
        .then().statusCode(200)
        .body("terminatedCount", equalTo(SEEDED_EXPIRED));

    assertThat(stateOf(DataRequest.BIO_SUISSE_01)).isEqualTo(DataRequestStateEnum.TERMINATION_PENDING.name());
  }

  @Test
  void givenTerminationRunning_whenTerminationTriggered_thenConflict() throws SQLException {
    try (var connection = dataSource.getConnection(); var statement = connection.prepareStatement("select pg_advisory_lock(?)")) {
      statement.setLong(1, DataRequestTerminationRunner.TERMINATION_LOCK_KEY);
      statement.execute();

      AuthTestUtils.requestAs(ADMIN).when().post(TERMINATION_PATH).then().statusCode(409);
    }
  }

  @Test
  void givenOneRequestFails_whenTriggered_thenOthersAreStillTerminated() {
    terminationPendingUntil(DataRequest.BIO_SUISSE_01, LocalDate.now().minusDays(1));
    terminationPendingUntil(DataRequest.BIO_SUISSE_02, LocalDate.now().minusDays(1));
    doThrow(new IllegalStateException("simulated failure"))
        .when(dataRequestStateService)
        .transitionToTerminated(argThat(entity -> entity != null && DataRequest.BIO_SUISSE_01.uuid().equals(entity.getId())));

    AuthTestUtils.requestAs(ADMIN).when().post(TERMINATION_PATH)
        .then().statusCode(200)
        .body("terminatedCount", equalTo(SEEDED_EXPIRED + 1))
        .body("failedCount", equalTo(1));

    assertThat(stateOf(DataRequest.BIO_SUISSE_01)).isEqualTo(DataRequestStateEnum.TERMINATION_PENDING.name());
    assertThat(stateOf(DataRequest.BIO_SUISSE_02)).isEqualTo(DataRequestStateEnum.TERMINATED.name());
  }

  private void terminationPendingUntil(Identifier<DataRequestEntity> dataRequest, LocalDate until) {
    setStatusAs(dataRequest.toString(), DataRequestStateEnum.TERMINATION_PENDING, CONSUMER_BIO_SUISSE).then().statusCode(200);
    QuarkusTransaction.requiringNew().run(
        () -> entityManager.createNativeQuery("update data_request set termination_period_until = :until where id = :id")
            .setParameter("until", until)
            .setParameter("id", dataRequest.uuid())
            .executeUpdate()
    );
  }

  private String stateOf(Identifier<DataRequestEntity> dataRequest) {
    return (String) QuarkusTransaction.requiringNew().call(
        () -> entityManager.createNativeQuery("select state_code from data_request where id = :id")
            .setParameter("id", dataRequest.uuid())
            .getSingleResult()
    );
  }
}
