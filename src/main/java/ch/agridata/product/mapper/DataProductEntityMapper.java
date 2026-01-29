package ch.agridata.product.mapper;

import ch.agridata.product.dto.DataProductDto;
import ch.agridata.product.dto.DataProductProviderConfigurationDto;
import ch.agridata.product.persistence.DataProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Maps between DataProductEntity and its DTO representations. It ensures product metadata is accurately transformed for both persistence
 * and API responses.
 *
 * @CommentLastReviewed 2025-08-25
 */

@Mapper(componentModel = "jakarta")
public interface DataProductEntityMapper {

  @Mapping(target = "dataSourceSystemCode", source = "dataSourceSystem.code")
  DataProductDto toDto(DataProductEntity dataProductEntity);

  DataProductProviderConfigurationDto toProviderConfigurationDto(DataProductEntity dataProductEntity);
}
