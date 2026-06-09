package ch.agridata.agreement.service;

import ch.agridata.agreement.persistence.ContractRevisionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

/**
 * Provides business logic for contract revision mutations.
 *
 * @CommentLastReviewed 2026-06-09
 */
@ApplicationScoped
@RequiredArgsConstructor
public class ContractRevisionMutationService {

  private final ContractRevisionRepository contractRevisionRepository;

  @Transactional
  public void archiveAllForDataRequest(UUID dataRequestId) {
    contractRevisionRepository.deleteByDataRequestId(dataRequestId);
  }
}
