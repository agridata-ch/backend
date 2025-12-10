package ch.agridata.agreement.service;

import static ch.agridata.agreement.dto.DataRequestStateEnum.DRAFT;
import static ch.agridata.agreement.dto.DataRequestStateEnum.IN_REVIEW;
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

/**
 * Manages state transitions of data requests and enforces transition rules. Handles validation and submission logic related to changing
 * request states.
 *
 * @CommentLastReviewed 2025-08-25
 */

@ApplicationScoped
@RequiredArgsConstructor
public class DataRequestStateService {

  public static final Map<DataRequestEntity.DataRequestStateEnum, Set<DataRequestEntity.DataRequestStateEnum>> ALLOWED_TRANSITIONS = Map.of(
      DataRequestEntity.DataRequestStateEnum.DRAFT,
      Set.of(DataRequestEntity.DataRequestStateEnum.IN_REVIEW),
      DataRequestEntity.DataRequestStateEnum.IN_REVIEW,
      Set.of(DataRequestEntity.DataRequestStateEnum.DRAFT, DataRequestEntity.DataRequestStateEnum.TO_BE_SIGNED),
      DataRequestEntity.DataRequestStateEnum.TO_BE_SIGNED,
      Set.of(DataRequestEntity.DataRequestStateEnum.ACTIVE),
      DataRequestEntity.DataRequestStateEnum.ACTIVE,
      Set.of()
  );

  private final DataRequestRepository dataRequestRepository;
  private final DataRequestMapper dataRequestMapper;
  private final AgridataSecurityIdentity agridataSecurityIdentity;
  private final Validator validator;

  public static void verifyStatusTransition(DataRequestEntity.DataRequestStateEnum from, DataRequestEntity.DataRequestStateEnum to) {
    if (!ALLOWED_TRANSITIONS.getOrDefault(from, Set.of()).contains(to)) {
      throw new IllegalStateException(
          "Unable to transition data request from state: " + from + " to: " + to);
    }
  }

  @Transactional
  @RolesAllowed(CONSUMER_ROLE)
  public DataRequestDto setStateAsConsumer(UUID requestId, DataRequestStateEnum state) {
    if (!List.of(DRAFT, IN_REVIEW).contains(state)) {
      throw new IllegalStateException("Only DRAFT and IN_REVIEW state can be set by consumer");
    }
    var entity = dataRequestRepository.findByIdAndDataConsumerUid(requestId, agridataSecurityIdentity.getUidOrElseThrow())
        .orElseThrow(() -> new NotFoundException(requestId.toString()));
    var dataRequestDto = dataRequestMapper.toUpdateDto(entity);
    var newStateCode = DataRequestEntity.DataRequestStateEnum.valueOf(state.name());

    validate(dataRequestDto, ValidationSchemaGenerator.Submit.class);
    return setStateTo(entity, newStateCode);

  }

  @RolesAllowed(ADMIN_ROLE)
  @Transactional
  public DataRequestDto setStateAsAdmin(UUID requestId, DataRequestStateEnum state) {
    var entity = dataRequestRepository.findByIdOptional(requestId)
        .orElseThrow(() -> new NotFoundException(requestId.toString()));

    var oldStateCode = DataRequestStateEnum.valueOf(entity.getStateCode().name());
    if (DRAFT == oldStateCode) {
      throw new IllegalStateException(
          "Cannot change state from DRAFT - data request must be set to IN_REVIEW by the consumer first");
    }

    var newStateCode = DataRequestEntity.DataRequestStateEnum.valueOf(state.name());

    return setStateTo(entity, newStateCode);
  }

  private DataRequestDto setStateTo(DataRequestEntity entity, DataRequestEntity.DataRequestStateEnum newStateDto) {
    var newStateCode = DataRequestEntity.DataRequestStateEnum.valueOf(newStateDto.name());

    verifyStatusTransition(entity.getStateCode(), newStateCode);

    entity.setStateCode(newStateCode);

    if (newStateCode == DataRequestEntity.DataRequestStateEnum.DRAFT) {
      entity.setSubmissionDate(null);
    } else if (newStateCode == DataRequestEntity.DataRequestStateEnum.IN_REVIEW) {
      entity.setSubmissionDate(LocalDateTime.now());
    }

    dataRequestRepository.persist(entity);
    return dataRequestMapper.toDto(entity);
  }

  private <T> void validate(T object, Class<?>... groups) {
    Set<ConstraintViolation<T>> violations = validator.validate(object, groups);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }
  }
}
