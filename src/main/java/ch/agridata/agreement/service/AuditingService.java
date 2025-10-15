package ch.agridata.agreement.service;

import static ch.agridata.auditing.api.ActionEnum.CONSENT_REQUEST_DECLINED;
import static ch.agridata.auditing.api.ActionEnum.CONSENT_REQUEST_GRANTED;
import static ch.agridata.auditing.api.ActionEnum.CONSENT_REQUEST_REOPENED;
import static ch.agridata.auditing.api.EntityTypeEnum.CONSENT_REQUEST;

import ch.agridata.auditing.api.AuditingApi;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

/**
 * Manages auditing concerns for requests. It records and validates actions to ensure traceability.
 *
 * @CommentLastReviewed 2025-08-25
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

}
