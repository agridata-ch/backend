package ch.agridata.agreement.service;

import static ch.agridata.common.utils.AuthenticationUtil.ADMIN_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PROVIDER_ROLE;

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
 * @CommentLastReviewed: 2026-03-16
 */

@ApplicationScoped
@RequiredArgsConstructor
public class ContractRevisionQueryService {
  private final ContractRevisionRepository contractRevisionRepository;
  private final ContractRevisionMapper contractRevisionMapper;
  private final AgridataSecurityIdentity agridataSecurityIdentity;
  private final DataRequestQueryService dataRequestQueryService;

  @RolesAllowed({ADMIN_ROLE, PROVIDER_ROLE, CONSUMER_ROLE})
  public ContractRevisionDto getDtoWithAccessCheck(UUID id) {
    var contract = getWithAccessCheck(id);
    return contractRevisionMapper.toDto(contract);
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

  @RolesAllowed({ADMIN_ROLE, PROVIDER_ROLE, CONSUMER_ROLE})
  public ContractRevisionEntity getWithAccessCheck(UUID id) {
    if (agridataSecurityIdentity.isAdmin()) {
      return contractRevisionRepository.findByIdOptional(id)
          .filter(contractRevision -> contractRevision.getDataRequest().getStateCode() != DataRequestEntity.DataRequestStateEnum.DRAFT)
          .orElseThrow(() -> new NotFoundException(id.toString()));
    }

    if (agridataSecurityIdentity.isProvider()) {
      return contractRevisionRepository.findByIdOptional(id)
          .filter(this::isAssignedToCurrentProvider)
          .orElseThrow(() -> new NotFoundException(id.toString()));
    }

    return contractRevisionRepository
        .findByIdAndDataConsumerUid(id, agridataSecurityIdentity.getUidOrElseThrow())
        .orElseThrow(() -> new NotFoundException(id.toString()));
  }

}
