package ch.agridata.product.service;

import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.product.persistence.DataProductRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

/**
 * Existence and ownership checks for data products, shared by all document operations.
 *
 * <p>Ownership mismatches deliberately surface as {@link NotFoundException} rather than 403,
 * so a provider cannot probe for the existence of other providers' data products.
 *
 * @CommentLastReviewed 2026-07-09
 */

@ApplicationScoped
@RequiredArgsConstructor
public class DataProductAccessGuard {

  private final DataProductRepository dataProductRepository;
  private final AgridataSecurityIdentity agridataSecurityIdentity;

  /**
   * Ensures the data product exists and is owned by the currently authenticated provider.
   *
   * @throws NotFoundException if the data product does not exist or belongs to another provider
   */
  public void verifyOwnedByCurrentProvider(UUID dataProductId) {
    dataProductRepository
        .findByIdAndDataProviderUidOptional(dataProductId, agridataSecurityIdentity.getUidOrElseThrow())
        .orElseThrow(() -> new NotFoundException(dataProductId.toString()));
  }

  /**
   * Ensures the data product exists, without an ownership constraint (admin paths).
   *
   * @throws NotFoundException if the data product does not exist
   */
  public void verifyExists(UUID dataProductId) {
    dataProductRepository.findByIdOptional(dataProductId)
        .orElseThrow(() -> new NotFoundException(dataProductId.toString()));
  }
}
