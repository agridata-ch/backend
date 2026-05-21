package ch.agridata.agreement.service;

import static ch.agridata.agreement.persistence.DataRequestEntity.DataRequestStateEnum.TO_BE_SIGNED_BY_CONSUMER;
import static ch.agridata.auditing.api.ActionEnum.CONSENT_REQUEST_DECLINED;
import static ch.agridata.auditing.api.ActionEnum.CONSENT_REQUEST_GRANTED;
import static ch.agridata.auditing.api.ActionEnum.CONSENT_REQUEST_REOPENED;
import static ch.agridata.auditing.api.ActionEnum.CONTRACT_COLLECTIVE_SIGNATURE_FOR_CONSUMER_CHOSEN;
import static ch.agridata.auditing.api.ActionEnum.CONTRACT_COLLECTIVE_SIGNATURE_FOR_PROVIDER_CHOSEN;
import static ch.agridata.auditing.api.ActionEnum.CONTRACT_FIRST_CONSUMER_SLOT_SIGNED;
import static ch.agridata.auditing.api.ActionEnum.CONTRACT_FIRST_PROVIDER_SLOT_SIGNED;
import static ch.agridata.auditing.api.ActionEnum.CONTRACT_INDIVIDUAL_SIGNATURE_FOR_CONSUMER_CHOSEN;
import static ch.agridata.auditing.api.ActionEnum.CONTRACT_INDIVIDUAL_SIGNATURE_FOR_PROVIDER_CHOSEN;
import static ch.agridata.auditing.api.ActionEnum.CONTRACT_PDF_ELECTRONICALLY_SIGNED;
import static ch.agridata.auditing.api.ActionEnum.CONTRACT_SECOND_CONSUMER_SLOT_SIGNED;
import static ch.agridata.auditing.api.ActionEnum.CONTRACT_SECOND_PROVIDER_SLOT_SIGNED;
import static ch.agridata.auditing.api.ActionEnum.DATA_REQUEST_ACTIVATED;
import static ch.agridata.auditing.api.ActionEnum.DATA_REQUEST_APPROVED;
import static ch.agridata.auditing.api.ActionEnum.DATA_REQUEST_REJECTED;
import static ch.agridata.auditing.api.ActionEnum.DATA_REQUEST_RELEASED_BY_CONSUMER;
import static ch.agridata.auditing.api.ActionEnum.DATA_REQUEST_RELEASED_BY_PROVIDER;
import static ch.agridata.auditing.api.ActionEnum.DATA_REQUEST_SUBMITTED;
import static ch.agridata.auditing.api.ActionEnum.DATA_REQUEST_WITHDRAWN;
import static ch.agridata.auditing.api.EntityTypeEnum.CONSENT_REQUEST;
import static ch.agridata.auditing.api.EntityTypeEnum.CONTRACT_REVISION;
import static ch.agridata.auditing.api.EntityTypeEnum.DATA_REQUEST;

import ch.agridata.agreement.dto.SignatureSlotCodeEnum;
import ch.agridata.agreement.dto.SignatureTypeEnum;
import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.auditing.api.ActionEnum;
import ch.agridata.auditing.api.AuditingApi;
import ch.agridata.auditing.api.EntityTypeEnum;
import ch.agridata.auditing.api.SystemActorEnum;
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

  public void logDataRequestTerminated(UUID entityId) {
    api.logSystemAction(
        ActionEnum.CONSENT_REQUEST_TERMINATED,
        EntityTypeEnum.CONSENT_REQUEST,
        entityId,
        SystemActorEnum.CONSENT_REQUEST_CLEANUP_JOB
    );
  }

  public void logContractRevisionSigned(UUID contractRevisionId, SignatureSlotCodeEnum signatureSlotCodeEnum) {
    ActionEnum action = switch (signatureSlotCodeEnum) {
      case DATA_CONSUMER_01 -> CONTRACT_FIRST_CONSUMER_SLOT_SIGNED;
      case DATA_CONSUMER_02 -> CONTRACT_SECOND_CONSUMER_SLOT_SIGNED;
      case DATA_PROVIDER_01 -> CONTRACT_FIRST_PROVIDER_SLOT_SIGNED;
      case DATA_PROVIDER_02 -> CONTRACT_SECOND_PROVIDER_SLOT_SIGNED;
    };

    api.logUserAction(action, CONTRACT_REVISION, contractRevisionId);
  }

  public void logSignatureTypeChosen(
      UUID dataRequestId,
      SignatureTypeEnum signatureType,
      DataRequestEntity.DataRequestStateEnum requiredState
  ) {

    boolean isConsumer = requiredState == TO_BE_SIGNED_BY_CONSUMER;

    ActionEnum action = switch (signatureType) {
      case INDIVIDUAL_SIGNATURE -> isConsumer
          ? CONTRACT_INDIVIDUAL_SIGNATURE_FOR_CONSUMER_CHOSEN
          : CONTRACT_INDIVIDUAL_SIGNATURE_FOR_PROVIDER_CHOSEN;

      case COLLECTIVE_SIGNATURE -> isConsumer
          ? CONTRACT_COLLECTIVE_SIGNATURE_FOR_CONSUMER_CHOSEN
          : CONTRACT_COLLECTIVE_SIGNATURE_FOR_PROVIDER_CHOSEN;
    };

    api.logUserAction(action, DATA_REQUEST, dataRequestId);
  }

  public void logDataRequestReleasedByConsumer(UUID dataRequestId) {
    api.logUserAction(DATA_REQUEST_RELEASED_BY_CONSUMER, DATA_REQUEST, dataRequestId);
  }

  public void logDataRequestReleasedByProvider(UUID dataRequestId) {
    api.logUserAction(DATA_REQUEST_RELEASED_BY_PROVIDER, DATA_REQUEST, dataRequestId);
  }

  public void logContractPdfElectronicallySigned(UUID contractRevisionId) {
    api.logUserAction(CONTRACT_PDF_ELECTRONICALLY_SIGNED, CONTRACT_REVISION, contractRevisionId);
  }
}