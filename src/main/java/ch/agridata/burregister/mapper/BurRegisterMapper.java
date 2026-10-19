package ch.agridata.burregister.mapper;

import ch.agridata.burregister.dto.BurLocalUnitDto;
import ch.agridata.burregister.dto.BurRegisterAddressDto;
import ch.agridata.burregister.dto.BurRegisterOrganisationDto;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.ObjectUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Maps a BUR local unit to the organisation DTO. Name and legal name fall back to each other, the street is joined with the house number
 * and the zip code falls back to the foreign zip code for organisations outside Switzerland.
 *
 * @CommentLastReviewed 2026-10-12
 */
@Mapper(componentModel = "jakarta", imports = ObjectUtils.class)
public interface BurRegisterMapper {

  @Mapping(target = "name", expression = "java(ObjectUtils.firstNonNull(localUnit.getName(), localUnit.getLegalName()))")
  @Mapping(target = "legalName", expression = "java(ObjectUtils.firstNonNull(localUnit.getLegalName(), localUnit.getName()))")
  @Mapping(target = "address", source = "localUnit")
  BurRegisterOrganisationDto toOrganisationDto(BurLocalUnitDto localUnit);

  @Mapping(target = "street", expression = "java(joinStreet(localUnit))")
  @Mapping(target = "zip", expression = "java(ObjectUtils.firstNonNull(localUnit.getSwissZipCode(), localUnit.getForeignZipCode()))")
  @Mapping(target = "city", source = "town")
  @Mapping(target = "country", source = "countryIdISO2")
  BurRegisterAddressDto toAddressDto(BurLocalUnitDto localUnit);

  default String toUid(BurLocalUnitDto.Uid uid) {
    return uid == null ? null : uid.getUidOrganisationIdCategorie() + uid.getUidOrganisationId();
  }

  default String joinStreet(BurLocalUnitDto localUnit) {
    var street = Stream.of(localUnit.getStreet(), localUnit.getHouseNumber())
        .filter(Objects::nonNull)
        .collect(Collectors.joining(" "));
    return street.isEmpty() ? null : street;
  }
}
