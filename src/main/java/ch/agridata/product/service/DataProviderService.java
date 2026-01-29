package ch.agridata.product.service;

import ch.agridata.product.dto.DataProductDto;
import ch.agridata.product.dto.DataProviderDto;
import ch.agridata.product.mapper.DataProductEntityMapper;
import ch.agridata.product.mapper.DataProviderEntityMapper;
import ch.agridata.product.persistence.DataProductRepository;
import ch.agridata.product.persistence.DataProviderRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

/**
 * Implements dataProvider-related operations. It exposes queries for all dataProviders and resolves dataProviders by ID, enforcing error
 * handling for missing products.
 *
 * @CommentLastReviewed 2026-02-06
 */

@ApplicationScoped
@RequiredArgsConstructor
public class DataProviderService {
  private final DataProviderRepository dataProviderRepository;
  private final DataProductRepository dataProductRepository;

  private final DataProviderEntityMapper dataProviderEntityMapper;
  private final DataProductEntityMapper dataProductEntityMapper;

  public List<DataProviderDto> getDataProviders() {
    return dataProviderRepository.findAll().stream().map(dataProviderEntityMapper::toDto).toList();
  }

  public DataProviderDto getDataProviderById(UUID providerId) {
    return dataProviderRepository.findByIdOptional(providerId)
        .map(dataProviderEntityMapper::toDto)
        .orElseThrow(
            () -> new NotFoundException(providerId.toString()));
  }

  public List<DataProductDto> getDataProductsByProviderId(UUID providerId) {
    if (dataProviderRepository.findByIdOptional(providerId).isEmpty()) {
      throw new NotFoundException(providerId.toString());
    }

    return dataProductRepository.listByProviderId(providerId).stream().map(dataProductEntityMapper::toDto).toList();
  }
}
