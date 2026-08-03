package ch.agridata.product.mapper;

import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.common.persistence.TranslationPersistenceDto;
import ch.agridata.product.dto.DataProductDescriptionDto;
import ch.agridata.product.dto.DataProductDto;
import ch.agridata.product.dto.DataProductExtendedDescriptionDto;
import ch.agridata.product.dto.DataProductNameDto;
import ch.agridata.product.dto.DataProductProviderConfigurationDto;
import ch.agridata.product.dto.DataProductStateEnum;
import ch.agridata.product.dto.DataProductUpdateDto;
import ch.agridata.product.dto.PublicDataProductDto;
import ch.agridata.product.persistence.DataProductEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Maps between DataProductEntity and its DTO representations. It ensures product metadata is accurately transformed for both persistence
 * and API responses.
 *
 * @CommentLastReviewed 2026-07-22
 */

@Mapper(componentModel = "jakarta", uses = RestClientMapper.class)
public interface DataProductMapper {

  @Mapping(target = "dataSourceSystemCode", source = "dataSourceSystem.code")
  DataProductDto toDto(DataProductEntity dataProductEntity);

  DataProductProviderConfigurationDto toProviderConfigurationDto(DataProductEntity dataProductEntity);

  PageResponseDto<DataProductDto> toPagedDataProductDto(PageResponseDto<DataProductEntity> pagedEntities);

  PublicDataProductDto toPublicDto(DataProductEntity dataProductEntity);

  PageResponseDto<PublicDataProductDto> toPagedPublicDataProductDto(PageResponseDto<DataProductEntity> pagedEntities);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "name", source = "dataProductUpdateDto.name")
  @Mapping(target = "deprecatedSince", ignore = true)
  @Mapping(target = "archived", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "modifiedBy", ignore = true)
  @Mapping(target = "modifiedAt", ignore = true)
  @Mapping(target = "stateCode", ignore = true)
  @Mapping(target = "dataSourceSystem", ignore = true)
  @Mapping(target = "restClient", ignore = true)
  @Mapping(target = "restClientIdentifierCode",
      expression = "java(dataProductEntity.getRestClient() != null ? dataProductEntity.getRestClient().getCode() : null)")
  @Mapping(target = "dataProviderUid", ignore = true)
  void updateEntity(DataProductUpdateDto dataProductUpdateDto, @MappingTarget DataProductEntity dataProductEntity);

  @Mapping(target = "dataSourceSystemId", source = "dataSourceSystem.id")
  @Mapping(target = "restClientId", source = "restClient.id")
  DataProductUpdateDto toUpdateDto(DataProductEntity entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "deprecatedSince", ignore = true)
  @Mapping(target = "archived", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "modifiedBy", ignore = true)
  @Mapping(target = "modifiedAt", ignore = true)
  @Mapping(target = "stateCode", ignore = true)
  @Mapping(target = "dataSourceSystem", ignore = true)
  @Mapping(target = "restClient", ignore = true)
  @Mapping(target = "restClientIdentifierCode",
      expression = "java(dataProductEntity.getRestClient() != null ? dataProductEntity.getRestClient().getCode() : null)")
  @Mapping(target = "dataProviderUid", ignore = true)
  void patchEntity(DataProductUpdateDto dataProductUpdateDto, @MappingTarget DataProductEntity dataProductEntity);

  TranslationPersistenceDto toTranslationPersistenceDto(DataProductNameDto translationDto);

  TranslationPersistenceDto toTranslationPersistenceDto(DataProductDescriptionDto translationDto);

  TranslationPersistenceDto toTranslationPersistenceDto(DataProductExtendedDescriptionDto translationDto);

  DataProductStateEnum toDtoDataProductStateEnum(ch.agridata.product.persistence.DataProductStateEnum persistenceStateEnum);

  ch.agridata.product.persistence.DataProductStateEnum toPersistenceDataProductStateEnum(DataProductStateEnum dtoStateEnum);
}
