package ch.agridata.product.api;

import ch.agridata.product.dto.DataProductDto;
import ch.agridata.product.dto.DataProductProviderConfigurationDto;
import java.util.UUID;

/**
 * Declares operations for retrieving a product or its provider configuration by identifier. It ensures a stable contract for other
 * modules.
 *
 * @CommentLastReviewed 2025-08-25
 */
public interface DataProductApi {
  DataProductDto getProductById(UUID productId);

  DataProductProviderConfigurationDto getProviderConfigurationById(UUID productId);
}
