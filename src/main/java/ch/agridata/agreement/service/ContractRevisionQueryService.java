package ch.agridata.agreement.service;

import ch.agridata.agreement.dto.ContractRevisionDto;
import ch.agridata.agreement.mapper.ContractRevisionMapper;
import ch.agridata.agreement.persistence.ContractRevisionRepository;
import ch.agridata.common.security.AgridataSecurityIdentity;
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

  public ContractRevisionDto getContractRevisionOfCurrentConsumer(UUID contractRevisionId) {
    return contractRevisionRepository
        .findByIdAndDataConsumerUid(contractRevisionId, agridataSecurityIdentity.getUidOrElseThrow())
        .map(contractRevisionMapper::toDto)
        .orElseThrow(() -> new NotFoundException(contractRevisionId.toString()));
  }
}
