package ch.agridata.agreement.service;

import static ch.agridata.common.utils.AuthenticationUtil.PRODUCER_ROLE;

import ch.agridata.agreement.dto.ConsentRequestCreatedDto;
import ch.agridata.agreement.dto.ConsentRequestStateEnum;
import ch.agridata.agreement.dto.CreateConsentRequestDto;
import ch.agridata.agreement.mapper.ConsentRequestMapper;
import ch.agridata.agreement.persistence.ConsentRequestEntity;
import ch.agridata.agreement.persistence.ConsentRequestRepository;
import ch.agridata.agreement.persistence.DataRequestDataProductEntity;
import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.agreement.persistence.DataRequestRepository;
import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.product.api.DataProductApi;
import ch.agridata.product.dto.DataProductDto;
import ch.agridata.product.dto.FlowCodeEnum;
import ch.agridata.user.api.UserApi;
import ch.agridata.user.dto.BurDto;
import ch.agridata.user.dto.UidDto;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;

/**
 * Provides business logic for consent requests. It coordinates creation, validation, and updates across related entities.
 *
 * @CommentLastReviewed 2026-08-17
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
  private final DataProductApi dataProductApi;
  private final SessionFactory sessionFactory;

  @RolesAllowed(PRODUCER_ROLE)
  @Transactional
  public void updateConsentRequestStateAsCurrentDataProducer(
      UUID consentRequestId,
      ConsentRequestStateEnum state
  ) {
    var uids = getAuthorizedUidsAsCurrentProducer();
    var consentRequestEntity = consentRequestRepository.findByIdAndDataProducerUids(consentRequestId, uids)
        .orElseThrow(() -> new NotFoundException(consentRequestId.toString()));
    var targetState = consentRequestMapper.toEntityStateEnum(state);

    consentRequestStateService.verifyStatusTransition(
        consentRequestEntity.getStateCode(),
        targetState,
        consentRequestEntity.getLastStateChangeDate()
    );
    consentRequestEntity.setStateCode(consentRequestMapper.toEntityStateEnum(state));
    addLogEntry(targetState, consentRequestEntity.getId());
  }

  @RolesAllowed(PRODUCER_ROLE)
  public List<ConsentRequestCreatedDto> createConsentRequestForDataRequest(List<CreateConsentRequestDto> createConsentRequestDtos) {
    var uids = getAuthorizedUidsAsCurrentProducer();
    // Memoized per batch so that repeated UIDs or data requests do not trigger the same lookup twice.
    Map<UUID, Boolean> hasBurProductsByDataRequestId = new HashMap<>();
    Map<String, List<BurDto>> bursByUid = new HashMap<>();

    return sessionFactory.fromTransaction(state ->
        createConsentRequestDtos.stream().map(dto -> {
          if (!uids.contains(dto.uid())) {
            throw new IllegalArgumentException(
                "Current user is not authorized to create consent request for data producer UID: " + dto.uid());
          }

          var dataRequest = dataRequestRepository.findByIdOptional(dto.dataRequestId())
              .orElseThrow(() -> new NotFoundException(dto.dataRequestId().toString()));

          if (!DataRequestEntity.DataRequestStateEnum.ACTIVE.equals(dataRequest.getStateCode())) {
            throw new IllegalStateException("Data request " + dto.dataRequestId()
                + " must be in ACTIVE state to create a consent request.");
          }

          var uidConsentRequest = consentRequestRepository.findNonBurByDataRequestIdAndDataProducerUid(dto.dataRequestId(), dto.uid())
              .map(existingRequest -> consentRequestMapper.toConsentRequestCreatedDto(existingRequest, false))
              .orElseGet(() ->
                  createConsentRequest(dto.uid(), null, null, dataRequest)
              );

          @NonNull var isAbsent = hasBurProductsByDataRequestId.computeIfAbsent(dto.dataRequestId(), id -> hasBurProducts(dataRequest));
          if (isAbsent) {
            createMissingBurConsentRequests(dto.uid(), dataRequest, bursByUid);
          }

          return uidConsentRequest;
        }).toList()
    );
  }

  private boolean hasBurProducts(DataRequestEntity dataRequest) {
    var dataProductIds = dataRequest.getDataProducts().stream()
        .map(DataRequestDataProductEntity::getDataProductId)
        .toList();

    return dataProductApi.getActiveProductsByIds(dataProductIds).stream()
        .map(DataProductDto::flowCode)
        .anyMatch(FlowCodeEnum::isBurBased);
  }

  /**
   * Creates a consent request for every active BUR of the given UID that does not have one yet. A consent request is identified by
   * (data request, UID, BUR and its active state ({@code uidBurRelationUntil == null})); only an active row blocks creation of a new one.
   * A terminated row is historical and must not be rewritten, but active rows are made idempotently.
   */
  private void createMissingBurConsentRequests(String uid, DataRequestEntity dataRequest, Map<String, List<BurDto>> bursByUid) {
    var burDtos = bursByUid.computeIfAbsent(uid, userApi::getAuthorizedBurs);
    if (burDtos.isEmpty()) {
      return;
    }

    var burs = burDtos.stream().map(BurDto::bur).toList();
    var activeBurs = consentRequestRepository
        .findActiveByDataRequestIdAndDataProducerBurs(dataRequest.getId(), burs).stream()
        .filter(consentRequest -> uid.equals(consentRequest.getDataProducerUid()))
        .map(ConsentRequestEntity::getDataProducerBur)
        .collect(Collectors.toSet());

    burDtos.stream()
        .filter(bur -> !activeBurs.contains(bur.bur()))
        .forEach(bur -> createConsentRequest(uid, bur.bur(), bur.relationSince(), dataRequest));
  }

  private ConsentRequestCreatedDto createConsentRequest(
      String uid,
      String bur,
      LocalDateTime uidBurRelationSince,
      DataRequestEntity dataRequest
  ) {
    var consentRequestEntity = ConsentRequestEntity.builder()
        .requestDate(LocalDateTime.now())
        .dataRequest(dataRequest)
        .dataProducerUid(uid)
        .dataProducerBur(bur)
        .uidBurRelationSince(uidBurRelationSince)
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

  private List<String> getAuthorizedUidsAsCurrentProducer() {
    return userApi.getAuthorizedUids(identity.getKtIdP(), identity.getAgateLoginId()).stream()
        .map(UidDto::uid)
        .toList();
  }
}
