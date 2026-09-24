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
 * @CommentLastReviewed 2026-09-29
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
   * <p>The UID-based consent request ({@code dataProducerBur} is {@code null}) is created only if it does not exist yet. BURs may only be
   * provided if the data request contains at least one BUR-based data product. When BURs are provided, they are validated against the
   * UID's BURs in AGIS and against existing consent requests: an active (non-expired) consent
   * request for the UID and one of the BURs makes the whole request invalid. A new BUR-based consent request is then created for every
   * provided BUR, even if an expired consent request for that UID/BUR combination already exists.
   */
  @RolesAllowed(CONSUMER_ROLE)
  public List<ConsentRequestCreatedDto> createConsentRequestsForDataRequestAsCurrentConsumer(
      UUID dataRequestId,
      CreateConsentRequestsForUidDto createConsentRequestsForUidDto
  ) {
    var uid = createConsentRequestsForUidDto.uid();
    var burs = createConsentRequestsForUidDto.burs().stream().distinct().toList();
    var consumerUid = identity.getUidOrElseThrow();

    // Two separate transactions, so no DB connection is held during the AGIS call in resolveBursForCreation.
    var products = sessionFactory.fromTransaction(state -> loadProducts(loadActiveDataRequestOfConsumer(dataRequestId, consumerUid)));
    var relationSinceByBur = resolveBursForCreation(products, uid, burs);

    return sessionFactory.fromTransaction(state -> {
      var dataRequest = loadActiveDataRequestOfConsumer(dataRequestId, consumerUid);
      assertNoActiveConsentRequestExistsForBurs(dataRequestId, uid, burs);

      var consentRequestState = resolveConsentRequestState(products);
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
    var hasBurProducts = hasBurBasedProducts(products);
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
   * Resolves and validates the provided BURs for creation. Must only be called once the current consumer's ownership of the ACTIVE data
   * request has been verified, so an unauthorized caller neither triggers the AGIS lookup nor learns whether the provided BURs belong to
   * the UID. When no BURs are provided, AGIS is not called.
   */
  private Map<String, LocalDateTime> resolveBursForCreation(List<DataProductDto> products, String uid, List<String> burs) {
    if (burs.isEmpty()) {
      return Map.of();
    }
    if (!hasBurBasedProducts(products)) {
      throw new IllegalArgumentException("The data request has no BUR-based data products, so no burs may be provided.");
    }
    return resolveAndValidateBurs(uid, burs);
  }

  private DataRequestEntity loadActiveDataRequestOfConsumer(UUID dataRequestId, String consumerUid) {
    var dataRequest = dataRequestRepository.findByIdAndDataConsumerUid(dataRequestId, consumerUid)
        .orElseThrow(() -> new NotFoundException(dataRequestId.toString()));
    assertActive(dataRequest);
    return dataRequest;
  }

  private boolean hasBurBasedProducts(List<DataProductDto> products) {
    return products.stream()
        .map(DataProductDto::flowCode)
        .anyMatch(FlowCodeEnum::isBurBased);
  }

  private void assertActive(DataRequestEntity dataRequest) {
    if (!DataRequestEntity.DataRequestStateEnum.ACTIVE.equals(dataRequest.getStateCode())) {
      throw new IllegalStateException(
          "Data request " + dataRequest.getId() + " must be in ACTIVE state to create a consent request.");
    }
  }

  /**
   * Fetches the UID's BURs from AGIS, ensures every provided BUR belongs to the UID, and returns the UID-to-BUR relation start date of
   * each provided BUR.
   */
  private Map<String, LocalDateTime> resolveAndValidateBurs(String uid, List<String> burs) {
    var relationSinceByBur = userApi.getAuthorizedBurs(uid).stream()
        .filter(authorizedBur -> burs.contains(authorizedBur.bur()))
        .collect(Collectors.toMap(BurDto::bur, BurDto::relationSince, (first, second) -> first));

    var unknownBurs = burs.stream()
        .filter(bur -> !relationSinceByBur.containsKey(bur))
        .toList();
    if (!unknownBurs.isEmpty()) {
      throw new IllegalArgumentException("The following burs do not belong to uid " + uid + " according to AGIS: " + unknownBurs);
    }

    return relationSinceByBur;
  }

  private void assertNoActiveConsentRequestExistsForBurs(UUID dataRequestId, String uid, List<String> burs) {
    if (burs.isEmpty()) {
      return;
    }
    var activeBurs = consentRequestRepository.findActiveBurBasedByDataRequestIdAndDataProducerUidAndBurs(dataRequestId, uid, burs)
        .stream()
        .map(ConsentRequestEntity::getDataProducerBur)
        .distinct()
        .toList();
    if (!activeBurs.isEmpty()) {
      throw new IllegalStateException(
          "An active consent request already exists for uid " + uid + " and burs: " + activeBurs);
    }
  }

  private ConsentRequestEntity.StateEnum resolveConsentRequestState(List<DataProductDto> products) {
    return products.stream().anyMatch(DataProductDto::consentRequired) ? OPENED : LEGALLY_PERMITTED;
  }

  private DataRequestEntity loadActiveDataRequest(UUID dataRequestId) {
    var dataRequest = dataRequestRepository.findByIdOptional(dataRequestId)
        .orElseThrow(() -> new NotFoundException(dataRequestId.toString()));
    assertActive(dataRequest);
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
