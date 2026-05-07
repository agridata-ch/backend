package ch.agridata.agreement.service;

import static ch.agridata.agreement.persistence.DataRequestEntity.DataRequestStateEnum.ACTIVE;
import static ch.agridata.agreement.persistence.DataRequestEntity.DataRequestStateEnum.DRAFT;
import static ch.agridata.agreement.persistence.DataRequestEntity.DataRequestStateEnum.IN_REVIEW;
import static ch.agridata.agreement.persistence.DataRequestEntity.DataRequestStateEnum.TO_BE_ACTIVATED;
import static ch.agridata.agreement.persistence.DataRequestEntity.DataRequestStateEnum.TO_BE_RELEASED_BY_CONSUMER;
import static ch.agridata.agreement.persistence.DataRequestEntity.DataRequestStateEnum.TO_BE_RELEASED_BY_PROVIDER;
import static ch.agridata.agreement.persistence.DataRequestEntity.DataRequestStateEnum.TO_BE_SIGNED_BY_CONSUMER;
import static ch.agridata.agreement.persistence.DataRequestEntity.DataRequestStateEnum.TO_BE_SIGNED_BY_PROVIDER;

import ch.agridata.agreement.persistence.DataRequestEntity;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

/**
 * Handles audit logging for data request state transitions.
 *
 * @CommentLastReviewed 2026-04-01
 */
@ApplicationScoped
@RequiredArgsConstructor
public class DataRequestStateAuditService {

  private final AuditingService auditingService;
  private final NotificationService notificationService;

  public void auditAdminStatusTransition(
      UUID requestId,
      DataRequestEntity.DataRequestStateEnum oldStateCode,
      DataRequestEntity.DataRequestStateEnum newStateCode
  ) {
    if (oldStateCode == IN_REVIEW && newStateCode == DRAFT) {
      auditingService.logDataRequestRejected(requestId);
    } else if (oldStateCode == IN_REVIEW && newStateCode == TO_BE_SIGNED_BY_CONSUMER) {
      auditingService.logDataRequestApproved(requestId);
    } else if (oldStateCode == TO_BE_ACTIVATED && newStateCode == ACTIVE) {
      auditingService.logDataRequestActivated(requestId);
    }
  }

  public void auditConsumerStatusTransition(
      UUID requestId,
      DataRequestEntity.DataRequestStateEnum oldStateCode,
      DataRequestEntity.DataRequestStateEnum newStateCode
  ) {
    if (oldStateCode == IN_REVIEW && newStateCode == DRAFT) {
      auditingService.logDataRequestWithdrawn(requestId);
    } else if (oldStateCode == DRAFT && newStateCode == IN_REVIEW) {
      auditingService.logDataRequestSubmitted(requestId);
      notificationService.queueDataRequestInReview(requestId);
    } else if (oldStateCode == TO_BE_RELEASED_BY_CONSUMER && newStateCode == TO_BE_SIGNED_BY_PROVIDER) {
      auditingService.logDataRequestReleasedByConsumer(requestId);
    }
  }

  public void auditProviderStatusTransition(UUID requestId,
                                            DataRequestEntity.DataRequestStateEnum oldStateCode,
                                            DataRequestEntity.DataRequestStateEnum newStateCode) {
    if (oldStateCode == TO_BE_RELEASED_BY_PROVIDER && newStateCode == TO_BE_ACTIVATED) {
      auditingService.logDataRequestReleasedByProvider(requestId);
    }
  }
}
