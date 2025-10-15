package ch.agridata.agreement.mapper;

import ch.agridata.agreement.dto.DataRequestDescriptionDto;
import ch.agridata.agreement.dto.DataRequestDto;
import ch.agridata.agreement.dto.DataRequestPurposeDto;
import ch.agridata.agreement.dto.DataRequestTitleDto;
import ch.agridata.agreement.dto.DataRequestUpdateDto;
import ch.agridata.agreement.persistence.DataRequestDataProductEntity;
import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.common.persistence.TranslationPersistenceDto;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

/**
 * Handles conversions between data request DTOs and their corresponding persistence entities. It ensures consistency across service and
 * repository layers.
 *
 * @CommentLastReviewed 2025-08-25
 */

@Mapper(componentModel = "jakarta")
public interface DataRequestMapper {
  @Named("convertLogoToBase64")
  static String convertLogoToBase64(DataRequestEntity entity) {
    if (entity.getDataConsumerLogo() == null || entity.getDataConsumerLogoType() == null) {
      return null;
    }
    String base64 = Base64.getEncoder().encodeToString(entity.getDataConsumerLogo());
    return "data:" + entity.getDataConsumerLogoType() + ";base64," + base64;
  }

  @Named("toLowerCase")
  static String toLowerCase(String string) {
    return string == null ? null : string.toLowerCase();
  }

  @AfterMapping
  default void mapDataProducts(DataRequestUpdateDto dto, @MappingTarget DataRequestEntity entity) {
    if (dto.products() == null) {
      return;
    }
    if (entity.getDataProducts() == null) {
      entity.setDataProducts(new ArrayList<>());
    }
    var updatedEntities = dto.products().stream()
        .map(id ->
            entity.getDataProducts().stream()
                .filter(p -> p.getDataProductId().equals(id))
                .findFirst()
                .orElseGet(() -> new DataRequestDataProductEntity(entity, id))
        ).toList();
    entity.getDataProducts().clear();
    updatedEntities.forEach(productEntity -> entity.getDataProducts().add(productEntity));
  }

  @Mapping(target = "products", source = "dataProducts")
  @Mapping(target = "id", source = "entity.id")
  @Mapping(target = "dataConsumerLogoBase64", source = "entity", qualifiedByName = "convertLogoToBase64")
  DataRequestDto toDto(DataRequestEntity entity);


  @Mapping(target = "humanFriendlyId", ignore = true)
  @Mapping(target = "submissionDate", ignore = true)
  @Mapping(target = "dataConsumerUid", ignore = true)
  @Mapping(target = "dataConsumerLegalName", ignore = true)
  @Mapping(target = "validRedirectUriRegex", ignore = true)
  // This will be handled in the AfterMapping method
  @Mapping(target = "dataProducts", ignore = true)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "archived", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "modifiedBy", ignore = true)
  @Mapping(target = "modifiedAt", ignore = true)
  @Mapping(target = "stateCode", ignore = true)
  @Mapping(target = "contactEmailAddress", source = "contactEmailAddress", qualifiedByName = "toLowerCase")
  @Mapping(target = "dataConsumerLogo", ignore = true)
  @Mapping(target = "dataConsumerLogoType", ignore = true)
  void updateEntity(DataRequestUpdateDto dataRequestUpdateDto,
                    @MappingTarget DataRequestEntity entity);

  TranslationPersistenceDto toTranslationPersistenceDto(DataRequestDescriptionDto translationDto);

  TranslationPersistenceDto toTranslationPersistenceDto(DataRequestTitleDto translationDto);

  TranslationPersistenceDto toTranslationPersistenceDto(DataRequestPurposeDto translationDto);

  @Mapping(target = "products", source = "dataProducts")
  DataRequestUpdateDto toUpdateDto(DataRequestEntity entity);

  default List<UUID> toDataRequestProduct(List<DataRequestDataProductEntity> entities) {
    return entities.stream()
        .map(DataRequestDataProductEntity::getDataProductId)
        .toList();
  }
}
