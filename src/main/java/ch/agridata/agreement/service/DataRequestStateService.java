package ch.agridata.agreement.service;

import static ch.agridata.agreement.dto.DataRequestStateEnum.DRAFT;
import static ch.agridata.agreement.dto.DataRequestStateEnum.IN_REVIEW;
import static ch.agridata.agreement.dto.DataRequestStateEnum.TO_BE_SIGNED_BY_PROVIDER;
import static ch.agridata.common.utils.AuthenticationUtil.ADMIN_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;

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
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Manages state transitions of data requests and enforces transition rules. Handles validation and submission logic related to changing
 * request states.
 *
 * @CommentLastReviewed 2026-01-22
 */

@ApplicationScoped
@RequiredArgsConstructor
public class DataRequestStateService {

  public static final Map<DataRequestEntity.DataRequestStateEnum, Set<DataRequestEntity.DataRequestStateEnum>> ALLOWED_TRANSITIONS = Map.of(
      DataRequestEntity.DataRequestStateEnum.DRAFT,
      Set.of(DataRequestEntity.DataRequestStateEnum.IN_REVIEW),
      DataRequestEntity.DataRequestStateEnum.IN_REVIEW,
      Set.of(DataRequestEntity.DataRequestStateEnum.DRAFT, DataRequestEntity.DataRequestStateEnum.TO_BE_SIGNED_BY_CONSUMER),
      DataRequestEntity.DataRequestStateEnum.TO_BE_SIGNED_BY_CONSUMER,
      Set.of(DataRequestEntity.DataRequestStateEnum.DRAFT),
      DataRequestEntity.DataRequestStateEnum.TO_BE_RELEASED_BY_CONSUMER,
      Set.of(DataRequestEntity.DataRequestStateEnum.DRAFT, DataRequestEntity.DataRequestStateEnum.TO_BE_SIGNED_BY_PROVIDER),
      DataRequestEntity.DataRequestStateEnum.TO_BE_SIGNED_BY_PROVIDER,
      Set.of(DataRequestEntity.DataRequestStateEnum.DRAFT, DataRequestEntity.DataRequestStateEnum.ACTIVE),
      DataRequestEntity.DataRequestStateEnum.ACTIVE,
      Set.of()
  );

  private final DataRequestRepository dataRequestRepository;
  private final DataRequestMapper dataRequestMapper;
  private final AgridataSecurityIdentity agridataSecurityIdentity;
  private final Validator validator;
  private final AuditingService auditingService;
  private final DataRequestEnrichmentService dataRequestEnrichmentService;
  private final ContractRevisionInitializationService contractRevisionInitializationService;


  public static void verifyStatusTransition(DataRequestEntity.DataRequestStateEnum from, DataRequestEntity.DataRequestStateEnum to) {
    if (!ALLOWED_TRANSITIONS.getOrDefault(from, Set.of()).contains(to)) {
      throw new IllegalStateException(
          "Unable to transition data request from state: " + from + " to: " + to);
    }
  }

  @Transactional
  @RolesAllowed(CONSUMER_ROLE)
  public DataRequestDto setStateAsConsumer(UUID requestId, DataRequestStateEnum state) {
    verifyConsumerStateAllowed(state);
    var entity = loadEntityForConsumer(requestId);
    var oldStateCode = entity.getStateCode();
    var newStateCode = toEntityState(state);

    if (oldStateCode == DataRequestEntity.DataRequestStateEnum.DRAFT
        && newStateCode == DataRequestEntity.DataRequestStateEnum.IN_REVIEW) {
      validate(dataRequestMapper.toUpdateDto(entity), ValidationSchemaGenerator.Submit.class);
    }

    verifyStatusTransition(oldStateCode, newStateCode);

    var dto = setStateTo(entity, newStateCode);

    auditConsumerStatusTransition(requestId, oldStateCode, newStateCode);

    return dto;
  }

  @RolesAllowed(ADMIN_ROLE)
  @Transactional
  public DataRequestDto setStateAsAdmin(UUID requestId, DataRequestStateEnum state) {
    var entity = loadEntityForAdmin(requestId);

    var oldStateCode = entity.getStateCode();
    var newStateCode = toEntityState(state);

    verifyAdminMayChangeFrom(oldStateCode);
    verifyStatusTransition(oldStateCode, newStateCode);

    if (oldStateCode == DataRequestEntity.DataRequestStateEnum.IN_REVIEW
        && newStateCode == DataRequestEntity.DataRequestStateEnum.TO_BE_SIGNED_BY_CONSUMER) {
      contractRevisionInitializationService.createAndAssignInitialRevision(entity);
    }

    var dto = setStateTo(entity, newStateCode);

    auditAdminStatusTransition(requestId, oldStateCode, newStateCode);

    return dto;
  }

