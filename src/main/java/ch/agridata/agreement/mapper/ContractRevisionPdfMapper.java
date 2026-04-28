package ch.agridata.agreement.mapper;

import ch.agridata.agreement.dto.ContractRevisionPdfDto;
import ch.agridata.agreement.dto.ContractRevisionPdfTranslationDto;
import ch.agridata.agreement.persistence.ContractRevisionEntity;
import ch.agridata.common.persistence.TranslationPersistenceDto;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * Mapper for converting ContractRevisionEntity into PDF-ready Data Transfer Objects.
 * This mapper handles the flattening of address components and performs
 * specific string formatting for PDF layout requirements, such as
 * inline address lines and combined Zip/City strings.
 *
 * @CommentLastReviewed: 2026-04-17
 */

@Mapper(componentModel = "jakarta")
public interface ContractRevisionPdfMapper {
  @Mapping(target = "requestTitle", source = "title", qualifiedByName = "toContractRevisionPdfTranslationDto")
  @Mapping(target = "requestDescription", source = "description", qualifiedByName = "toContractRevisionPdfTranslationDto")
  @Mapping(target = "requestPurpose", source = "purpose", qualifiedByName = "toContractRevisionPdfTranslationDto")
  @Mapping(target = "products", source = "dataProducts", qualifiedByName = "toContractRevisionPdfTranslationDtoList")
  @Mapping(target = "consumerName", source = "dataConsumerName")
  @Mapping(target = "consumerStreet", source = "dataConsumerStreet")
  @Mapping(target = "consumerZipCity", expression = "java(entity.getDataConsumerZip() + \" \" + entity.getDataConsumerCity())")
  @Mapping(target = "consumerAddressInline", source = "entity", qualifiedByName = "mapConsumerAddressInline")
  @Mapping(target = "consumerPhoneNumber", source = "contactPhoneNumber")
  @Mapping(target = "consumerEmailAddress", source = "contactEmailAddress")
  @Mapping(target = "consumerUid", source = "dataConsumerUid")
  @Mapping(target = "providerName", source = "dataProviderName")
  @Mapping(target = "providerStreet", source = "dataProviderStreet")
  @Mapping(target = "providerZipCity", expression = "java(entity.getDataProviderZip() + \" \" + entity.getDataProviderCity())")
  @Mapping(target = "providerAddressInline", source = "entity", qualifiedByName = "mapProviderAddressInline")
  @Mapping(target = "providerSystemName", source = "systemName", qualifiedByName = "toContractRevisionPdfTranslationDto")
  @Mapping(target = "consumerSignatureDate1",
      source = "consumerSignatureTimestamp1",
      qualifiedByName = "toSwissDate")
  @Mapping(target = "consumerSignatureDate2",
      source = "consumerSignatureTimestamp2",
      qualifiedByName = "toSwissDate")
  @Mapping(target = "providerSignatureDate1",
      source = "providerSignatureTimestamp1",
      qualifiedByName = "toSwissDate")
  @Mapping(target = "providerSignatureDate2",
      source = "providerSignatureTimestamp2",
      qualifiedByName = "toSwissDate")
  @Mapping(target = "targetGroup", source = "targetGroup")
  ContractRevisionPdfDto toPdfDto(ContractRevisionEntity entity);

  @Named("mapConsumerAddressInline")
  default String mapConsumerAddressInline(ContractRevisionEntity entity) {
    return String.format("%s, %s, %s %s",
        entity.getDataConsumerName(),
        entity.getDataConsumerStreet(),
        entity.getDataConsumerZip(),
        entity.getDataConsumerCity());
  }

  @Named("mapProviderAddressInline")
  default String mapProviderAddressInline(ContractRevisionEntity entity) {
    return String.format("%s, %s, %s %s",
        entity.getDataProviderName(),
        entity.getDataProviderStreet(),
        entity.getDataProviderZip(),
        entity.getDataProviderCity());
  }

  @Named("toSwissDate")
  default String toSwissDate(LocalDateTime dateTime) {
    if (dateTime == null) {
      return null;
    }
    return dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
  }

  @Named("toContractRevisionPdfTranslationDto")
  default ContractRevisionPdfTranslationDto toContractRevisionPdfTranslationDto(TranslationPersistenceDto dto) {
    return dto == null ? null : ContractRevisionPdfTranslationDto.builder().de(dto.de()).fr(dto.fr()).it(dto.it()).build();
  }


  @Named("toContractRevisionPdfTranslationDtoList")
  default List<ContractRevisionPdfTranslationDto> toContractRevisionPdfTranslationDtoList(
      List<TranslationPersistenceDto> dtos
  ) {
    if (dtos == null || dtos.isEmpty()) {
      return List.of();
    }

    return dtos.stream()
        .map(this::toContractRevisionPdfTranslationDto)
        .toList();
  }
}
