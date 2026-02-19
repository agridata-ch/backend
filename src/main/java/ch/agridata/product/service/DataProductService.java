package ch.agridata.product.service;

import ch.agridata.product.api.DataProductApi;
import ch.agridata.product.dto.DataProductDto;
import ch.agridata.product.dto.DataProductProviderConfigurationDto;
import ch.agridata.product.dto.DataSourceSystemDto;
import ch.agridata.product.mapper.DataProductEntityMapper;
import ch.agridata.product.mapper.DataSourceSystemEntityMapper;
import ch.agridata.product.persistence.DataProductRepository;
import ch.agridata.product.persistence.DataSourceSystemRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

/**
 * Implements product-related operations. It exposes queries for all products, resolves products by ID, and retrieves provider
 * configurations, enforcing error handling for missing products.
 *
 * @CommentLastReviewed 2025-08-25
 */

@ApplicationScoped
@RequiredArgsConstructor
public class DataProductService implements DataProductApi {
  private final DataProductRepository dataProductRepository;
  private final DataProductEntityMapper dataProductEntityMapper;
  private final DataSourceSystemRepository dataSourceSystemRepository;
  private final DataSourceSystemEntityMapper dataSourceSystemMapper;

  public List<DataProductDto> getDataProducts() {
    return dataProductRepository.findAll().stream().map(dataProductEntityMapper::toDto).toList();
  }

  @Override
  public DataProductDto getProductById(UUID productId) {
    return dataProductRepository.findByIdOptional(productId)
        .map(dataProductEntityMapper::toDto)
        .orElseThrow(
            () -> new NotFoundException(productId.toString()));
  }

  @Override
  public DataProductProviderConfigurationDto getProviderConfigurationById(UUID productId) {
    return dataProductRepository.findByIdOptional(productId)
        .map(dataProductEntityMapper::toProviderConfigurationDto)
        .orElseThrow(
            () -> new NotFoundException(productId.toString()));
  }

  @Override
  public UUID getDataSourceSystemId(UUID productId) {
    return dataProductRepository.findDataSourceSystemIdByProductId(productId);
  }

  @Override
  public DataSourceSystemDto getDataSourceSystem(UUID dataSourceSystemId) {
    return dataSourceSystemRepository.findByIdOptional(dataSourceSystemId)
        .map(dataSourceSystemMapper::toDto)
        .orElseThrow(() ->
            new NotFoundException("DataSourceSystem not found: " + dataSourceSystemId)
        );
  }
}
