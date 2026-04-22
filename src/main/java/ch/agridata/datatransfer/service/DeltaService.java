package ch.agridata.datatransfer.service;

import ch.agridata.agreement.api.ConsentRequestApi;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

/**
 * Service for retrieving product-related identifiers (UIDs) that have granted consent requests since a specific point in time.
 *
 * @CommentLastReviewed 2025-09-10
 */
@ApplicationScoped
@RequiredArgsConstructor
public class DeltaService {

  private final ConsentRequestApi consentRequestApi;

  public List<String> getDeltaIds(UUID productId, LocalDateTime since) {
    return consentRequestApi.getGrantedConsentRequestUidsForProductOfCurrentConsumerSince(productId, since);
  }

}
