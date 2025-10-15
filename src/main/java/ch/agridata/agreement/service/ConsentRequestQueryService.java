package ch.agridata.agreement.service;

import static ch.agridata.common.utils.AuthenticationUtil.ADMIN_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PRODUCER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.SUPPORT_ROLE;

import ch.agridata.agis.api.AgisApi;
import ch.agridata.agreement.api.ConsentRequestApi;
import ch.agridata.agreement.dto.ConsentRequestConsumerViewDto;
import ch.agridata.agreement.dto.ConsentRequestProducerViewDto;
import ch.agridata.agreement.dto.ConsentRequestStateEnum;
import ch.agridata.agreement.mapper.ConsentRequestMapper;
import ch.agridata.agreement.mapper.DataRequestMapper;
import ch.agridata.agreement.persistence.ConsentRequestEntity;
import ch.agridata.agreement.persistence.ConsentRequestRepository;
import ch.agridata.agreement.persistence.DataRequestRepository;
import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.user.api.UserApi;
import ch.agridata.user.dto.UidDto;
import jakarta.annotation.Nullable;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.NotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Provides read access to consent request information for different roles
 *
 * @CommentLastReviewed 2025-09-10
 */

@ApplicationScoped
@RequiredArgsConstructor
public class ConsentRequestQueryService implements ConsentRequestApi {

  private final ConsentRequestRepository consentRequestRepository;
  private final ConsentRequestMapper consentRequestMapper;
  private final AgridataSecurityIdentity identity;
  private final AgridataSecurityIdentity agridataSecurityIdentity;
  private final UserApi participantApi;
  private final AgisApi agisApi;
  private final DataRequestRepository dataRequestRepository;
  private final DataRequestMapper dataRequestMapper;

  @RolesAllowed({PRODUCER_ROLE, SUPPORT_ROLE})
  public List<ConsentRequestProducerViewDto> getConsentRequestsAsCurrentDataProducer(@Nullable String dataProducerUid) {
    var uids = getAuthorizedUids(identity.getKtIdpOfUserOrImpersonatedUser()).stream()
        .filter(uid -> dataProducerUid == null || uid.equals(dataProducerUid))
        .toList();
    var consentRequestEntities = consentRequestRepository.findByDataProducerUids(uids);
    return consentRequestEntities.stream()
        .map(this::toConsentRequestProducerViewDto)
        .toList();
  }

  @RolesAllowed(ADMIN_ROLE)
  public List<ConsentRequestConsumerViewDto> getConsentRequestsOfDataRequestForKtIdP(UUID dataRequestId, String ktIdP) {
    var existingConsentRequests = getConsentRequestsOfDataRequest(dataRequestId);

    return mergeWithAuthorizedUidsOfKtIdP(existingConsentRequests, ktIdP);
  }

  @RolesAllowed(CONSUMER_ROLE)
  public List<ConsentRequestConsumerViewDto> getConsentRequestsOfDataRequestOfCurrentConsumerForKtIdP(UUID dataRequestId, String ktIdP) {
    var dataRequest = dataRequestRepository.findByIdAndDataConsumerUid(
        dataRequestId,
        agridataSecurityIdentity.getUidOrElseThrow());

    if (dataRequest.isEmpty()) {
      return new ArrayList<>();
    }

    var existingConsentRequests = getConsentRequestsOfDataRequest(dataRequestId);

    return mergeWithAuthorizedUidsOfKtIdP(existingConsentRequests, ktIdP);
  }

  @RolesAllowed(CONSUMER_ROLE)
  @Override
  public List<UUID> getConsentRequestIdsOfCurrentConsumerGrantedByProducerForProductByBur(@Valid @NotNull String bur,
                                                                                          @Valid @NotNull
                                                                                          UUID productId) {
    var consumerUid = agridataSecurityIdentity.getUidOrElseThrow();
    var farm = agisApi.fetchFarmForBur(bur).orElseThrow(() -> new NotFoundException(bur));
    return consentRequestRepository.findConsentRequestIdsOfConsumerGrantedByProducerForProduct(consumerUid, farm.getUid(), productId);
  }

  @RolesAllowed(CONSUMER_ROLE)
  @Override
  public List<UUID> getConsentRequestIdsOfCurrentConsumerGrantedByProducerForProductByUid(@Valid @NotNull String uid,
                                                                                          @Valid @NotNull UUID productId) {
    var consumerUid = agridataSecurityIdentity.getUidOrElseThrow();
    return consentRequestRepository.findConsentRequestIdsOfConsumerGrantedByProducerForProduct(consumerUid, uid, productId);
  }

  @RolesAllowed(CONSUMER_ROLE)
  @Override
  public List<String> getGrantedConsentRequestUidsForProductOfCurrentConsumerSince(UUID productId, LocalDateTime since) {
    var dataConsumerUid = agridataSecurityIdentity.getUidOrElseThrow();

    return consentRequestRepository.findGrantedConsentRequestUidsForProductOfConsumerSince(productId, dataConsumerUid, since);
  }

  private ConsentRequestProducerViewDto toConsentRequestProducerViewDto(ConsentRequestEntity entity) {
    return consentRequestMapper.toConsentRequestProducerViewDto(entity,
        dataRequestMapper.toDto(entity.getDataRequest()));
  }

  private List<String> getAuthorizedUids(String ktIdP) {
    return participantApi.getAuthorizedUids(ktIdP).stream().map(UidDto::uid).toList();
  }

  private List<ConsentRequestConsumerViewDto> getConsentRequestsOfDataRequest(UUID dataRequestId) {
    return consentRequestRepository.findByDataRequestId(dataRequestId).stream()
        .map(consentRequestMapper::toConsentRequestConsumerViewDto)
        .toList();
  }

  private List<ConsentRequestConsumerViewDto> mergeWithAuthorizedUidsOfKtIdP(
      @NonNull List<ConsentRequestConsumerViewDto> existingConsentRequests,
      @NonNull String ktIdP) {

    var authorizedUids = getAuthorizedUids(ktIdP);
    var existingUids = existingConsentRequests.stream()
        .collect(Collectors.toMap(ConsentRequestConsumerViewDto::dataProducerUid, Function.identity()));

    return authorizedUids.stream()
        .map(uid -> existingUids.getOrDefault(uid,
            ConsentRequestConsumerViewDto.builder()
                .id(null)
                .dataProducerUid(uid)
                .stateCode(ConsentRequestStateEnum.NOT_CREATED)
                .build()))
        .toList();
  }
}
