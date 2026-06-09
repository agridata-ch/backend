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
import lombok.RequiredArgsConstructor;

/**
 * Handles audit logging for data request state transitions.
 *
 * @CommentLastReviewed 2026-06-08
 */
@ApplicationScoped
@RequiredArgsConstructor
public class DataRequestStateAuditService {

  private final AuditingService auditingService;
  private final NotificationService notificationService;

  public void auditAdminStatusTransition(
      DataRequestEntity entity,
      DataRequestEntity.DataRequestStateEnum oldStateCode,
      DataRequestEntity.DataRequestStateEnum newStateCode
  ) {
    if (oldStateCode == IN_REVIEW && newStateCode == DRAFT) {
      auditingService.logDataRequestRejected(entity.getId());
    } else if (oldStateCode == IN_REVIEW && newStateCode == TO_BE_SIGNED_BY_CONSUMER) {
      auditingService.logDataRequestApproved(entity.getId());
      auditingService.logCollectiveSignatureSet(entity.getId());
    } else if (oldStateCode == TO_BE_ACTIVATED && newStateCode == ACTIVE) {
      auditingService.logDataRequestActivated(entity.getId());
    }
  }

  public void auditConsumerStatusTransition(
      DataRequestEntity entity,
      DataRequestEntity.DataRequestStateEnum oldStateCode,
      DataRequestEntity.DataRequestStateEnum newStateCode
  ) {
    if (isWithdrawableState(oldStateCode) && newStateCode == DRAFT) {
      auditingService.logDataRequestWithdrawn(entity.getId());
    } else if (oldStateCode == DRAFT && newStateCode == IN_REVIEW) {
      auditingService.logDataRequestSubmitted(entity.getId());
      notificationService.queueDataRequestInReview(entity);
    } else if (oldStateCode == TO_BE_RELEASED_BY_CONSUMER && newStateCode == TO_BE_SIGNED_BY_PROVIDER) {
      auditingService.logDataRequestReleasedByConsumer(entity.getId());
    }
  }

  public void auditProviderStatusTransition(
      DataRequestEntity entity,
      DataRequestEntity.DataRequestStateEnum oldStateCode,
      DataRequestEntity.DataRequestStateEnum newStateCode
  ) {
    if (oldStateCode == TO_BE_RELEASED_BY_PROVIDER && newStateCode == TO_BE_ACTIVATED) {
      auditingService.logDataRequestReleasedByProvider(entity.getId());
    }
  }

  private boolean isWithdrawableState(DataRequestEntity.DataRequestStateEnum state) {
    return state == IN_REVIEW
        || state == TO_BE_SIGNED_BY_CONSUMER
        || state == TO_BE_RELEASED_BY_CONSUMER
        || state == TO_BE_SIGNED_BY_PROVIDER
        || state == TO_BE_RELEASED_BY_PROVIDER
        || state == TO_BE_ACTIVATED;
  }

}