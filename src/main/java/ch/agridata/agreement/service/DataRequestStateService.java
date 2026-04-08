package ch.agridata.agreement.service;

import static ch.agridata.agreement.persistence.DataRequestEntity.DataRequestStateEnum.ACTIVE;
import static ch.agridata.agreement.persistence.DataRequestEntity.DataRequestStateEnum.DRAFT;
import static ch.agridata.agreement.persistence.DataRequestEntity.DataRequestStateEnum.IN_REVIEW;
import static ch.agridata.agreement.persistence.DataRequestEntity.DataRequestStateEnum.TO_BE_ACTIVATED;
import static ch.agridata.agreement.persistence.DataRequestEntity.DataRequestStateEnum.TO_BE_RELEASED_BY_CONSUMER;
import static ch.agridata.agreement.persistence.DataRequestEntity.DataRequestStateEnum.TO_BE_RELEASED_BY_PROVIDER;
import static ch.agridata.agreement.persistence.DataRequestEntity.DataRequestStateEnum.TO_BE_SIGNED_BY_CONSUMER;
import static ch.agridata.agreement.persistence.DataRequestEntity.DataRequestStateEnum.TO_BE_SIGNED_BY_PROVIDER;
import static ch.agridata.common.utils.AuthenticationUtil.ADMIN_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PROVIDER_ROLE;

import ch.agridata.agreement.dto.DataRequestDto;
import ch.agridata.agreement.dto.DataRequestStateEnum;
import ch.agridata.agreement.mapper.DataRequestMapper;
import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.agreement.persistence.DataRequestRepository;
import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.common.utils.ValidationSchemaGenerator;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import jakarta.ws.rs.NotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Manages state transitions of data requests and enforces transition rules. Handles validation and submission logic related to changing
 * request states.
 *
 * @CommentLastReviewed 2026-04-01
 */
@ApplicationScoped
@RequiredArgsConstructor
public class DataRequestStateService {

  private final DataRequestQueryService dataRequestQueryService;

  private static final List<AllowedTransition> ALLOWED_TRANSITIONS = List.of(
      new AllowedTransition(DRAFT, IN_REVIEW, Set.of(Actor.CONSUMER)),
      new AllowedTransition(IN_REVIEW, DRAFT, Set.of(Actor.CONSUMER, Actor.ADMIN)),
      new AllowedTransition(IN_REVIEW, TO_BE_SIGNED_BY_CONSUMER, Set.of(Actor.ADMIN)),
      new AllowedTransition(TO_BE_SIGNED_BY_CONSUMER, DRAFT, Set.of(Actor.CONSUMER)),
      new AllowedTransition(TO_BE_SIGNED_BY_CONSUMER, TO_BE_RELEASED_BY_CONSUMER, Set.of(Actor.SYSTEM)),
      new AllowedTransition(TO_BE_RELEASED_BY_CONSUMER, DRAFT, Set.of(Actor.CONSUMER)),
      new AllowedTransition(TO_BE_RELEASED_BY_CONSUMER, TO_BE_SIGNED_BY_PROVIDER, Set.of(Actor.CONSUMER)),
      new AllowedTransition(TO_BE_SIGNED_BY_PROVIDER, DRAFT, Set.of(Actor.CONSUMER, Actor.ADMIN)),
      new AllowedTransition(TO_BE_SIGNED_BY_PROVIDER, TO_BE_RELEASED_BY_PROVIDER, Set.of(Actor.SYSTEM)),
      new AllowedTransition(TO_BE_RELEASED_BY_PROVIDER, DRAFT, Set.of(Actor.CONSUMER, Actor.ADMIN)),
      new AllowedTransition(TO_BE_RELEASED_BY_PROVIDER, TO_BE_ACTIVATED, Set.of(Actor.PROVIDER)),
      new AllowedTransition(TO_BE_ACTIVATED, ACTIVE, Set.of(Actor.ADMIN))
  );

  private enum Actor {
    CONSUMER, ADMIN, SYSTEM, PROVIDER
  }

  private record AllowedTransition(
      DataRequestEntity.DataRequestStateEnum from,
      DataRequestEntity.DataRequestStateEnum to,
      Set<Actor> allowedActors
  ) {
  }

  private final DataRequestRepository dataRequestRepository;
  private final DataRequestMapper dataRequestMapper;
  private final AgridataSecurityIdentity agridataSecurityIdentity;
  private final Validator validator;
  private final DataRequestStateAuditService dataRequestStateAuditService;
  private final DataRequestEnrichmentService dataRequestEnrichmentService;
  private final ContractRevisionInitializationService contractRevisionInitializationService;

