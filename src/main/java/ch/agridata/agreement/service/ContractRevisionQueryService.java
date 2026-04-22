package ch.agridata.agreement.service;

import static ch.agridata.common.utils.AuthenticationUtil.PROVIDER_ROLE;

import ch.agridata.agreement.dto.ContractRevisionDto;
import ch.agridata.agreement.mapper.ContractRevisionMapper;
import ch.agridata.agreement.persistence.ContractRevisionEntity;
import ch.agridata.agreement.persistence.ContractRevisionRepository;
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

  public ContractRevisionDto getContractRevisionOfCurrentConsumer(UUID contractRevisionId) {
    return contractRevisionRepository
        .findByIdAndDataConsumerUid(contractRevisionId, agridataSecurityIdentity.getUidOrElseThrow())
        .map(contractRevisionMapper::toDto)
        .orElseThrow(() -> new NotFoundException(contractRevisionId.toString()));
  }

  @RolesAllowed(PROVIDER_ROLE)
  public ContractRevisionDto getContractRevisionOfCurrentProvider(UUID contractRevisionId) {
    return contractRevisionRepository.findByIdOptional(contractRevisionId)
        .filter(this::isAssignedToCurrentProvider)
        .map(contractRevisionMapper::toDto)
        .orElseThrow(() -> new NotFoundException(contractRevisionId.toString()));
  }

  @RolesAllowed(PROVIDER_ROLE)
  public boolean isAssignedToCurrentProvider(ContractRevisionEntity contractRevision) {
    return dataRequestQueryService.isAssignedToCurrentProvider(contractRevision.getDataRequest().getId());
  }

  @RolesAllowed(PROVIDER_ROLE)
  public boolean isAssignedToCurrentProvider(UUID contractRevisionId) {
    return contractRevisionRepository.findByIdOptional(contractRevisionId)
        .map(this::isAssignedToCurrentProvider)
        .orElse(false);
  }
}
