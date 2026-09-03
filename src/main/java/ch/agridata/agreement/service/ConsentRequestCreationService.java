package ch.agridata.agreement.service;

import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.LEGALLY_PERMITTED;
import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.OPENED;
import static ch.agridata.common.utils.AuthenticationUtil.PRODUCER_ROLE;

import ch.agridata.agreement.dto.ConsentRequestCreatedDto;
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
import ch.agridata.user.dto.UidDto;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;

/**
 * Provides business logic for consent requests. It coordinates creation, validation, and updates across related entities.
 *
 * @CommentLastReviewed 2026-08-31
 */

@ApplicationScoped
@RequiredArgsConstructor
public class ConsentRequestCreationService {

  private final ConsentRequestRepository consentRequestRepository;
  private final ConsentRequestMapper consentRequestMapper;
  private final ConsentRequestSyncService consentRequestSyncService;
  private final AgridataSecurityIdentity identity;
  private final UserApi userApi;
  private final DataRequestRepository dataRequestRepository;
  private final DataProductApi dataProductApi;
  private final SessionFactory sessionFactory;

  @RolesAllowed(PRODUCER_ROLE)
  public List<ConsentRequestCreatedDto> createConsentRequests(List<CreateConsentRequestDto> createConsentRequestDtos) {
    var uids = getAuthorizedUidsAsCurrentProducer();
    assertAllUidsAuthorized(createConsentRequestDtos, uids);

    return sessionFactory.fromTransaction(state ->
        createConsentRequestDtos.stream()
            .map(dto -> createConsentRequestForUidAndAllBurs(dto.dataRequestId(), dto.uid()))
            .flatMap(Collection::stream)
            .toList());
  }

  public void createLegallyPermittedConsentRequestIfMissing(UUID dataRequestId,
                                                            String uid,
                                                            String bur,
                                                            LocalDateTime uidBurRelationSince) {
    sessionFactory.inTransaction(session -> {
      var dataRequest = loadActiveDataRequest(dataRequestId);
      createConsentRequestIfMissing(dataRequest, LEGALLY_PERMITTED, uid, null, null);
      if (bur != null && uidBurRelationSince != null) {
        createConsentRequestIfMissing(dataRequest, LEGALLY_PERMITTED, uid, bur, uidBurRelationSince);
      }
    });
  }

  private void assertAllUidsAuthorized(List<CreateConsentRequestDto> createConsentRequestDtos, List<String> authorizedUids) {
    var unauthorizedUids = createConsentRequestDtos.stream()
        .map(CreateConsentRequestDto::uid)
        .filter(uid -> !authorizedUids.contains(uid))
        .toList();

    if (!unauthorizedUids.isEmpty()) {
      throw new IllegalArgumentException(
          "Current user is not authorized to create consent request for data producer uids: " + unauthorizedUids);
    }
  }

  private List<ConsentRequestCreatedDto> createConsentRequestForUidAndAllBurs(UUID dataRequestId, String uid) {
    var dataRequest = loadActiveDataRequest(dataRequestId);
    var products = loadProducts(dataRequest);
    var hasBurProducts = products.stream().map(DataProductDto::flowCode).anyMatch(FlowCodeEnum::isBurBased);
    var isConsentRequired = products.stream().anyMatch(DataProductDto::consentRequired);
    var consentRequestState = isConsentRequired ? OPENED : LEGALLY_PERMITTED;

    List<ConsentRequestCreatedDto> createdConsentRequests = new ArrayList<>();
    createdConsentRequests.add(createConsentRequestIfMissing(dataRequest, consentRequestState, uid, null, null));

    if (hasBurProducts) {
      userApi.getAuthorizedBurs(uid).stream()
          .map(bur -> createConsentRequestIfMissing(dataRequest, consentRequestState, bur.uid(), bur.bur(), bur.relationSince()))
          .forEach(createdConsentRequests::add);
      consentRequestSyncService.syncUidConsentRequestStateWithBurConsentRequests(dataRequest.getId(), uid);
    }

    return createdConsentRequests;
  }

  private DataRequestEntity loadActiveDataRequest(UUID dataRequestId) {
    var dataRequest = dataRequestRepository.findByIdOptional(dataRequestId)
        .orElseThrow(() -> new NotFoundException(dataRequestId.toString()));

    if (!DataRequestEntity.DataRequestStateEnum.ACTIVE.equals(dataRequest.getStateCode())) {
      throw new IllegalStateException("Data request " + dataRequestId + " must be in ACTIVE state to create a consent request.");
    }
    return dataRequest;
  }

  private List<DataProductDto> loadProducts(DataRequestEntity dataRequest) {
    var dataProductIds = dataRequest.getDataProducts().stream()
        .map(DataRequestDataProductEntity::getDataProductId)
        .toList();

    var products = dataProductApi.getActiveProductsByIds(dataProductIds);
    if (products.isEmpty()) {
      throw new IllegalStateException("DataRequest with id=" + dataRequest.getId() + " has no active data products.");
    }
    return products;
  }

  private ConsentRequestCreatedDto createConsentRequestIfMissing(
      DataRequestEntity dataRequest,
      ConsentRequestEntity.StateEnum consentRequestState,
      String uid,
      String bur,
      LocalDateTime uidBurRelationSince
  ) {
    var existingConsentRequest =
        consentRequestRepository.findActiveUidAndBurBasedByDataRequestIdAndDataProducerUid(dataRequest.getId(), uid).stream()
            .filter(cr -> (cr.getDataProducerUid().equals(uid) && Objects.equals(cr.getDataProducerBur(), bur)))
            .findAny();

    if (existingConsentRequest.isPresent()) {
      return consentRequestMapper.toConsentRequestCreatedDto(existingConsentRequest.get(), false);
    }

    var consentRequestEntity = ConsentRequestEntity.builder()
        .requestDate(LocalDateTime.now())
        .dataRequest(dataRequest)
        .dataProducerUid(uid)
        .dataProducerBur(bur)
        .uidBurRelationSince(uidBurRelationSince)
        .stateCode(consentRequestState)
        .build();
    consentRequestRepository.persist(consentRequestEntity);
    return consentRequestMapper.toConsentRequestCreatedDto(consentRequestEntity, true);
  }

  private List<String> getAuthorizedUidsAsCurrentProducer() {
    return userApi.getAuthorizedUids(identity.getKtIdP(), identity.getAgateLoginId()).stream()
        .map(UidDto::uid)
        .toList();
  }
}
