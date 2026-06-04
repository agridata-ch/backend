package ch.agridata.product.service;

import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.product.dto.DataSourceSystemDto;
import ch.agridata.product.mapper.DataSourceSystemMapper;
import ch.agridata.product.persistence.DataProviderRepository;
import ch.agridata.product.persistence.DataSourceSystemRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

/**
 * Provides functionality for managing data source systems. This service enables fetching
 * data source systems associated with a specific data provider, utilizing repository and
 * mapping layers to retrieve and transform entities into DTO representations.
 *
 * @CommentLastReviewed 2026-06-11
 */

@ApplicationScoped
@RequiredArgsConstructor
public class DataSourceSystemService {
  private final DataSourceSystemRepository dataSourceSystemRepository;
  private final DataSourceSystemMapper dataSourceSystemMapper;
  private final DataProviderRepository dataProviderRepository;
  private final AgridataSecurityIdentity agridataSecurityIdentity;

  public List<DataSourceSystemDto> getByProviderIdAsAdmin(UUID dataProviderId) {
    dataProviderRepository.findByIdOptional(dataProviderId).orElseThrow(() -> new NotFoundException(dataProviderId.toString()));
    return dataSourceSystemRepository.find("dataProvider.id", dataProviderId)
        .stream().map(dataSourceSystemMapper::toDto).toList();
  }

  public List<DataSourceSystemDto> getByProviderIdAsProvider(UUID providerId) {
    dataProviderRepository.findByIdAndProviderUidOptional(providerId, agridataSecurityIdentity.getUidOrElseThrow())
        .orElseThrow(() -> new NotFoundException(providerId.toString()));
    return dataSourceSystemRepository.find("dataProvider.id", providerId)
        .stream().map(dataSourceSystemMapper::toDto).toList();
  }
}
