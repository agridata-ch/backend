package ch.agridata.agreement.service;

import ch.agridata.agreement.dto.DataRequestDto;
import ch.agridata.agreement.mapper.DataRequestMapper;
import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.product.api.DataProductApi;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

/**
 * Enriches {@link DataRequestDto} instances with additional data source system and provider information.
 * It resolves missing details via the {@link DataProductApi} based on the referenced data source system ID
 * and augments the DTO accordingly.
 *
 * @CommentLastReviewed 2026-02-17
 */

@ApplicationScoped
@RequiredArgsConstructor
public class DataRequestEnrichmentService {
  private final DataProductApi dataProductApi;
  private final DataRequestMapper dataRequestMapper;

  public DataRequestDto toEnrichedDto(DataRequestEntity entity) {
    if (entity == null) {
      return null;
    }

    UUID dataSourceSystemId = entity.getDataSourceSystemId(); // adapt getter name to your entity
    if (dataSourceSystemId == null) {
      return dataRequestMapper.toDto(entity, null);
    }

    var dataSourceSystem = dataProductApi.getDataSourceSystem(dataSourceSystemId);

    return dataRequestMapper.toDto(entity, dataSourceSystem);
  }
}
