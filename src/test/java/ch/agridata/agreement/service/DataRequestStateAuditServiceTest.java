package ch.agridata.agreement.service;

import static ch.agridata.agreement.persistence.DataRequestEntity.DataRequestStateEnum.DRAFT;
import static ch.agridata.agreement.persistence.DataRequestEntity.DataRequestStateEnum.IN_REVIEW;
import static ch.agridata.agreement.persistence.DataRequestEntity.DataRequestStateEnum.TO_BE_SIGNED_BY_CONSUMER;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import ch.agridata.agreement.persistence.DataRequestEntity;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link DataRequestStateAuditService}.
 *
 * @CommentLastReviewed 2026-05-11
 */
@ExtendWith(MockitoExtension.class)
class DataRequestStateAuditServiceTest {

  @InjectMocks
  private DataRequestStateAuditService service;

  @Mock
  private AuditingService auditingService;

  @Mock
  private NotificationService notificationService;

  private static DataRequestEntity entity() {
    return DataRequestEntity.builder().id(UUID.randomUUID()).build();
  }

  // ── auditAdminStatusTransition ────────────────────────────────────────────

  @Test
  void givenInReviewToDraft_whenAuditAdmin_thenLogRejected() {
    var entity = entity();

    service.auditAdminStatusTransition(entity, IN_REVIEW, DRAFT);

    verify(auditingService).logDataRequestRejected(entity.getId());
    verifyNoInteractions(notificationService);
  }

  @Test
  void givenInReviewToToBeSignedByConsumer_whenAuditAdmin_thenLogApproved() {
    var entity = entity();

    service.auditAdminStatusTransition(entity, IN_REVIEW, TO_BE_SIGNED_BY_CONSUMER);

    verify(auditingService).logDataRequestApproved(entity.getId());
    verifyNoInteractions(notificationService);
  }

  // ── auditConsumerStatusTransition ─────────────────────────────────────────

  @Test
  void givenDraftToInReview_whenAuditConsumer_thenLogSubmittedAndQueueNotification() {
    var entity = entity();

    service.auditConsumerStatusTransition(entity, DRAFT, IN_REVIEW);

    verify(auditingService).logDataRequestSubmitted(entity.getId());
    verify(notificationService).queueDataRequestInReview(entity);
  }

  @Test
  void givenInReviewToDraft_whenAuditConsumer_thenLogWithdrawnAndNoNotification() {
    var entity = entity();

    service.auditConsumerStatusTransition(entity, IN_REVIEW, DRAFT);

    verify(auditingService).logDataRequestWithdrawn(entity.getId());
    verifyNoInteractions(notificationService);
  }
}
