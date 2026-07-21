package integration.agreement;

import static integration.agreement.DataRequestTestFactory.createDataRequest;
import static integration.agreement.DataRequestTestFactory.getDataRequestDtoBuilder;
import static integration.agreement.DataRequestTestFactory.setStatusAs;
import static integration.agreement.DataRequestTestFactory.updateDataRequest;
import static integration.testutils.TestUserEnum.ADMIN;
import static integration.testutils.TestUserEnum.CONSUMER_BIO_SUISSE;
import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.agreement.dto.DataRequestStateEnum;
import ch.agridata.notification.dto.EventTypeCodeEnum;
import ch.agridata.notification.persistence.NotificationBatchEntity;
import ch.agridata.notification.persistence.NotificationBatchRepository;
import ch.agridata.notification.persistence.NotificationBatchStatusEnum;
import ch.agridata.notification.persistence.NotificationDispatchEntity;
import ch.agridata.notification.persistence.NotificationDispatchRepository;
import integration.testutils.AuthTestUtils;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.persistence.EntityManager;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * Integration tests verifying that notification batches are queued on data-request state transitions.
 *
 * @CommentLastReviewed 2026-05-06
 */
@Slf4j
@QuarkusTest
@RequiredArgsConstructor
class DataRequestNotificationTest {

  private final NotificationBatchRepository notificationBatchRepository;
  private final NotificationDispatchRepository notificationDispatchRepository;
  private final EntityManager em;

  @Test
  void givenValidInReviewRequest_whenAdminApproves_thenPendingNotificationBatchIsQueued() {
    // Trigger consumer user creation so the recipient list has at least one result
    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE).when().get("/api/user/v1/user-info");

    String id = createDataRequest().then().statusCode(201).extract().path("id");
    updateDataRequest(id, getDataRequestDtoBuilder().build()).then().statusCode(200);
    setStatusAs(id, DataRequestStateEnum.IN_REVIEW, CONSUMER_BIO_SUISSE).then().statusCode(200);

    setStatusAs(id, DataRequestStateEnum.TO_BE_SIGNED_BY_CONSUMER, ADMIN).then().statusCode(200);

    assertNotificationBatchCompleted(EventTypeCodeEnum.DATA_REQUEST_APPROVED);
  }

  @Test
  void givenValidInReviewRequest_whenAdminRequestsChanges_thenPendingNotificationBatchIsQueued() {
    // Trigger consumer user creation so the recipient list has at least one result
    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE).when().get("/api/user/v1/user-info");

    String id = createDataRequest().then().statusCode(201).extract().path("id");
    updateDataRequest(id, getDataRequestDtoBuilder().build()).then().statusCode(200);
    setStatusAs(id, DataRequestStateEnum.IN_REVIEW, CONSUMER_BIO_SUISSE).then().statusCode(200);

    setStatusAs(id, DataRequestStateEnum.DRAFT, ADMIN).then().statusCode(200);

    assertNotificationBatchCompleted(EventTypeCodeEnum.DATA_REQUEST_CHANGES_NEEDED);
  }

  @Test
  void givenValidDraftRequest_whenConsumerSubmits_thenPendingNotificationBatchIsQueued() {
    // Trigger admin user creation so UserService.getAdminUserIds() has at least one result
    AuthTestUtils.requestAs(ADMIN).when().get("/api/user/v1/user-info");

    String id = createDataRequest().then().statusCode(201).extract().path("id");
    updateDataRequest(id, getDataRequestDtoBuilder().build()).then().statusCode(200);

    setStatusAs(id, DataRequestStateEnum.IN_REVIEW, CONSUMER_BIO_SUISSE).then().statusCode(200);

    assertNotificationBatchCompleted(EventTypeCodeEnum.DATA_REQUEST_READY_FOR_REVIEW);
  }

  private void assertNotificationBatchCompleted(EventTypeCodeEnum eventType) {
    var batch = awaitProcessedNotificationBatch(eventType);
    if (batch.getStatusCode() != NotificationBatchStatusEnum.COMPLETE) {
      logDispatchErrors(batch);
    }

    assertThat(batch.getStatusCode()).isEqualTo(NotificationBatchStatusEnum.COMPLETE);
  }

  /**
   * Logs the per-recipient dispatch error messages when a batch did not complete. This test has shown flaky behaviour,
   * so the errors are logged here to aid troubleshooting when it fails again.
   */
  private void logDispatchErrors(NotificationBatchEntity batch) {
    List<String> errors = notificationDispatchRepository.find("recipient.batch.id = ?1", batch.getId()).stream()
        .map(NotificationDispatchEntity::getError)
        .filter(Objects::nonNull)
        .toList();
    log.error("Notification batch {} ended in status {} instead of COMPLETE. Dispatch errors: {}", batch.getId(),
        batch.getStatusCode(), errors);
  }

  private NotificationBatchEntity awaitProcessedNotificationBatch(EventTypeCodeEnum eventType) {
    awaitUntil(() -> {
      em.clear();
      return findNotificationBatch(eventType)
          .filter(b -> b.getStatusCode() != NotificationBatchStatusEnum.PENDING)
          .isPresent();
    }, Duration.ofSeconds(4));

    em.clear();
    return findNotificationBatch(eventType)
        .orElseThrow(() -> new AssertionError("No notification batch was queued for event " + eventType));
  }

  private Optional<NotificationBatchEntity> findNotificationBatch(EventTypeCodeEnum eventType) {
    return notificationBatchRepository.findAll().stream()
        .filter(b -> b.getTemplate().getEventTypeCode().equals(eventType.name()))
        .findFirst();
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
}
