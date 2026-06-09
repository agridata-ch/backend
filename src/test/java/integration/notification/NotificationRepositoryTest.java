package integration.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.notification.persistence.NotificationBatchEntity;
import ch.agridata.notification.persistence.NotificationBatchStatusEnum;
import ch.agridata.notification.persistence.NotificationChannelCodeEnum;
import ch.agridata.notification.persistence.NotificationDispatchEntity;
import ch.agridata.notification.persistence.NotificationDispatchRepository;
import ch.agridata.notification.persistence.NotificationDispatchStatusEnum;
import ch.agridata.notification.persistence.NotificationInboxEntity;
import ch.agridata.notification.persistence.NotificationInboxRepository;
import ch.agridata.notification.persistence.NotificationRecipientEntity;
import ch.agridata.notification.persistence.NotificationTemplateEntity;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Integration tests for {@link NotificationInboxRepository} and {@link NotificationDispatchRepository}.
 * Each test creates its own isolated data to avoid conflicts from the uniqueness constraint on
 * {@code notification_inbox.recipient_id} and accumulated state between test runs.
 *
 * @CommentLastReviewed 2026-05-05
 */
@QuarkusTest
@RequiredArgsConstructor
class NotificationRepositoryTest {

  private final NotificationInboxRepository inboxRepository;
  private final NotificationDispatchRepository dispatchRepository;
  private final EntityManager entityManager;

  @InjectMock
  AgridataSecurityIdentity securityIdentity;

  private static final UUID TEST_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000099");

  @BeforeEach
  void setUpSecurityIdentity() {
    when(securityIdentity.getUserId()).thenReturn(TEST_USER_ID);
  }

  // ── helpers ──────────────────────────────────────────────────────────────

  /**
   * Persists a fresh template → batch → recipient chain in a new transaction and returns the
   * recipient ID. Each call produces an independent recipient to avoid the unique inbox constraint.
   */
  private UUID persistFreshRecipient() {
    return QuarkusTransaction.requiringNew().call(() -> {
      var template = NotificationTemplateEntity.builder().eventTypeCode("TEST_EVENT").templateVersion(1).build();
      entityManager.persist(template);

      var batch = NotificationBatchEntity.builder().template(template).statusCode(NotificationBatchStatusEnum.PENDING).build();
      entityManager.persist(batch);

      var recipient = NotificationRecipientEntity.builder().batch(batch).userId(TEST_USER_ID).email("repo-test@example.com").build();
      entityManager.persist(recipient);

      return recipient.getId();
    });
  }

  // ── NotificationInboxRepository.existsByRecipientId ──────────────────────

  @Test
  void givenInboxEntryExists_whenExistsByRecipientId_thenReturnsTrue() {
    UUID recipientId = persistFreshRecipient();
    QuarkusTransaction.requiringNew().run(() -> {
      var recipient = entityManager.find(NotificationRecipientEntity.class, recipientId);
      entityManager.persist(NotificationInboxEntity.builder().recipient(recipient).userId(TEST_USER_ID).isRead(false).build());
    });

    boolean exists = QuarkusTransaction.requiringNew().call(() -> inboxRepository.existsByRecipientId(recipientId));

    assertThat(exists).isTrue();
  }

  @Test
  void givenNoInboxEntry_whenExistsByRecipientId_thenReturnsFalse() {
    UUID recipientId = persistFreshRecipient();

    boolean exists = QuarkusTransaction.requiringNew().call(() -> inboxRepository.existsByRecipientId(recipientId));

    assertThat(exists).isFalse();
  }

  // ── NotificationInboxRepository.markAsRead ───────────────────────────────

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void givenUnreadInboxEntry_whenMarkReadStatus_thenUpdatesCountAndSetsReadFlag(boolean markAsRead) {
    UUID recipientId = persistFreshRecipient();
    UUID inboxId = QuarkusTransaction.requiringNew().call(() -> {
      var inbox = NotificationInboxEntity.builder()
          .recipient(entityManager.find(NotificationRecipientEntity.class, recipientId))
          .userId(TEST_USER_ID)
          .isRead(!markAsRead)
          .build();
      entityManager.persist(inbox);
      return inbox.getId();
    });

    int updated = QuarkusTransaction.requiringNew().call(() -> inboxRepository.markReadStatus(TEST_USER_ID, List.of(inboxId), markAsRead));

    assertThat(updated).isEqualTo(1);
    boolean isRead = QuarkusTransaction.requiringNew().call(() -> entityManager.find(NotificationInboxEntity.class, inboxId).isRead());
    assertThat(isRead).isEqualTo(markAsRead);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void givenInboxEntryOfDifferentUser_whenMarkAsRead_thenUpdatesNothing(boolean markAsRead) {
    UUID recipientId = persistFreshRecipient();
    UUID otherUserId = UUID.randomUUID();
    UUID inboxId = QuarkusTransaction.requiringNew().call(() -> {
      var inbox = NotificationInboxEntity.builder()
          .recipient(entityManager.find(NotificationRecipientEntity.class, recipientId))
          .userId(TEST_USER_ID)
          .isRead(!markAsRead)
          .build();
      entityManager.persist(inbox);
      return inbox.getId();
    });

    int updated = QuarkusTransaction.requiringNew().call(() -> inboxRepository.markReadStatus(otherUserId, List.of(inboxId), markAsRead));

    assertThat(updated).isZero();
  }

  // ── NotificationDispatchRepository.existsSentByRecipientId ───────────────

  @Test
  void givenSubmittedDispatch_whenExistsSentByRecipientId_thenReturnsTrue() {
    UUID recipientId = persistFreshRecipient();
    QuarkusTransaction.requiringNew()
        .run(() -> entityManager.persist(NotificationDispatchEntity.builder()
            .recipient(entityManager.find(NotificationRecipientEntity.class, recipientId))
            .channelCode(NotificationChannelCodeEnum.EMAIL)
            .statusCode(NotificationDispatchStatusEnum.SUBMITTED)
            .build()));

    boolean exists = QuarkusTransaction.requiringNew().call(() -> dispatchRepository.existsSentByRecipientId(recipientId));

    assertThat(exists).isTrue();
  }

  @Test
  void givenOnlyFailedDispatch_whenExistsSentByRecipientId_thenReturnsFalse() {
    UUID recipientId = persistFreshRecipient();
    QuarkusTransaction.requiringNew()
        .run(() -> entityManager.persist(NotificationDispatchEntity.builder()
            .recipient(entityManager.find(NotificationRecipientEntity.class, recipientId))
            .channelCode(NotificationChannelCodeEnum.EMAIL)
            .statusCode(NotificationDispatchStatusEnum.FAILED)
            .build()));

    boolean exists = QuarkusTransaction.requiringNew().call(() -> dispatchRepository.existsSentByRecipientId(recipientId));

    assertThat(exists).isFalse();
  }

  @Test
  void givenNoDispatch_whenExistsSentByRecipientId_thenReturnsFalse() {
    UUID recipientId = persistFreshRecipient();

    boolean exists = QuarkusTransaction.requiringNew().call(() -> dispatchRepository.existsSentByRecipientId(recipientId));

    assertThat(exists).isFalse();
  }
}
