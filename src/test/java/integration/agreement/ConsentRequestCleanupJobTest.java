package integration.agreement;

import static ch.agridata.agreement.job.ConsentRequestCleanupJob.USER_ID_SCHEDULED_CLEANUP_JOB;
import static ch.agridata.auditing.api.ActionEnum.CONSENT_REQUEST_TERMINATED;
import static ch.agridata.auditing.api.EntityTypeEnum.CONSENT_REQUEST;
import static ch.agridata.auditing.api.SystemActorEnum.CONSENT_REQUEST_CLEANUP_JOB;
import static ch.agridata.auditing.persistence.AuditLogEntity.ActorTypeEnum.SYSTEM;
import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.agreement.job.ConsentRequestCleanupJob;
import ch.agridata.auditing.persistence.AuditLogEntity;
import integration.auditing.utils.AuditLogTestUtils;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class ConsentRequestCleanupJobTest {

  private final ConsentRequestCleanupJob consentRequestCleanupJob;
  private final EntityManager em;
  private final AuditLogTestUtils auditLogTestUtils;

  @Test
  void whenCleanupRuns_thenArchivesMatchingRowsAndWritesAuditLogs() {
    var dateTimeBeforeTermination = LocalDateTime.now();

    consentRequestCleanupJob.run();

    List<Object[]> rows = em.createNativeQuery("""
            SELECT id, uid_bur_relation_until, modified_at, modified_by
            FROM consent_request
            WHERE data_producer_bur IN ('99910002', '99910003')
            ORDER BY id
            """)
        .getResultList();

    assertThat(rows)
        .hasSize(6)
        .allSatisfy(row -> {
          assertThat(row[0]).isInstanceOf(UUID.class);
          assertThat(((LocalDateTime) row[1]).isAfter(dateTimeBeforeTermination));
          assertThat(((LocalDateTime) row[2]).isEqual((LocalDateTime) row[1]));
          assertThat(row[3].equals(USER_ID_SCHEDULED_CLEANUP_JOB));
        });

    List<UUID> affectedIds = rows.stream()
        .map(row -> (UUID) row[0])
        .toList();

    var latestLogs = List.of(
        auditLogTestUtils.getLatestAuditLogEntry(),
        auditLogTestUtils.getLatestAuditLogEntry(1),
        auditLogTestUtils.getLatestAuditLogEntry(2),
        auditLogTestUtils.getLatestAuditLogEntry(3),
        auditLogTestUtils.getLatestAuditLogEntry(4),
        auditLogTestUtils.getLatestAuditLogEntry(5)
    );

    assertThat(latestLogs)
        .doesNotContainNull()
        .allSatisfy(this::assertTerminationAuditEntry);

    assertThat(latestLogs.stream()
        .map(AuditLogEntity::getEntityId)
        .toList())
        .containsExactlyInAnyOrderElementsOf(affectedIds);

    assertThat(auditLogTestUtils.getLatestAuditLogEntry(6)).isNull();
  }

  private void assertTerminationAuditEntry(AuditLogEntity log) {
    assertThat(log.getEntityTypeCode()).isEqualTo(CONSENT_REQUEST.name());
    assertThat(log.getActionCode()).isEqualTo(CONSENT_REQUEST_TERMINATED.name());
    assertThat(log.getRequestId()).isNull();
    assertThat(log.getActorId()).isEqualTo(CONSENT_REQUEST_CLEANUP_JOB.getActorId());
    assertThat(log.getActorTypeCode()).isEqualTo(SYSTEM);
  }
}
