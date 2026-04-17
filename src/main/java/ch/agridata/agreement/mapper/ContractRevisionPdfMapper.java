package ch.agridata.agreement.mapper;

import ch.agridata.agreement.dto.ContractRevisionPdfDto;
import ch.agridata.agreement.persistence.ContractRevisionEntity;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
  @Mapping(target = "consumerName", source = "dataConsumerName")
  @Mapping(target = "consumerStreet", source = "dataConsumerStreet")
  @Mapping(target = "consumerZipCity", expression = "java(entity.getDataConsumerZip() + \" \" + entity.getDataConsumerCity())")
  @Mapping(target = "consumerAddressInline", source = "entity", qualifiedByName = "mapConsumerAddressInline")
  @Mapping(target = "providerName", source = "dataProviderName")
  @Mapping(target = "providerStreet", source = "dataProviderStreet")
  @Mapping(target = "providerZipCity", expression = "java(entity.getDataProviderZip() + \" \" + entity.getDataProviderCity())")
  @Mapping(target = "providerAddressInline", source = "entity", qualifiedByName = "mapProviderAddressInline")
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
}
