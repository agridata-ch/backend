package ch.agridata.product.mapper;

import ch.agridata.common.dto.LinkDto;
import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.common.persistence.LinkPersistenceDto;
import ch.agridata.common.persistence.TranslationPersistenceDto;
import ch.agridata.product.dto.DataProductDescriptionDto;
import ch.agridata.product.dto.DataProductDto;
import ch.agridata.product.dto.DataProductExtendedDescriptionDto;
import ch.agridata.product.dto.DataProductNameDto;
import ch.agridata.product.dto.DataProductProviderConfigurationDto;
import ch.agridata.product.dto.DataProductStateEnum;
import ch.agridata.product.dto.DataProductUpdateDto;
import ch.agridata.product.dto.FlowCodeEnum;
import ch.agridata.product.dto.PublicDataProductDto;
import ch.agridata.product.dto.RestClientMethodCodeEnum;
import ch.agridata.product.persistence.DataProductEntity;
import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.Condition;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.openapitools.jackson.nullable.JsonNullable;

/**
 * Maps between DataProductEntity and its DTO representations. It ensures product metadata is accurately transformed for both persistence
 * and API responses.
 *
 * <p>The mutation mappings work on the {@link JsonNullable}-wrapped fields of {@link DataProductUpdateDto}. MapStruct 1.6 has no native
 * {@code JsonNullable} support, so the {@link #isPresent} {@code @Condition} presence check and the
 * {@link #unwrap} / {@link #wrap} helpers below supply the behaviour that the generated
 * {@link #updateEntity} / {@link #patchEntity} / {@link #toUpdateDto} bodies delegate to. A field is only read from the DTO
 * when its {@code JsonNullable} is present; the two update methods differ solely in how an <em>absent</em> field is treated:
 * {@link #updateEntity} uses the default {@code SET_TO_NULL} (full replace for POST/PUT), while {@link #patchEntity} uses
 * {@code IGNORE} so an omitted field leaves the current value untouched &ndash; which is what lets PATCH differentiate omitted from a
 * present {@code null} (the latter clears the field in both). {@link #toUpdateDto} conversely wraps every entity value as
 * <em>present</em> (including {@code null}) so the {@code Submit} completeness constraints still fire on a missing value.
 *
 * @CommentLastReviewed 2026-09-08
 */

@Mapper(componentModel = "jakarta", uses = {RestClientMapper.class})
public interface DataProductMapper {

  @Mapping(target = "dataSourceSystemCode", source = "dataSourceSystem.code")
  DataProductDto toDto(DataProductEntity dataProductEntity);

  DataProductProviderConfigurationDto toProviderConfigurationDto(DataProductEntity dataProductEntity);

  PageResponseDto<DataProductDto> toPagedDataProductDto(PageResponseDto<DataProductEntity> pagedEntities);

  PublicDataProductDto toPublicDto(DataProductEntity dataProductEntity);

  PageResponseDto<PublicDataProductDto> toPagedPublicDataProductDto(PageResponseDto<DataProductEntity> pagedEntities);

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
      expression = "java(entity.getRestClient() != null ? entity.getRestClient().getCode() : null)")
  @Mapping(target = "dataProviderUid", ignore = true)
  void updateEntity(DataProductUpdateDto dto, @MappingTarget DataProductEntity entity);

  /**
   * Partial-update mapping used by PATCH. Identical to {@link #updateEntity} except for
   * {@link NullValuePropertyMappingStrategy#IGNORE}: an omitted field leaves the current value untouched (no {@code else set(null)}
   * branch is generated), while a field present with an explicit {@code null} clears it. {@code consentRequired} and
   * {@code paymentRequired} are primitives on the entity, so a present-{@code null} would unbox to a NPE; that never happens because
   * both carry an ungrouped {@code @NotNull} that rejects a present {@code null} before this mapper runs, on every verb.
   */
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @InheritConfiguration(name = "updateEntity")
  void patchEntity(DataProductUpdateDto dto, @MappingTarget DataProductEntity entity);

  @Mapping(target = "dataSourceSystemId", source = "dataSourceSystem.id")
  @Mapping(target = "restClientId", source = "restClient.id")
  DataProductUpdateDto toUpdateDto(DataProductEntity entity);

  TranslationPersistenceDto toTranslationPersistenceDto(DataProductNameDto translationDto);

  TranslationPersistenceDto toTranslationPersistenceDto(DataProductDescriptionDto translationDto);

  TranslationPersistenceDto toTranslationPersistenceDto(DataProductExtendedDescriptionDto translationDto);

  DataProductNameDto toNameDto(TranslationPersistenceDto translation);

  DataProductDescriptionDto toDescriptionDto(TranslationPersistenceDto translation);

  DataProductExtendedDescriptionDto toExtendedDescriptionDto(TranslationPersistenceDto translation);

  List<LinkPersistenceDto> toLinkPersistenceDtos(List<LinkDto> links);

  List<LinkDto> toLinkDtos(List<LinkPersistenceDto> links);

  DataProductStateEnum toDtoDataProductStateEnum(ch.agridata.product.persistence.DataProductStateEnum persistenceStateEnum);

  ch.agridata.product.persistence.DataProductStateEnum toPersistenceDataProductStateEnum(DataProductStateEnum dtoStateEnum);

  default String toMethodCodeName(RestClientMethodCodeEnum value) {
    return value == null ? null : value.name();
  }

  default String toFlowCodeName(FlowCodeEnum value) {
    return value == null ? null : value.name();
  }

  default RestClientMethodCodeEnum toMethodCodeEnum(String value) {
    return value == null ? null : RestClientMethodCodeEnum.valueOf(value);
  }

  default FlowCodeEnum toFlowCodeEnum(String value) {
    return value == null ? null : FlowCodeEnum.valueOf(value);
  }

  @Condition
  default <T> boolean isPresent(JsonNullable<T> nullable) {
    return nullable != null && nullable.isPresent();
  }

  default <T> T unwrap(JsonNullable<T> nullable) {
    return nullable == null ? null : nullable.orElse(null);
  }

  default <T> JsonNullable<T> wrap(T value) {
    return JsonNullable.of(value);
  }
}
