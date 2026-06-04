package ch.agridata.product.service;

import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.product.dto.DataProductDto;
import ch.agridata.product.dto.DataProviderDto;
import ch.agridata.product.dto.RestClientDto;
import ch.agridata.product.mapper.DataProductMapper;
import ch.agridata.product.mapper.DataProviderMapper;
import ch.agridata.product.mapper.RestClientMapper;
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
  private final DataProviderMapper dataProviderMapper;
  private final DataProductMapper dataProductMapper;
  private final RestClientMapper restClientMapper;
  private final AgridataSecurityIdentity agridataSecurityIdentity;

  public List<DataProviderDto> getDataProviders() {
    return dataProviderRepository.findAll().stream().map(dataProviderMapper::toDto).toList();
  }

  public DataProviderDto getDataProviderById(UUID providerId) {
    return dataProviderRepository.findByIdOptional(providerId)
        .map(dataProviderMapper::toDto)
        .orElseThrow(
            () -> new NotFoundException(providerId.toString()));
  }

  public List<DataProductDto> getActiveDataProductsByProviderId(UUID providerId) {
    var provider = dataProviderRepository.findByIdOptional(providerId)
        .orElseThrow(() -> new NotFoundException(providerId.toString()));

    return dataProductRepository.listActiveByProviderUid(provider.getUid()).stream().map(dataProductMapper::toDto).toList();
  }

  public List<RestClientDto> getRestClientsByDataProviderIdAsAdmin(UUID providerId) {
    return dataProviderRepository.findByIdOptional(providerId).orElseThrow(() -> new NotFoundException(providerId.toString()))
        .getRestClients().stream().map(restClientMapper::toDto).toList();
  }

  public List<RestClientDto> getRestClientsByDataProviderIdAsProvider(UUID providerId) {
    return dataProviderRepository.findByIdAndProviderUidOptional(providerId, agridataSecurityIdentity.getUidOrElseThrow())
        .orElseThrow(() -> new NotFoundException(providerId.toString())).getRestClients().stream().map(restClientMapper::toDto).toList();
  }
}
