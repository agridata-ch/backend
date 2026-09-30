package ch.agridata.agreement.service;

import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.DECLINED;
import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.GRANTED;
import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.OPENED;
import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.WITHDRAWN;
import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PRODUCER_ROLE;
import static java.util.Map.entry;

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

  private static final Map<Transition, Rule> ALLOWED_TRANSITIONS = Map.ofEntries(
      entry(new Transition(null, OPENED), Rule.allow(Actor.PRODUCER)),
      entry(new Transition(null, GRANTED), Rule.allow(Actor.PRODUCER)),
      entry(new Transition(null, DECLINED), Rule.allow(Actor.PRODUCER)),
      entry(new Transition(OPENED, GRANTED), Rule.allow(Actor.PRODUCER)),
      entry(new Transition(OPENED, DECLINED), Rule.allow(Actor.PRODUCER)),
      entry(new Transition(GRANTED, DECLINED), Rule.allow(Actor.PRODUCER)),
      entry(new Transition(GRANTED, OPENED), Rule.allowWithinSeconds(Actor.PRODUCER, 30)),
      entry(new Transition(DECLINED, GRANTED), Rule.allow(Actor.PRODUCER)),
      entry(new Transition(DECLINED, OPENED), Rule.allowWithinSeconds(Actor.PRODUCER, 30)),
      entry(new Transition(OPENED, WITHDRAWN), Rule.allow(Actor.CONSUMER)),
      entry(new Transition(GRANTED, WITHDRAWN), Rule.allow(Actor.CONSUMER)),
      entry(new Transition(DECLINED, WITHDRAWN), Rule.allow(Actor.CONSUMER))
  );

  @RolesAllowed(PRODUCER_ROLE)
  @Transactional
  public void updateConsentRequestStateAsCurrentDataProducer(UUID consentRequestId, ConsentRequestStateEnum state) {
    var uids = getAuthorizedUidsAsCurrentProducer();
    var consentRequest = consentRequestRepository.findActiveByIdAndDataProducerUids(consentRequestId, uids)
        .orElseThrow(() -> new NotFoundException(consentRequestId.toString()));
    var targetState = consentRequestMapper.toEntityStateEnum(state);

    updateConsentRequestState(consentRequest, targetState, Actor.PRODUCER);
  }

  @RolesAllowed(CONSUMER_ROLE)
  @Transactional
  public void updateConsentRequestStateAsCurrentDataConsumer(UUID consentRequestId, ConsentRequestStateEnum state) {
    var consentRequest = consentRequestRepository.findActiveByIdAndDataConsumerUid(consentRequestId,
            identity.getUidOrElseThrow())
        .orElseThrow(() -> new NotFoundException(consentRequestId.toString()));
    var targetState = consentRequestMapper.toEntityStateEnum(state);

    updateConsentRequestState(consentRequest, targetState, Actor.CONSUMER);
  }

  private void updateConsentRequestState(ConsentRequestEntity consentRequest, ConsentRequestEntity.StateEnum targetState, Actor actor) {
    if (consentRequest.isBurConsentRequest()) {
      updateBurConsentRequestState(consentRequest, targetState, actor);
    } else {
      updateUidConsentRequestState(consentRequest, targetState, actor);
    }
  }

  private void updateUidConsentRequestState(ConsentRequestEntity consentRequest, ConsentRequestEntity.StateEnum targetState, Actor actor) {
    verifyNoBurConsentRequests(consentRequest);
    verifyStatusTransition(consentRequest, targetState, actor);
    consentRequest.setStateCode(targetState);
    auditingService.logConsentRequestStateChange(consentRequest);
  }

  private void updateBurConsentRequestState(ConsentRequestEntity consentRequest, ConsentRequestEntity.StateEnum targetState, Actor actor) {
    verifyStatusTransition(consentRequest, targetState, actor);
    consentRequest.setStateCode(targetState);
    auditingService.logConsentRequestStateChange(consentRequest);

    consentRequestSyncService.syncUidConsentRequestStateWithBurConsentRequests(consentRequest.getDataRequest().getId(),
        consentRequest.getDataProducerUid());
  }

  private void verifyNoBurConsentRequests(ConsentRequestEntity consentRequest) {
    var dataRequestId = consentRequest.getDataRequest().getId();
    var uid = consentRequest.getDataProducerUid();
    var hasBurConsentRequests = consentRequestRepository.findActiveByDataRequestIdAndDataProducerUid(dataRequestId, uid)
        .stream()
        .anyMatch(ConsentRequestEntity::isBurConsentRequest);

    if (hasBurConsentRequests) {
      throw new ValidationException(
          "UID consent request " + consentRequest.getId() + " cannot be edited directly while active BUR consent requests exist for uid "
              + consentRequest.getDataProducerUid()
      );
    }
  }

  private void verifyStatusTransition(ConsentRequestEntity consentRequest, ConsentRequestEntity.StateEnum targetState, Actor actor) {
    var currentState = consentRequest.getStateCode();
    var lastStateChangeDate = consentRequest.getLastStateChangeDate();

    Rule rule = Optional.ofNullable(ALLOWED_TRANSITIONS.get(new Transition(currentState, targetState)))
        .filter(allowedRule -> allowedRule.actor == actor)
        .orElseThrow(() -> new ValidationException("invalid transition from " + currentState + " to " + targetState + " for " + actor));

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

  private enum Actor {
    PRODUCER,
    CONSUMER
  }

  private record Rule(Actor actor, Integer maxAgeSeconds) {
    static Rule allow(Actor actor) {
      return new Rule(actor, null);
    }

    // enables revert functionality
    static Rule allowWithinSeconds(Actor actor, int seconds) {
      return new Rule(actor, seconds);
    }
  }

  private List<String> getAuthorizedUidsAsCurrentProducer() {
    return userApi.getAuthorizedUids(identity.getKtIdP(), identity.getAgateLoginId()).stream()
        .map(UidDto::uid)
        .toList();
  }
}
