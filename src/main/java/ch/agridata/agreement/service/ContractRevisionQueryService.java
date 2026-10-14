package ch.agridata.agreement.service;

import static ch.agridata.common.utils.AuthenticationUtil.ADMIN_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PRODUCER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PROVIDER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.SUPPORT_ROLE;

import ch.agridata.agreement.dto.ContractRevisionDto;
import ch.agridata.agreement.mapper.ContractRevisionMapper;
import ch.agridata.agreement.persistence.ContractRevisionEntity;
import ch.agridata.agreement.persistence.ContractRevisionRepository;
import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.common.security.AgridataSecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

/**
 * Provides query operations for contract revisions.
 *
 * @CommentLastReviewed 2026-03-16
 */
@ApplicationScoped
@RequiredArgsConstructor
public class ContractRevisionQueryService {
  private final ContractRevisionRepository contractRevisionRepository;
  private final ContractRevisionMapper contractRevisionMapper;
  private final AgridataSecurityIdentity agridataSecurityIdentity;
  private final DataRequestQueryService dataRequestQueryService;
  private final ConsentRequestAggregationService consentRequestAggregationService;

  @RolesAllowed(ADMIN_ROLE)
  public ContractRevisionEntity getAsAdmin(UUID id) {
    return contractRevisionRepository.findByIdOptional(id)
        .filter(contractRevision -> contractRevision.getDataRequest().getStateCode() != DataRequestEntity.DataRequestStateEnum.DRAFT)
        .orElseThrow(() -> new NotFoundException(id.toString()));
  }

  @RolesAllowed(ADMIN_ROLE)
  public ContractRevisionDto getDtoAsAdmin(UUID id) {
    return contractRevisionMapper.toDto(getAsAdmin(id));
  }

  @RolesAllowed(PROVIDER_ROLE)
  public ContractRevisionEntity getAsProvider(UUID id) {
    return contractRevisionRepository.findByIdOptional(id)
        .filter(this::isAssignedToCurrentProvider)
        .orElseThrow(() -> new NotFoundException(id.toString()));
  }

  @RolesAllowed(PROVIDER_ROLE)
  public ContractRevisionDto getDtoAsProvider(UUID id) {
    return contractRevisionMapper.toDto(getAsProvider(id));
  }

  @RolesAllowed(CONSUMER_ROLE)
  public ContractRevisionEntity getAsConsumer(UUID id) {
    return contractRevisionRepository
        .findByIdAndDataConsumerUid(id, agridataSecurityIdentity.getUidOrElseThrow())
        .orElseThrow(() -> new NotFoundException(id.toString()));
  }

  @RolesAllowed(CONSUMER_ROLE)
  public ContractRevisionDto getDtoAsConsumer(UUID id) {
    return contractRevisionMapper.toDto(getAsConsumer(id));
  }

  /**
   * Returns the contract revision if it is the current revision of a data request that is visible to the current data producer
   * as a consent request aggregation.
   */
  @RolesAllowed({PRODUCER_ROLE, SUPPORT_ROLE})
  public ContractRevisionEntity getAsProducer(UUID id) {
    return contractRevisionRepository.findByIdOptional(id)
        .filter(contractRevision -> id.equals(contractRevision.getDataRequest().getCurrentContractRevisionId()))
        .filter(contractRevision -> consentRequestAggregationService.isVisibleToCurrentDataProducer(
            contractRevision.getDataRequest().getId()))
        .orElseThrow(() -> new NotFoundException(id.toString()));
  }

  @RolesAllowed(PROVIDER_ROLE)
  public boolean isAssignedToCurrentProvider(ContractRevisionEntity contractRevision) {
    return dataRequestQueryService.isAssignedToCurrentProvider(contractRevision.getDataRequest());
  }

  @RolesAllowed(PROVIDER_ROLE)
  public boolean isAssignedToCurrentProvider(UUID contractRevisionId) {
    return contractRevisionRepository.findByIdOptional(contractRevisionId)
        .map(this::isAssignedToCurrentProvider)
        .orElse(false);
  }

}
