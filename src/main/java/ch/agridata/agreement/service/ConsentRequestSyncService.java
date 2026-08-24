package ch.agridata.agreement.service;

import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.DECLINED;
import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.GRANTED;
import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.LEGALLY_PERMITTED;
import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.OPENED;

import ch.agridata.agreement.persistence.ConsentRequestEntity;
import ch.agridata.agreement.persistence.ConsentRequestRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

/**
 * Keeps the UID consent request in sync with its active BUR consent requests.
 *
 * @CommentLastReviewed 2026-08-21
 */
@ApplicationScoped
@RequiredArgsConstructor
public class ConsentRequestSyncService {

  private final ConsentRequestRepository consentRequestRepository;
  private final AuditingService auditingService;

  public void syncUidConsentRequestStateWithBurConsentRequests(UUID dataRequestId, String uid) {
    var uidConsentRequest = consentRequestRepository.findAndLockUidBasedByDataRequestIdAndDataProducerUid(dataRequestId, uid)
        .orElseThrow(
            () -> new IllegalStateException("no uid consent request found for dataRequestId=" + dataRequestId + " and uid=" + uid));

    var burConsentRequests = consentRequestRepository.findActiveUidAndBurBasedByDataRequestIdAndDataProducerUid(dataRequestId, uid)
        .stream()
        .filter(ConsentRequestEntity::isBurConsentRequest)
        .toList();

    if (burConsentRequests.isEmpty()) {
      return;
    }

    var uidTargetState = determineUidTargetState(burConsentRequests);
    if (!uidConsentRequest.getStateCode().equals(uidTargetState)) {
      uidConsentRequest.setStateCode(uidTargetState);
      auditingService.logConsentRequestStateChange(uidConsentRequest);
    }
  }

  private ConsentRequestEntity.StateEnum determineUidTargetState(List<ConsentRequestEntity> burConsentRequests) {
    Set<ConsentRequestEntity.StateEnum> burStates = burConsentRequests.stream()
        .map(ConsentRequestEntity::getStateCode)
        .collect(Collectors.toSet());

    if (burStates.contains(LEGALLY_PERMITTED)) {
      return LEGALLY_PERMITTED;
    }
    if (burStates.contains(GRANTED)) {
      return GRANTED;
    }
    if (burStates.contains(DECLINED)) {
      return DECLINED;
    }
    return OPENED;
  }
}
