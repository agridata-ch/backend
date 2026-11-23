package ch.agridata.user.mapper;

import ch.agridata.agis.dto.AgisAddressInformationType;
import ch.agridata.agis.dto.AgisFarmType;
import ch.agridata.agis.dto.AgisMailAddressType;
import ch.agridata.agis.dto.AgisOrganisationMailAddressInfoType;
import ch.agridata.user.dto.AddressDto;
import ch.agridata.user.dto.CompanyDto;
import ch.agridata.user.dto.FarmDto;
import ch.agridata.user.dto.FarmTypeEnum;
import jakarta.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper for converting AGIS register data into the company, farm and address DTOs of the farm overview.
 *
 * @CommentLastReviewed 2026-11-23
 */
@Mapper(componentModel = "jakarta", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface FarmOverviewMapper {

  @Mapping(target = "address", source = "postalAddress.addressInformation")
  CompanyDto toCompanyDto(String uid, String name, @Nullable AgisMailAddressType postalAddress);

  @Mapping(target = "bur", source = "ber")
  @Mapping(target = "farmTypeCode", source = "farmType", qualifiedByName = "toFarmType")
  @Mapping(target = "address", source = "addressInformation")
  FarmDto toFarmDto(AgisFarmType farm);

  @Mapping(target = "addressLine", source = "addressLine1")
  @Mapping(target = "postOfficeBox", source = ".", qualifiedByName = "toPostOfficeBox")
  @Mapping(target = "zipCode", source = ".", qualifiedByName = "toZipCode")
  AddressDto toAddressDto(AgisAddressInformationType address);

  /**
   * Returns the organisation name, or the person's full name if the address belongs to a natural person.
   */
  default Optional<String> toName(AgisMailAddressType postalAddress) {
    return Optional.ofNullable(postalAddress.getOrganisation())
        .map(AgisOrganisationMailAddressInfoType::getOrganisationName)
        .or(() -> Optional.ofNullable(postalAddress.getPerson())
            .map(person -> joinNonNull(person.getFirstName(), person.getLastName())));
  }

  @Named("toFarmType")
  default FarmTypeEnum toFarmType(String farmType) {
    return FarmTypeEnum.fromNumber(farmType);
  }

  @Named("toPostOfficeBox")
  default String toPostOfficeBox(AgisAddressInformationType address) {
    return joinNonNull(address.getPostOfficeBoxText(), Objects.toString(address.getPostOfficeBoxNumber(), null));
  }

  @Named("toZipCode")
  default String toZipCode(AgisAddressInformationType address) {
    return address.getSwissZipCode() != null ? address.getSwissZipCode().toString() : address.getForeignZipCode();
  }

  private static String joinNonNull(String... parts) {
    var joined = Stream.of(parts).filter(Objects::nonNull).collect(Collectors.joining(" "));
    return joined.isEmpty() ? null : joined;
  }
}
