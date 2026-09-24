package ch.agridata.agreement.service;

import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.LEGALLY_PERMITTED;
import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.OPENED;
import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PRODUCER_ROLE;

import ch.agridata.agreement.dto.ConsentRequestCreatedDto;
import ch.agridata.agreement.dto.CreateConsentRequestDto;
import ch.agridata.agreement.dto.CreateConsentRequestsForUidDto;
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
import jakarta.ws.rs.NotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
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

  /**
   * Creates the consent requests of a single data producer UID for a data request owned by the current consumer.
   *
   * <p>The UID-based consent request ({@code dataProducerBur} is {@code null}) is created only if it does not exist yet. When BURs are
   * provided, they are validated against the UID's BURs in AGIS and against existing consent requests: an active (non-expired) consent
   * request for the UID and one of the BURs makes the whole request invalid. A new BUR-based consent request is then created for every
   * provided BUR, even if an expired consent request for that UID/BUR combination already exists.
   */
  @RolesAllowed(CONSUMER_ROLE)
  public List<ConsentRequestCreatedDto> createConsentRequestsForDataRequestAsCurrentConsumer(
      UUID dataRequestId,
      CreateConsentRequestsForUidDto createConsentRequestsForUidDto
  ) {
    var uid = createConsentRequestsForUidDto.uid();
    var burs = createConsentRequestsForUidDto.burs() == null ? List.<String>of() : createConsentRequestsForUidDto.burs();
    var consumerUid = identity.getUidOrElseThrow();

    var relationSinceByBur = burs.isEmpty() ? Map.<String, LocalDateTime>of() : resolveAndValidateBurs(uid, burs);

    return sessionFactory.fromTransaction(state -> {
      var dataRequest = dataRequestRepository.findByIdAndDataConsumerUid(dataRequestId, consumerUid)
          .orElseThrow(() -> new NotFoundException(dataRequestId.toString()));
      if (!DataRequestEntity.DataRequestStateEnum.ACTIVE.equals(dataRequest.getStateCode())) {
        throw new IllegalStateException(
            "Data request " + dataRequestId + " must be in ACTIVE state to create a consent request.");
      }
      assertNoActiveBurConsentRequestExists(dataRequestId, uid, burs);

      var consentRequestState = resolveConsentRequestState(dataRequest);
      var createdConsentRequests = new ArrayList<ConsentRequestCreatedDto>();
      createdConsentRequests.add(createConsentRequestIfMissing(dataRequest, consentRequestState, uid, null, null));
      burs.forEach(bur ->
          createdConsentRequests.add(createConsentRequest(dataRequest, consentRequestState, uid, bur, relationSinceByBur.get(bur))));

      if (!burs.isEmpty()) {
        consentRequestSyncService.syncUidConsentRequestStateWithBurConsentRequests(dataRequest.getId(), uid);
      }
      return createdConsentRequests;
    });
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
    var consentRequestState = resolveConsentRequestState(products);

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

  /**
   * Fetches the UID's BURs from AGIS, ensures every provided BUR belongs to the UID, and returns the UID-to-BUR relation start date per
   * provided BUR.
   */
  private Map<String, LocalDateTime> resolveAndValidateBurs(String uid, List<String> burs) {
    var relationSinceByBur = userApi.getAuthorizedBurs(uid).stream()
        .collect(Collectors.toMap(BurDto::bur, BurDto::relationSince, (first, second) -> first));

    var unknownBurs = burs.stream()
        .filter(bur -> !relationSinceByBur.containsKey(bur))
        .toList();
    if (!unknownBurs.isEmpty()) {
      throw new IllegalArgumentException("The following burs do not belong to uid " + uid + " according to AGIS: " + unknownBurs);
    }

    return relationSinceByBur;
  }

  private void assertNoActiveBurConsentRequestExists(UUID dataRequestId, String uid, List<String> burs) {
    if (burs.isEmpty()) {
      return;
    }
    var activeBurs = consentRequestRepository.findActiveBurBasedByDataRequestIdAndDataProducerBurs(dataRequestId, burs).stream()
        .filter(consentRequest -> uid.equals(consentRequest.getDataProducerUid()))
        .map(ConsentRequestEntity::getDataProducerBur)
        .distinct()
        .toList();
    if (!activeBurs.isEmpty()) {
      throw new IllegalStateException(
          "An active consent request already exists for uid " + uid + " and burs: " + activeBurs);
    }
  }

  private ConsentRequestEntity.StateEnum resolveConsentRequestState(DataRequestEntity dataRequest) {
    return resolveConsentRequestState(loadProducts(dataRequest));
  }

  private ConsentRequestEntity.StateEnum resolveConsentRequestState(List<DataProductDto> products) {
    return products.stream().anyMatch(DataProductDto::consentRequired) ? OPENED : LEGALLY_PERMITTED;
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

    return createConsentRequest(dataRequest, consentRequestState, uid, bur, uidBurRelationSince);
  }

  private ConsentRequestCreatedDto createConsentRequest(
      DataRequestEntity dataRequest,
      ConsentRequestEntity.StateEnum consentRequestState,
      String uid,
      String bur,
      LocalDateTime uidBurRelationSince
  ) {
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
