package integration.agreement;

import static ch.agridata.agreement.service.ConsentRequestCleanupRunner.USER_ID_SCHEDULED_CLEANUP_JOB;
import static ch.agridata.auditing.api.ActionEnum.CONSENT_REQUEST_CLEANUP_TRIGGERED;
import static ch.agridata.auditing.api.ActionEnum.CONSENT_REQUEST_TERMINATED;
import static ch.agridata.auditing.api.EntityTypeEnum.CONSENT_REQUEST;
import static ch.agridata.auditing.persistence.AuditLogEntity.ActorTypeEnum.USER;
import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.agreement.controller.ConsentRequestController;
import ch.agridata.agreement.dto.ConsentRequestCleanupResultDto;
import ch.agridata.auditing.persistence.AuditLogEntity;
import ch.agridata.auditing.persistence.AuditLogRepository;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestUserEnum;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for the admin endpoint that triggers the consent request cleanup manually.
 * Verifies that the cleanup actually runs, that the result is reported back, and that the
 * triggering administrator is captured in the audit log.
 *
 * @CommentLastReviewed 2026-09-07
 */

@QuarkusTest
@RequiredArgsConstructor
class ConsentRequestCleanupEndpointTest {

  private static final String CLEANUP_PATH = ConsentRequestController.PATH + "/cleanup";

  private final EntityManager em;
  private final AuditLogRepository auditLogRepository;

  @Test
  void givenAdmin_whenTriggerCleanup_thenTerminatesConsentRequestsAndReportsResult() {
    var dateTimeBeforeTermination = LocalDateTime.now();

    var result = AuthTestUtils.requestAs(TestUserEnum.ADMIN)
        .when().post(CLEANUP_PATH)
        .then().statusCode(200)
        .extract().as(ConsentRequestCleanupResultDto.class);

    var today = LocalDate.now();
    assertThat(result.fromInclusive()).isEqualTo(today.minusDays(2));
    assertThat(result.toInclusive()).isEqualTo(today.minusDays(1));
    assertThat(result.terminatedConsentRequestCount()).isEqualTo(8L);
    assertThat(result.durationMs()).isNotNegative();

    List<Object[]> rows = em.createNativeQuery("""
            SELECT id, uid_bur_relation_until, modified_by
            FROM consent_request
            WHERE data_producer_bur IN ('99910002', '99910003')
            ORDER BY id
            """)
        .getResultList();

    assertThat(rows)
        .hasSize(8)
        .allSatisfy(row -> {
          assertThat(((LocalDateTime) row[1]).isAfter(dateTimeBeforeTermination)).isTrue();
          assertThat(row[2]).isEqualTo(USER_ID_SCHEDULED_CLEANUP_JOB);
        });
  }

  @Test
  void givenAdmin_whenTriggerCleanup_thenAuditsTheTriggeringAdminAndTheTerminations() {
    AuthTestUtils.requestAs(TestUserEnum.ADMIN)
        .when().post(CLEANUP_PATH)
        .then().statusCode(200);

    var triggerEntries = auditLogRepository
        .find("actionCode", CONSENT_REQUEST_CLEANUP_TRIGGERED.name())
        .list();

    assertThat(triggerEntries).hasSize(1);
    assertThat(triggerEntries.getFirst().getActorTypeCode()).isEqualTo(USER);
    assertThat(triggerEntries.getFirst().getActorId()).isNotEqualTo(USER_ID_SCHEDULED_CLEANUP_JOB.toString());
    assertThat(triggerEntries.getFirst().getEntityTypeCode()).isEqualTo(CONSENT_REQUEST.name());
    assertThat(triggerEntries.getFirst().getEntityId()).isNull();
    assertThat(triggerEntries.getFirst().getRequestId()).isNotNull();

    var terminationEntries = auditLogRepository
        .find("actionCode", CONSENT_REQUEST_TERMINATED.name())
        .list();

    assertThat(terminationEntries).hasSize(8);
    assertThat(terminationEntries)
        .extracting(AuditLogEntity::getEntityId)
        .doesNotContainNull();
  }

  @Test
  void givenAdminAndExplicitWindow_whenTriggerCleanup_thenUsesThatWindowInsteadOfTheDefault() {
    LocalDate from = LocalDate.of(2000, 1, 1);
    LocalDate to = LocalDate.of(2000, 1, 5);

    var result = AuthTestUtils.requestAs(TestUserEnum.ADMIN)
        .when().post(CLEANUP_PATH + "?fromInclusive=" + from + "&toInclusive=" + to)
        .then().statusCode(200)
        .extract().as(ConsentRequestCleanupResultDto.class);

    assertThat(result.fromInclusive()).isEqualTo(from);
    assertThat(result.toInclusive()).isEqualTo(to);
  }

  @Test
  void givenAdminAndReversedWindow_whenTriggerCleanup_thenReturns400AndTerminatesNothing() {
    LocalDate from = LocalDate.of(2000, 1, 5);
    LocalDate to = LocalDate.of(2000, 1, 1);

    AuthTestUtils.requestAs(TestUserEnum.ADMIN)
        .when().post(CLEANUP_PATH + "?fromInclusive=" + from + "&toInclusive=" + to)
        .then().statusCode(400);

    var terminationEntries = auditLogRepository
        .find("actionCode", CONSENT_REQUEST_TERMINATED.name())
        .list();

    assertThat(terminationEntries).isEmpty();
  }

  @Test
  void givenAdminAndCleanupAlreadyRan_whenTriggerCleanupAgain_thenTerminatesNothingMore() {
    AuthTestUtils.requestAs(TestUserEnum.ADMIN)
        .when().post(CLEANUP_PATH)
        .then().statusCode(200);

    var secondResult = AuthTestUtils.requestAs(TestUserEnum.ADMIN)
        .when().post(CLEANUP_PATH)
        .then().statusCode(200)
        .extract().as(ConsentRequestCleanupResultDto.class);

    assertThat(secondResult.terminatedConsentRequestCount()).isZero();
  }
}
