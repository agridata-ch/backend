package ch.agridata.agreement.service;

import static ch.agridata.common.utils.AuthenticationUtil.ADMIN_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PRODUCER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.SUPPORT_ROLE;

import ch.agridata.agis.api.AgisApi;
import ch.agridata.agreement.api.ConsentRequestApi;
import ch.agridata.agreement.dto.ConsentRequestConsumerViewDto;
import ch.agridata.agreement.dto.ConsentRequestConsumerViewV2Dto;
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
  private final UserApi userApi;
  private final AgisApi agisApi;
  private final DataRequestRepository dataRequestRepository;
  private final DataRequestMapper dataRequestMapper;

  @RolesAllowed({PRODUCER_ROLE, SUPPORT_ROLE})
  public List<ConsentRequestProducerViewDto> getConsentRequestsAsCurrentDataProducer(@Nullable String dataProducerUid) {
    var uids =
        userApi.getAuthorizedUids(identity.getKtIdpOrImpersonatedKtIdP(), identity.getAgateLoginIdOrImpersonatedAgateLoginId()).stream()
            .map(UidDto::uid)
            .filter(uid -> dataProducerUid == null || uid.equals(dataProducerUid))
            .toList();
    var consentRequestEntities = consentRequestRepository.findByDataProducerUids(uids);
    return consentRequestEntities.stream()
        .map(this::toConsentRequestProducerViewDto)
        .toList();
  }

  @RolesAllowed({PRODUCER_ROLE, SUPPORT_ROLE})
  public ConsentRequestProducerViewDto getConsentRequest(@NotNull UUID id) {
    var uids =
        userApi.getAuthorizedUids(identity.getKtIdpOrImpersonatedKtIdP(), identity.getAgateLoginIdOrImpersonatedAgateLoginId()).stream()
            .map(UidDto::uid)
            .toList();
    return consentRequestRepository.findByDataProducerUids(uids).stream()
        .filter(consentRequests -> consentRequests.getId().equals(id))
        .map(this::toConsentRequestProducerViewDto)
        .findFirst()
        .orElseThrow(() -> new NotFoundException("Consent request with id " + id + " not found for current data producer"));
  }

  @RolesAllowed(ADMIN_ROLE)
  public List<ConsentRequestConsumerViewV2Dto> getConsentRequestsOfDataRequestAndProducer(UUID dataRequestId,
                                                                                          String ktIdP,
                                                                                          String producerAgateLoginId) {
    var authorizedUids = userApi.getAuthorizedUids(ktIdP, producerAgateLoginId);
    var existingConsentRequests = consentRequestRepository.findByDataRequestIdAndDataProducerUids(
        dataRequestId,
        authorizedUids.stream().map(UidDto::uid).toList());

    return merge(existingConsentRequests, authorizedUids);
  }

  /**
   * This method is deprecated, because it does not return the UIDs of equid owners. They don't have a KtIdP and need to be identified
   * by their agateLoginId.
   *
   * @deprecated Replaced by {@link #getConsentRequestsOfDataRequestOfCurrentConsumerAndProducer(UUID, String, String)}
   */
  @Deprecated(since = "1.5.0")
  @RolesAllowed(CONSUMER_ROLE)
  public List<ConsentRequestConsumerViewDto> getConsentRequestsOfDataRequestOfCurrentConsumerForKtIdP(UUID dataRequestId, String ktIdP) {
    return getConsentRequestsOfDataRequestOfCurrentConsumerAndProducer(dataRequestId, ktIdP, null).stream()
        .map(consentRequestMapper::toConsentRequestConsumerViewDto)
        .toList();
  }

  @RolesAllowed(CONSUMER_ROLE)
  public List<ConsentRequestConsumerViewV2Dto> getConsentRequestsOfDataRequestOfCurrentConsumerAndProducer(UUID dataRequestId,
                                                                                                           String ktIdP,
                                                                                                           String producerAgateLoginId) {
    var dataRequest = dataRequestRepository.findByIdAndDataConsumerUid(
        dataRequestId,
        identity.getUidOrElseThrow());

    if (dataRequest.isEmpty()) {
      return new ArrayList<>();
    }

    var authorizedUids = userApi.getAuthorizedUids(ktIdP, producerAgateLoginId);
    var existingConsentRequests = consentRequestRepository.findByDataRequestIdAndDataProducerUids(
        dataRequestId,
        authorizedUids.stream().map(UidDto::uid).toList());

    return merge(existingConsentRequests, authorizedUids);
  }

  @RolesAllowed(CONSUMER_ROLE)
  @Override
  public List<UUID> getConsentRequestIdsOfCurrentConsumerGrantedByProducerForProductByBur(@Valid @NotNull String bur,
                                                                                          @Valid @NotNull
                                                                                          UUID productId) {
    var consumerUid = identity.getUidOrElseThrow();
    var farm = agisApi.fetchFarmForBur(bur).orElseThrow(() -> new NotFoundException(bur));
    return consentRequestRepository.findConsentRequestIdsOfConsumerGrantedByProducerForProduct(consumerUid, farm.getUid(), productId);
  }

  @RolesAllowed(CONSUMER_ROLE)
  @Override
  public List<UUID> getConsentRequestIdsOfCurrentConsumerGrantedByProducerForProductByUid(@Valid @NotNull String uid,
                                                                                          @Valid @NotNull UUID productId) {
    var consumerUid = identity.getUidOrElseThrow();
    return consentRequestRepository.findConsentRequestIdsOfConsumerGrantedByProducerForProduct(consumerUid, uid, productId);
  }

  @RolesAllowed(CONSUMER_ROLE)
  @Override
  public List<String> getGrantedConsentRequestUidsForProductOfCurrentConsumerSince(UUID productId, LocalDateTime since) {
    var dataConsumerUid = identity.getUidOrElseThrow();

    return consentRequestRepository.findGrantedConsentRequestUidsForProductOfConsumerSince(productId, dataConsumerUid, since);
  }

  private ConsentRequestProducerViewDto toConsentRequestProducerViewDto(ConsentRequestEntity entity) {
    return consentRequestMapper.toConsentRequestProducerViewDto(entity,
        dataRequestMapper.toDto(entity.getDataRequest()));
  }

  private List<ConsentRequestConsumerViewV2Dto> merge(
      @NonNull List<ConsentRequestEntity> existingConsentRequests,
      @NonNull List<UidDto> authorizedUids) {

    return authorizedUids.stream()
        .map(uidDto -> existingConsentRequests.stream()
            .filter(existingConsentRequest -> uidDto.uid().equals(existingConsentRequest.getDataProducerUid()))
            .findFirst()
            .map(existingConsentRequest -> consentRequestMapper.toConsentRequestConsumerViewV2Dto(existingConsentRequest, uidDto.name()))
            .orElse(ConsentRequestConsumerViewV2Dto.builder()
                .id(null)
                .dataProducerUid(uidDto.uid())
                .name(uidDto.name())
                .stateCode(ConsentRequestStateEnum.NOT_CREATED)
                .build()))
        .toList();
  }

}