  private DataRequestDto setStateTo(DataRequestEntity entity, DataRequestEntity.DataRequestStateEnum newStateCode) {

    entity.setStateCode(newStateCode);

    if (newStateCode == DataRequestEntity.DataRequestStateEnum.DRAFT) {
      entity.setSubmissionDate(null);
      entity.setCurrentContractRevisionId(null);
    } else if (newStateCode == DataRequestEntity.DataRequestStateEnum.IN_REVIEW) {
      entity.setSubmissionDate(LocalDateTime.now());
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

  private static void verifyConsumerStateAllowed(DataRequestStateEnum state) {
    if (!List.of(DRAFT, IN_REVIEW, TO_BE_SIGNED_BY_PROVIDER).contains(state)) {
      throw new IllegalStateException("Only DRAFT, IN_REVIEW and TO_BE_SIGNED_BY_PROVIDER state can be set by consumer");
    }
  }

  private static DataRequestEntity.@NotNull DataRequestStateEnum toEntityState(DataRequestStateEnum state) {
    return DataRequestEntity.DataRequestStateEnum.valueOf(state.name());
  }

  private DataRequestEntity loadEntityForConsumer(UUID requestId) {
    return dataRequestRepository.findByIdAndDataConsumerUid(requestId, agridataSecurityIdentity.getUidOrElseThrow())
        .orElseThrow(() -> new NotFoundException(requestId.toString()));
  }

  private void auditAdminStatusTransition(UUID requestId, DataRequestEntity.DataRequestStateEnum oldStateCode,
                                          DataRequestEntity.DataRequestStateEnum newStateCode) {
    if (oldStateCode == DataRequestEntity.DataRequestStateEnum.IN_REVIEW && newStateCode == DataRequestEntity.DataRequestStateEnum.DRAFT) {
      auditingService.logDataRequestRejected(requestId);
    } else if (oldStateCode == DataRequestEntity.DataRequestStateEnum.IN_REVIEW
        && newStateCode == DataRequestEntity.DataRequestStateEnum.TO_BE_SIGNED_BY_CONSUMER) {
      auditingService.logDataRequestApproved(requestId);
    } else if (oldStateCode == DataRequestEntity.DataRequestStateEnum.TO_BE_SIGNED_BY_PROVIDER
        && newStateCode == DataRequestEntity.DataRequestStateEnum.ACTIVE) {
      auditingService.logDataRequestActivated(requestId);
    }
  }

  private static void verifyAdminMayChangeFrom(DataRequestEntity.DataRequestStateEnum oldStateCode) {
    if (DataRequestEntity.DataRequestStateEnum.DRAFT == oldStateCode) {
      throw new IllegalStateException(
          "Cannot change state from DRAFT - data request must be set to IN_REVIEW by the consumer first");
    }
    if (DataRequestEntity.DataRequestStateEnum.TO_BE_SIGNED_BY_CONSUMER == oldStateCode
        || DataRequestEntity.DataRequestStateEnum.TO_BE_RELEASED_BY_CONSUMER == oldStateCode) {
      throw new IllegalStateException(
          "Cannot change state from " + oldStateCode.name()
              + " - data request must be set to TO_BE_SIGNED_BY_PROVIDER by the consumer first"
      );
    }
  }

  private DataRequestEntity loadEntityForAdmin(UUID requestId) {
    return dataRequestRepository.findByIdOptional(requestId)
        .orElseThrow(() -> new NotFoundException(requestId.toString()));
  }

  private void auditConsumerStatusTransition(UUID requestId, DataRequestEntity.DataRequestStateEnum oldStateCode,
                                             DataRequestEntity.DataRequestStateEnum newStateCode) {
    if (oldStateCode == DataRequestEntity.DataRequestStateEnum.IN_REVIEW
        && newStateCode == DataRequestEntity.DataRequestStateEnum.DRAFT) {
      auditingService.logDataRequestWithdrawn(requestId);
    } else if (oldStateCode == DataRequestEntity.DataRequestStateEnum.DRAFT
        && newStateCode == DataRequestEntity.DataRequestStateEnum.IN_REVIEW) {
      auditingService.logDataRequestSubmitted(requestId);
    }
  }

  public void transitionToPendingReleaseByConsumer(DataRequestEntity entity) {
    if (entity.getStateCode() != DataRequestEntity.DataRequestStateEnum.TO_BE_SIGNED_BY_CONSUMER) {
      throw new IllegalStateException(
          "Data request must be in state TO_BE_SIGNED_BY_CONSUMER to transition to TO_BE_RELEASED_BY_CONSUMER"
      );
    }

    entity.setStateCode(DataRequestEntity.DataRequestStateEnum.TO_BE_RELEASED_BY_CONSUMER);
  }
}
