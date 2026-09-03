package ch.agridata.agreement.service;

import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.DECLINED;
import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.GRANTED;
import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.OPENED;
import static ch.agridata.common.utils.AuthenticationUtil.PRODUCER_ROLE;

import ch.agridata.agreement.dto.ConsentRequestStateEnum;
import ch.agridata.agreement.mapper.ConsentRequestMapper;
import ch.agridata.agreement.persistence.ConsentRequestEntity;
import ch.agridata.agreement.persistence.ConsentRequestRepository;
import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.user.api.UserApi;
import ch.agridata.user.dto.UidDto;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.NotFoundException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

/**
 * Manages state transitions of consent requests and enforces transition rules. Handles validation and submission logic related to changing
 * request states.
 *
 * @CommentLastReviewed 2025-09-26
 */

@ApplicationScoped
@RequiredArgsConstructor
public class ConsentRequestStateService {

  private final ConsentRequestRepository consentRequestRepository;
  private final ConsentRequestMapper consentRequestMapper;
  private final ConsentRequestSyncService consentRequestSyncService;
  private final AuditingService auditingService;
  private final AgridataSecurityIdentity identity;
  private final UserApi userApi;
  private final Clock clock;

  private static final Map<Transition, Rule> ALLOWED_TRANSITIONS = Map.of(
      new Transition(null, OPENED), Rule.allow(),
      new Transition(null, GRANTED), Rule.allow(),
      new Transition(null, DECLINED), Rule.allow(),
      new Transition(OPENED, GRANTED), Rule.allow(),
      new Transition(OPENED, DECLINED), Rule.allow(),
      new Transition(GRANTED, DECLINED), Rule.allow(),
      new Transition(GRANTED, OPENED), Rule.allowWithinSeconds(30),
      new Transition(DECLINED, GRANTED), Rule.allow(),
      new Transition(DECLINED, OPENED), Rule.allowWithinSeconds(30)
  );

  @RolesAllowed(PRODUCER_ROLE)
  @Transactional
  public void updateConsentRequestStateAsCurrentDataProducer(UUID consentRequestId, ConsentRequestStateEnum state) {
    var uids = getAuthorizedUidsAsCurrentProducer();
    var consentRequest = consentRequestRepository.findActiveUidAndBurBasedByIdAndDataProducerUids(consentRequestId, uids)
        .orElseThrow(() -> new NotFoundException(consentRequestId.toString()));
    var targetState = consentRequestMapper.toEntityStateEnum(state);

    if (consentRequest.isBurConsentRequest()) {
      updateBurConsentRequestStateAsCurrentDataProducer(consentRequest, targetState);
    } else {
      updateUidConsentRequestStateAsCurrentDataProducer(consentRequest, targetState);
    }
  }

  private void updateUidConsentRequestStateAsCurrentDataProducer(ConsentRequestEntity consentRequest,
                                                                 ConsentRequestEntity.StateEnum targetState) {
    verifyNoBurConsentRequests(consentRequest);
    verifyStatusTransition(consentRequest, targetState);
    consentRequest.setStateCode(targetState);
    auditingService.logConsentRequestStateChange(consentRequest);
  }

  private void updateBurConsentRequestStateAsCurrentDataProducer(ConsentRequestEntity consentRequest,
                                                                 ConsentRequestEntity.StateEnum targetState) {
    verifyStatusTransition(consentRequest, targetState);
    consentRequest.setStateCode(targetState);
    auditingService.logConsentRequestStateChange(consentRequest);

    consentRequestSyncService.syncUidConsentRequestStateWithBurConsentRequests(consentRequest.getDataRequest().getId(),
        consentRequest.getDataProducerUid());
  }


  private void verifyNoBurConsentRequests(ConsentRequestEntity consentRequest) {
    var dataRequestId = consentRequest.getDataRequest().getId();
    var uid = consentRequest.getDataProducerUid();
    var hasBurConsentRequests = consentRequestRepository.findActiveUidAndBurBasedByDataRequestIdAndDataProducerUid(dataRequestId, uid)
        .stream()
        .anyMatch(ConsentRequestEntity::isBurConsentRequest);

    if (hasBurConsentRequests) {
      throw new ValidationException(
          "UID consent request " + consentRequest.getId() + " cannot be edited directly while active BUR consent requests exist for uid "
              + consentRequest.getDataProducerUid()
      );
    }
  }

  private void verifyStatusTransition(ConsentRequestEntity consentRequest, ConsentRequestEntity.StateEnum targetState) {
    var currentState = consentRequest.getStateCode();
    var lastStateChangeDate = consentRequest.getLastStateChangeDate();

    Rule rule = Optional.ofNullable(ALLOWED_TRANSITIONS.get(new Transition(currentState, targetState)))
        .orElseThrow(() -> new ValidationException("invalid transition from " + currentState + " to " + targetState));

    if (rule.maxAgeSeconds == null || lastStateChangeDate == null) {
      return;
    }
    var lastAcceptableChangeDate = LocalDateTime.now(clock).minusSeconds(rule.maxAgeSeconds);
    if (lastStateChangeDate.isBefore(lastAcceptableChangeDate)) {
      throw new ValidationException(String.format(
          "unable to transition from %s to %s. LastStateChangeDate '%s' was too long ago. LastAcceptableChangeDate was '%s'",
          currentState,
          targetState,
          lastStateChangeDate,
          lastAcceptableChangeDate
      ));
    }
  }

  private record Transition(ConsentRequestEntity.StateEnum from, ConsentRequestEntity.StateEnum to) {
  }

  private record Rule(Integer maxAgeSeconds) {
    static Rule allow() {
      return new Rule(null);
    }

    // enables revert functionality
    static Rule allowWithinSeconds(int seconds) {
      return new Rule(seconds);
    }
  }

  private List<String> getAuthorizedUidsAsCurrentProducer() {
    return userApi.getAuthorizedUids(identity.getKtIdP(), identity.getAgateLoginId()).stream()
        .map(UidDto::uid)
        .toList();
  }
}
