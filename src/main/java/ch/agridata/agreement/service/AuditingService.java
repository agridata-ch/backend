package ch.agridata.agreement.service;

import static ch.agridata.auditing.api.ActionEnum.CONSENT_REQUEST_DECLINED;
import static ch.agridata.auditing.api.ActionEnum.CONSENT_REQUEST_GRANTED;
import static ch.agridata.auditing.api.ActionEnum.CONSENT_REQUEST_REOPENED;
import static ch.agridata.auditing.api.ActionEnum.DATA_REQUEST_ACTIVATED;
import static ch.agridata.auditing.api.ActionEnum.DATA_REQUEST_APPROVED;
import static ch.agridata.auditing.api.ActionEnum.DATA_REQUEST_REJECTED;
import static ch.agridata.auditing.api.ActionEnum.DATA_REQUEST_SUBMITTED;
import static ch.agridata.auditing.api.ActionEnum.DATA_REQUEST_WITHDRAWN;
import static ch.agridata.auditing.api.EntityTypeEnum.CONSENT_REQUEST;
import static ch.agridata.auditing.api.EntityTypeEnum.DATA_REQUEST;

import ch.agridata.auditing.api.AuditingApi;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

/**
 * Manages auditing concerns for requests. It records actions to ensure traceability.
 *
 * @CommentLastReviewed 2026-01-22
 */

@ApplicationScoped
@RequiredArgsConstructor
public class AuditingService {

  private final AuditingApi api;

  public void logConsentRequestGranted(UUID entityId) {
    api.logUserAction(CONSENT_REQUEST_GRANTED, CONSENT_REQUEST, entityId);
  }

  public void logConsentRequestDeclined(UUID entityId) {
    api.logUserAction(CONSENT_REQUEST_DECLINED, CONSENT_REQUEST, entityId);
  }

  public void logConsentRequestReopened(UUID entityId) {
    api.logUserAction(CONSENT_REQUEST_REOPENED, CONSENT_REQUEST, entityId);
  }

  public void logDataRequestSubmitted(UUID entityId) {
    api.logUserAction(DATA_REQUEST_SUBMITTED, DATA_REQUEST, entityId);
  }

  public void logDataRequestRejected(UUID entityId) {
    api.logUserAction(DATA_REQUEST_REJECTED, DATA_REQUEST, entityId);
  }

  public void logDataRequestApproved(UUID entityId) {
    api.logUserAction(DATA_REQUEST_APPROVED, DATA_REQUEST, entityId);
  }

  public void logDataRequestActivated(UUID entityId) {
    api.logUserAction(DATA_REQUEST_ACTIVATED, DATA_REQUEST, entityId);
  }

  public void logDataRequestWithdrawn(UUID entityId) {
    api.logUserAction(DATA_REQUEST_WITHDRAWN, DATA_REQUEST, entityId);
  }

}
