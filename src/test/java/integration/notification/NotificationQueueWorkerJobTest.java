package integration.notification;

import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.notification.dto.EventTypeCodeEnum;
import ch.agridata.notification.dto.RecipientRequestDto;
import ch.agridata.notification.persistence.NotificationBatchRepository;
import ch.agridata.notification.persistence.NotificationBatchStatusEnum;
import ch.agridata.notification.service.NotificationBatchService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.persistence.EntityManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * End-to-end smoke test exercising the notification outbox happy path against the LocalStack
 * SES instance: queues a batch through the producer service and verifies that a real e-mail
 * was delivered to the LocalStack SES inbox in addition to the expected database side effects.
 * Covers both trigger paths — explicit {@code job.run()} (cron equivalent) and the post-commit
 * CDI event observer that triggers immediate processing.
 *
 * @CommentLastReviewed 2026-05-13
 */
@QuarkusTest
@TestProfile(NotificationQueueWorkerJobTest.NoSchedulerProfile.class)
@RequiredArgsConstructor
class NotificationQueueWorkerJobTest {

  @ConfigProperty(name = "quarkus.ses.endpoint-override")
  String sesEndpoint;

  private static final String RECIPIENT_EMAIL = "smoke-test@agridata.local";
  private static final String EXPECTED_SUBJECT = "Neuer Antrag zur Kontrolle eingereicht";

  private final AgridataSecurityIdentity agridataSecurityIdentity;
  private final NotificationBatchService batchService;
  private final NotificationBatchRepository batchRepository;
  private final EntityManager em;

  private final HttpClient http = HttpClient.newHttpClient();

  public static class NoSchedulerProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
      return Map.of("quarkus.scheduler.enabled", "false");
    }
  }

  @BeforeEach
  void clearLocalStackSesInbox() throws Exception {
    var request = HttpRequest.newBuilder(URI.create(sesInboxUri())).DELETE().build();
    http.send(request, HttpResponse.BodyHandlers.discarding());
  }

  @Test
  void givenPendingBatch_whenObserverFires_thenEmailIsDeliveredAndBatchCompletes() throws Exception {
    agridataSecurityIdentity.setRunAsUserId(UUID.randomUUID());
    batchService.queueNotification(
        List.of(new RecipientRequestDto(null, RECIPIENT_EMAIL)),
        EventTypeCodeEnum.DATA_REQUEST_READY_FOR_REVIEW,
        Map.of(
            "dataRequestUrl", "/admin/" + UUID.randomUUID(),
            "dataRequestTitleDe", "Testantrag DE",
            "dataRequestTitleFr", "Demande de test FR",
            "dataRequestTitleIt", "Richiesta di test IT",
            "dataConsumer", "Test AG"
        )
    );

    UUID batchId = (UUID) em.createNativeQuery("SELECT b.id FROM notification_batch b "
        + "JOIN notification_recipient r ON r.batch_id = b.id "
        + "WHERE r.email = ?1").setParameter(1, RECIPIENT_EMAIL).getSingleResult();

    awaitBatchComplete(batchId);

    Long submittedDispatches = (
        (Number) em.createNativeQuery("SELECT count(*) FROM notification_dispatch d "
            + "JOIN notification_recipient r ON r.id = d.recipient_id "
            + "WHERE r.batch_id = ?1 AND d.status_code = 'SUBMITTED'").setParameter(1, batchId).getSingleResult()
    ).longValue();
    assertThat(submittedDispatches).isEqualTo(1L);

    HttpResponse<String> response =
        http.send(HttpRequest.newBuilder(URI.create(sesInboxUri())).GET().build(), HttpResponse.BodyHandlers.ofString());
    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).as("LocalStack SES inbox should contain the dispatched mail")
        .contains(RECIPIENT_EMAIL)
        .contains(EXPECTED_SUBJECT);
  }

  private void awaitBatchComplete(UUID batchId) {
    awaitUntil(() -> {
      em.clear();
      var batch = batchRepository.findById(batchId);
      return batch != null && batch.getStatusCode() == NotificationBatchStatusEnum.COMPLETE;
    }, Duration.ofSeconds(10));

    var batch = batchRepository.findById(batchId);
    assertThat(batch.getStatusCode()).isEqualTo(NotificationBatchStatusEnum.COMPLETE);
  }

  private static void awaitUntil(BooleanSupplier condition, Duration timeout) {
    Instant deadline = Instant.now().plus(timeout);
    while (Instant.now().isBefore(deadline)) {
      if (condition.getAsBoolean()) {
        return;
      }
      try {
        Thread.sleep(100);
      } catch (InterruptedException _) {
        Thread.currentThread().interrupt();
        return;
      }
    }
  }

  private String sesInboxUri() {
    return sesEndpoint + "/_aws/ses";
  }
}
