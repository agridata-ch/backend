package integration.agreement;

import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.agreement.service.ConsentRequestCleanupService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
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

  @BeforeEach
  void setUp() {
    // will make sure testdata is reset between tests
    flyway.migrate();
  }

  @Test
  void whenCleanupRuns_thenArchivesMatchingRows() {
    long terminated = consentRequestCleanupService.cleanupConsentRequestsFromYesterdayAndDayBefore();

    List<LocalDateTime> untilValues = em.createNativeQuery("""
        SELECT uid_bur_relation_until
                FROM consent_request
                WHERE data_producer_bur IN ('99910002', '99910003')
        """, LocalDateTime.class).getResultList();

    assertThat(untilValues)
        .isNotEmpty()
        .allSatisfy(v -> assertThat(v).isNotNull());

    assertThat(terminated).isGreaterThan(0);
  }
}
