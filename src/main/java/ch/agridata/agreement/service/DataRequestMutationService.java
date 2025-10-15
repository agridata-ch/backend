package ch.agridata.agreement.service;

import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;

import ch.agridata.agreement.dto.DataRequestDto;
import ch.agridata.agreement.dto.DataRequestUpdateDto;
import ch.agridata.agreement.mapper.DataRequestMapper;
import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.agreement.persistence.DataRequestRepository;
import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.uidregister.api.UidRegisterServiceApi;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

/**
 * Implements mutations on data requests. It ensures consistent state changes and enforces validation.
 *
 * @CommentLastReviewed 2025-08-25
 */

@ApplicationScoped
@RequiredArgsConstructor
public class DataRequestMutationService {

  private final DataRequestRepository dataRequestRepository;
  private final DataRequestMapper dataRequestMapper;
  private final AgridataSecurityIdentity agridataSecurityIdentity;
  private final UidRegisterServiceApi uidRegisterServiceApi;
  private final HumanFriendlyIdService humanFriendlyIdService;

  @Transactional
  @RolesAllowed(CONSUMER_ROLE)
  public DataRequestDto createDataRequestDraft(DataRequestUpdateDto dataRequestDto) {
    var uidRegisterCompany = uidRegisterServiceApi.getByUidOfCurrentUser();
    var dataRequestEntity =
        DataRequestEntity.builder()
            .humanFriendlyId(humanFriendlyIdService.getHumanFriendlyIdForDataRequest())
            .dataConsumerUid(agridataSecurityIdentity.getUidOrElseThrow())
            .dataConsumerLegalName(uidRegisterCompany.legalName())
            .stateCode(DataRequestEntity.DataRequestStateEnum.DRAFT)
            .build();
    return updateEntityWithDto(dataRequestDto, dataRequestEntity);
  }

  @Transactional
  @RolesAllowed(CONSUMER_ROLE)
  public DataRequestDto updateDataRequestDetails(UUID requestId, DataRequestUpdateDto dataRequestDto) {
    var entity = dataRequestRepository.findByIdAndDataConsumerUid(requestId, agridataSecurityIdentity.getUidOrElseThrow())
        .orElseThrow(() -> new NotFoundException(requestId.toString()));
    if (!DataRequestEntity.DataRequestStateEnum.DRAFT.equals(entity.getStateCode())) {
      throw new IllegalStateException(
          "Data request with id " + entity.getId() + " is not in status draft");
    }
    return updateEntityWithDto(dataRequestDto, entity);
  }

  private DataRequestDto updateEntityWithDto(DataRequestUpdateDto dataRequestDto,
                                             DataRequestEntity entity) {
    dataRequestMapper.updateEntity(dataRequestDto, entity);
    dataRequestRepository.persist(entity);
    return dataRequestMapper.toDto(entity);
  }

}