  @Transactional
  @RolesAllowed(CONSUMER_ROLE)
  public DataRequestDto setStateAsConsumer(UUID requestId, DataRequestStateEnum state) {
    var entity = loadEntityForConsumer(requestId);
    var oldStateCode = entity.getStateCode();
    var newStateCode = toEntityState(state);

    if (oldStateCode == DRAFT && newStateCode == IN_REVIEW) {
      validate(dataRequestMapper.toUpdateDto(entity), ValidationSchemaGenerator.Submit.class);
    }

    var dto = setStateTo(entity, newStateCode, Actor.CONSUMER);
    dataRequestStateAuditService.auditConsumerStatusTransition(requestId, oldStateCode, newStateCode);
    return dto;
  }

  @Transactional
  @RolesAllowed(PROVIDER_ROLE)
  public DataRequestDto setStateAsProvider(UUID requestId, DataRequestStateEnum state) {
    var entity = loadEntityForProvider(requestId);
    var newStateCode = toEntityState(state);

    return setStateTo(entity, newStateCode, Actor.PROVIDER);
  }

  @RolesAllowed(ADMIN_ROLE)
  @Transactional
  public DataRequestDto setStateAsAdmin(UUID requestId, DataRequestStateEnum state) {
    var entity = loadEntityForAdmin(requestId);
    var oldStateCode = entity.getStateCode();
    var newStateCode = toEntityState(state);

    var dto = setStateTo(entity, newStateCode, Actor.ADMIN);
    dataRequestStateAuditService.auditAdminStatusTransition(requestId, oldStateCode, newStateCode);
    return dto;
  }

  public void transitionToPendingReleaseByConsumer(DataRequestEntity entity) {
    setStateTo(entity, TO_BE_RELEASED_BY_CONSUMER, Actor.SYSTEM);
  }

  public void transitionToPendingReleaseByProvider(DataRequestEntity entity) {
    setStateTo(entity, TO_BE_RELEASED_BY_PROVIDER, Actor.SYSTEM);
  }

  private DataRequestDto setStateTo(DataRequestEntity entity,
                                    DataRequestEntity.DataRequestStateEnum newStateCode,
                                    Actor actor) {
    verifyStatusTransition(entity.getStateCode(), newStateCode, actor);
    entity.setStateCode(newStateCode);

    if (newStateCode == DRAFT) {
      entity.setSubmissionDate(null);
      entity.setCurrentContractRevisionId(null);
    } else if (newStateCode == IN_REVIEW) {
      entity.setSubmissionDate(LocalDateTime.now());
    } else if (newStateCode == TO_BE_SIGNED_BY_CONSUMER) {
      contractRevisionInitializationService.createAndAssignInitialRevision(entity);
    }

    dataRequestRepository.persist(entity);
    return dataRequestEnrichmentService.toEnrichedDto(entity);
  }

  private <T> void validate(T object, Class<?>... groups) {
    Set<ConstraintViolation<T>> violations = validator.validate(object, groups);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }
  }

  private void verifyStatusTransition(
      DataRequestEntity.DataRequestStateEnum from,
      DataRequestEntity.DataRequestStateEnum to,
      Actor actor) {
    boolean allowed = ALLOWED_TRANSITIONS.stream()
        .anyMatch(t -> t.from() == from && t.to() == to && t.allowedActors().contains(actor));
    if (!allowed) {
      throw new IllegalStateException(
          "Unable to transition data request from state: " + from + " to: " + to + " as " + actor);
    }
  }

  private static DataRequestEntity.@NotNull DataRequestStateEnum toEntityState(DataRequestStateEnum state) {
    return DataRequestEntity.DataRequestStateEnum.valueOf(state.name());
  }

  private DataRequestEntity loadEntityForConsumer(UUID requestId) {
    return dataRequestRepository.findByIdAndDataConsumerUid(requestId, agridataSecurityIdentity.getUidOrElseThrow())
        .orElseThrow(() -> new NotFoundException(requestId.toString()));
  }

  private DataRequestEntity loadEntityForProvider(UUID requestId) {
    return dataRequestRepository.findByIdOptional(requestId)
        .filter(dataRequestQueryService::isAssignedToCurrentProvider)
        .orElseThrow(() -> new NotFoundException(requestId.toString()));
  }

  private DataRequestEntity loadEntityForAdmin(UUID requestId) {
    return dataRequestRepository.findByIdOptional(requestId)
        .orElseThrow(() -> new NotFoundException(requestId.toString()));
  }

}
