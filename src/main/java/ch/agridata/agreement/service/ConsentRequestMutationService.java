package ch.agridata.agreement.service;

import static ch.agridata.common.utils.AuthenticationUtil.PRODUCER_ROLE;

import ch.agridata.agreement.dto.ConsentRequestCreatedDto;
import ch.agridata.agreement.dto.ConsentRequestStateEnum;
import ch.agridata.agreement.dto.CreateConsentRequestDto;
import ch.agridata.agreement.mapper.ConsentRequestMapper;
import ch.agridata.agreement.persistence.ConsentRequestEntity;
import ch.agridata.agreement.persistence.ConsentRequestRepository;
import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.agreement.persistence.DataRequestRepository;
import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.user.api.UserApi;
import ch.agridata.user.dto.UidDto;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;

/**
 * Provides business logic for consent requests. It coordinates creation, validation, and updates across related entities.
 *
 * @CommentLastReviewed 2025-10-23
 */

@ApplicationScoped
@RequiredArgsConstructor
public class ConsentRequestMutationService {

  private final ConsentRequestRepository consentRequestRepository;
  private final ConsentRequestMapper consentRequestMapper;
  private final ConsentRequestStateService consentRequestStateService;
  private final AuditingService auditingService;
  private final AgridataSecurityIdentity identity;
  private final UserApi userApi;
  private final DataRequestRepository dataRequestRepository;
  private final SessionFactory sessionFactory;

  @RolesAllowed(PRODUCER_ROLE)
  @Transactional
  public void updateConsentRequestStateAsCurrentDataProducer(UUID consentRequestId,
                                                             ConsentRequestStateEnum state) {
    var uids = getAuthorizedUids(identity.getKtIdpOfUserOrImpersonatedUser());
    var consentRequestEntity = consentRequestRepository.findByIdAndDataProducerUids(consentRequestId, uids)
        .orElseThrow(() -> new NotFoundException(consentRequestId.toString()));
    var targetState = consentRequestMapper.toEntityStateEnum(state);

    consentRequestStateService.verifyStatusTransition(
        consentRequestEntity.getStateCode(),
        targetState,
        consentRequestEntity.getLastStateChangeDate());
    consentRequestEntity.setStateCode(consentRequestMapper.toEntityStateEnum(state));
    addLogEntry(targetState, consentRequestEntity.getId());
  }

  public List<ConsentRequestCreatedDto> createConsentRequestForDataRequest(List<CreateConsentRequestDto> createConsentRequestDtos) {
    var uids = getAuthorizedUids(identity.getKtIdpOfUserOrImpersonatedUser());

    return sessionFactory.fromTransaction(state ->
        createConsentRequestDtos.stream().map(dto -> {
          if (!uids.contains(dto.uid())) {
            throw new IllegalArgumentException(
                "Current user is not authorized to create consent request for data producer UID: " + dto.uid());
          }

          var dataRequest =
              dataRequestRepository.findByIdOptional(dto.dataRequestId())
                  .orElseThrow(() -> new NotFoundException(dto.dataRequestId().toString()));

          if (!DataRequestEntity.DataRequestStateEnum.ACTIVE.equals(dataRequest.getStateCode())) {
            throw new IllegalStateException(
                "Data request: " + dto.dataRequestId() + " must be in ACTIVE state to create a consent request.");
          }

          return consentRequestRepository.findByDataRequestIdAndDataProducerUid(dto.dataRequestId(), dto.uid())
              .map(existingRequest -> consentRequestMapper.toConsentRequestCreatedDto(existingRequest, false))
              .orElseGet(() ->
                  createConsentRequest(dto.uid(), dataRequest)
              );
        }).toList()
    );


  }

  private ConsentRequestCreatedDto createConsentRequest(String uid, DataRequestEntity dataRequest) {
    var consentRequestEntity = ConsentRequestEntity.builder()
        .requestDate(LocalDateTime.now())
        .dataRequest(dataRequest)
        .dataProducerUid(uid)
        .stateCode(ConsentRequestEntity.StateEnum.OPENED)
        .build();
    consentRequestRepository.persist(consentRequestEntity);
    return consentRequestMapper.toConsentRequestCreatedDto(consentRequestEntity, true);
  }

  private void addLogEntry(ConsentRequestEntity.StateEnum state, UUID entityId) {
    switch (state) {
      case GRANTED -> auditingService.logConsentRequestGranted(entityId);
      case DECLINED -> auditingService.logConsentRequestDeclined(entityId);
      case OPENED -> auditingService.logConsentRequestReopened(entityId);
    }
  }

  private List<String> getAuthorizedUids(String ktIdP) {
    return userApi.getAuthorizedUids(ktIdP).stream().map(UidDto::uid).toList();
  }
}
