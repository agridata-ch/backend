package ch.agridata.agreement.service;

import ch.agridata.agreement.dto.DataRequestDto;
import ch.agridata.agreement.mapper.DataRequestMapper;
import ch.agridata.agreement.persistence.DataRequestDataProductEntity;
import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.product.api.DataProductApi;
import ch.agridata.product.dto.DataProductDto;
import ch.agridata.product.dto.FlowCodeEnum;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

/**
 * Enriches {@link DataRequestDto} instances with information that is not stored on the data request itself.
 * It resolves the referenced data source system and determines whether any of the requested products is fetched
 * through a BUR based flow, both via the {@link DataProductApi}, and augments the DTO accordingly.
 *
 * @CommentLastReviewed 2026-09-10
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

    UUID dataSourceSystemId = entity.getDataSourceSystemId();
    var dataSourceSystem = dataSourceSystemId == null
        ? null
        : dataProductApi.getDataSourceSystem(dataSourceSystemId);

    return dataRequestMapper.toDto(entity, dataSourceSystem, hasBurBasedProducts(entity));
  }

  /**
   * Returns whether any of the data request's active products is fetched through a BUR based flow.
   * If none is, the data request's consent requests are purely UID based.
   */
  private boolean hasBurBasedProducts(DataRequestEntity entity) {
    var dataProductIds = entity.getDataProducts().stream()
        .map(DataRequestDataProductEntity::getDataProductId)
        .toList();

    return dataProductApi.getActiveProductsByIds(dataProductIds).stream()
        .map(DataProductDto::flowCode)
        .filter(Objects::nonNull)
        .anyMatch(FlowCodeEnum::isBurBased);
  }
}
