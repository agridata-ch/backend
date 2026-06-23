package ch.agridata.product.api;

import ch.agridata.product.dto.DataProductDto;
import ch.agridata.product.dto.DataProductProviderConfigurationDto;
import ch.agridata.product.dto.DataSourceSystemDto;
import java.util.List;
import java.util.UUID;

/**
 * Declares operations for retrieving a product, its provider configuration, or its provider id by identifier. It ensures a stable contract
 * for other modules.
 *
 * @CommentLastReviewed 2025-02-09
 */
public interface DataProductApi {
  DataProductDto getActiveProductById(UUID productId);

  DataProductProviderConfigurationDto getProviderConfigurationById(UUID productId);

  UUID getDataSourceSystemIdOfActiveProduct(UUID productId);

  DataSourceSystemDto getDataSourceSystem(UUID dataSourceSystemId);

  List<DataProductDto> getActiveProductsByIds(List<UUID> productIds);
}
