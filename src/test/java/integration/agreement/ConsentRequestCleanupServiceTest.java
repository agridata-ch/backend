package integration.agreement;

import static ch.agridata.auditing.api.ActionEnum.CONSENT_REQUEST_TERMINATED;
import static ch.agridata.auditing.api.EntityTypeEnum.CONSENT_REQUEST;
import static ch.agridata.auditing.api.SystemActorEnum.CONSENT_REQUEST_CLEANUP_JOB;
import static ch.agridata.auditing.persistence.AuditLogEntity.ActorTypeEnum.SYSTEM;
import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.agreement.service.ConsentRequestCleanupService;
import ch.agridata.auditing.persistence.AuditLogEntity;
import integration.auditing.utils.AuditLogTestUtils;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class ConsentRequestCleanupServiceTest {

  private final Flyway flyway;
  private final ConsentRequestCleanupService consentRequestCleanupService;
  private final EntityManager em;
  private final AuditLogTestUtils auditLogTestUtils;

  @BeforeEach
  void setUp() {
    // will make sure testdata is reset between tests
    flyway.migrate();
  }

  @Test
  void whenCleanupRuns_thenArchivesMatchingRowsAndWritesAuditLogs() {
    long terminated = consentRequestCleanupService.cleanupConsentRequestsFromYesterdayAndDayBefore();

    List<Object[]> rows = em.createNativeQuery("""
            SELECT id, uid_bur_relation_until
            FROM consent_request
            WHERE data_producer_bur IN ('99910002', '99910003')
            ORDER BY id
            """)
        .getResultList();

    assertThat(rows)
        .hasSize(3)
        .allSatisfy(row -> {
          assertThat(row[0]).isInstanceOf(UUID.class);
          assertThat(row[1]).isInstanceOf(LocalDateTime.class);
        });

    List<UUID> affectedIds = rows.stream()
        .map(row -> (UUID) row[0])
        .toList();

    assertThat(terminated).isEqualTo(3);

    var latestLogs = List.of(
        auditLogTestUtils.getLatestAuditLogEntry(),
        auditLogTestUtils.getLatestAuditLogEntry(1),
        auditLogTestUtils.getLatestAuditLogEntry(2)
    );

    assertThat(latestLogs)
        .doesNotContainNull()
        .allSatisfy(this::assertTerminationAuditEntry);

    assertThat(latestLogs.stream()
        .map(AuditLogEntity::getEntityId)
        .toList())
        .containsExactlyInAnyOrderElementsOf(affectedIds);

    assertThat(auditLogTestUtils.getLatestAuditLogEntry(3)).isNull();
  }

  private void assertTerminationAuditEntry(AuditLogEntity log) {
    assertThat(log.getEntityTypeCode()).isEqualTo(CONSENT_REQUEST.name());
    assertThat(log.getActionCode()).isEqualTo(CONSENT_REQUEST_TERMINATED.name());
    assertThat(log.getRequestId()).isNull();
    assertThat(log.getActorId()).isEqualTo(CONSENT_REQUEST_CLEANUP_JOB.getActorId());
    assertThat(log.getActorTypeCode()).isEqualTo(SYSTEM);
  }
}
