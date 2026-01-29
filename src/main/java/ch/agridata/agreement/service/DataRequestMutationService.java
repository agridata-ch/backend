package ch.agridata.agreement.service;

import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;

import ch.agridata.agreement.dto.DataRequestDto;
import ch.agridata.agreement.dto.DataRequestUpdateDto;
import ch.agridata.agreement.mapper.DataRequestMapper;
import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.agreement.persistence.DataRequestRepository;
import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.product.api.DataProductApi;
import ch.agridata.product.dto.DataProductDto;
import ch.agridata.uidregister.api.UidRegisterServiceApi;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.NotFoundException;
import java.util.HashSet;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

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
  private final DataProductApi dataProductApi;

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
    setDataSourceSystemId(dataRequestDto, entity);
    dataRequestRepository.persist(entity);
    return dataRequestMapper.toDto(entity);
  }

  /**
   * Verifies that all data products in the request exist and share the same Data Source System Code.
   *
   * @throws ValidationException if any product is not found
   */
  private void setDataSourceSystemId(@NotNull DataRequestUpdateDto dto, DataRequestEntity entity) {
    var products = dto.products();
    if (products == null || products.isEmpty()) {
      entity.setDataProviderId(null);
      return;
    }

    var codes = new HashSet<String>();

    for (UUID id : products) {
      codes.add(getProductOrThrowValidation(id).dataSourceSystemCode());
      if (codes.size() > 1) {
        throw new ValidationException("Cannot process request: all products must share the same data source system");
      }
    }

    var dataProviderId = dataProductApi.getProviderId(getProductOrThrowValidation(products.getFirst()).id());


    entity.setDataProviderId(dataProviderId);
  }

  private DataProductDto getProductOrThrowValidation(UUID id) {
    try {
      return dataProductApi.getProductById(id);
    } catch (NotFoundException e) {
      throw new ValidationException("Cannot process request: data product " + id + " not found", e);
    }
  }
}
