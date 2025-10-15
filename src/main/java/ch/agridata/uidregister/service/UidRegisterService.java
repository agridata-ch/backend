package ch.agridata.uidregister.service;


import ch.admin.uid.xmlns.uid_wse.ArrayOfOrganisationType;
import ch.admin.uid.xmlns.uid_wse.IPublicServices;
import ch.admin.uid.xmlns.uid_wse.IPublicServicesGetByUIDBusinessFaultFaultFaultMessage;
import ch.admin.uid.xmlns.uid_wse.IPublicServicesGetByUIDInfrastructureFaultFaultFaultMessage;
import ch.admin.uid.xmlns.uid_wse.IPublicServicesGetByUIDSecurityFaultFaultFaultMessage;
import ch.agridata.common.exceptions.ExternalWebServiceException;
import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.uidregister.api.UidRegisterServiceApi;
import ch.agridata.uidregister.dto.UidRegisterAddressDto;
import ch.agridata.uidregister.dto.UidRegisterOrganisationDto;
import ch.ech.xmlns.ech_0097._5.UidOrganisationIdCategorieType;
import ch.ech.xmlns.ech_0097._5.UidStructureType;
import ch.ech.xmlns.ech_0098._5.OrganisationType;
import io.quarkiverse.cxf.annotation.CXFClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.xml.bind.JAXBElement;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.namespace.QName;
import lombok.extern.slf4j.Slf4j;

/**
 * Implements the UidRegisterServiceApi. It retrieves organizations by UID, extracts structured address details, and integrates with user
 * identity to resolve the current user’s UID. The implementation provides error handling for missing or invalid responses from the
 * register.
 *
 * @CommentLastReviewed 2025-08-25
 */

@ApplicationScoped
@Slf4j
public class UidRegisterService implements UidRegisterServiceApi {

  private static final QName SWISS_ZIP_CODE_QNAME = new QName("http://www.ech.ch/xmlns/eCH-0098/5", "swissZipCode");

  @Inject
  public AgridataSecurityIdentity agridataSecurityIdentity;
  @Inject
  @CXFClient("uid")
  IPublicServices port;

  /**
   * Extracts the value of the 'swissZipCode' from the given list.
   *
   * @param elements the list swissZipCodeOrSwissZipCodeAddOnOrMunicipalityId
   * @return the value of swissZipCode, or null if not present
   */
  public static String extractSwissZipCode(List<JAXBElement<?>> elements) {
    if (elements == null) {
      return null;
    }
    for (JAXBElement<?> el : elements) {
      if (SWISS_ZIP_CODE_QNAME.equals(el.getName())) {
        Object value = el.getValue();
        return value != null ? value.toString() : null;
      }
    }
    return null;
  }

  private static OrganisationType getOrganisationFromResponse(ArrayOfOrganisationType result) {
    if (result == null || result.getOrganisationType() == null || result.getOrganisationType().isEmpty()) {
      throw new NotFoundException("Organisation not found for the given UID.");
    }
    var organisationType = result.getOrganisationType().getFirst();
    if (organisationType == null || organisationType.getOrganisation() == null) {
      throw new NotFoundException("Uid Search returned no valid organisationType.");
    }
    return organisationType.getOrganisation();

  }

  @Override
  public UidRegisterOrganisationDto getByUidOfCurrentUser() {
    String uid = agridataSecurityIdentity.getUidOrElseThrow();
    BigInteger uidWithoutPrefix = new BigInteger(uid.replace(UidOrganisationIdCategorieType.CHE.name(), ""));
    return getByUid(UidOrganisationIdCategorieType.CHE, uidWithoutPrefix);
  }

  private UidRegisterOrganisationDto toUidOrganisationDto(ArrayOfOrganisationType result) {
    var organisation = getOrganisationFromResponse(result);
    if (
        organisation == null
            || organisation.getOrganisationIdentification() == null
            || organisation.getOrganisationIdentification().getOrganisationLegalName() == null
    ) {
      throw new NotFoundException("Uid Search returned no valid organisationIdentification.");
    }
    var organisationIdentification = organisation.getOrganisationIdentification();
    var organisationBuilder = UidRegisterOrganisationDto.builder()
        .name(organisationIdentification.getOrganisationName())
        .legalName(organisationIdentification.getOrganisationLegalName())
        .uid(organisationIdentification.getUid().getUidOrganisationIdCategorie()
            + organisationIdentification.getUid().getUidOrganisationId().toString());

    if (organisation.getAddress() != null && !organisation.getAddress().isEmpty()) {
      var address = organisation.getAddress().getFirst();
      if (address != null) {
        var street = Stream.of(address.getStreet(), address.getHouseNumber())
            .filter(Objects::nonNull)
            .collect(Collectors.joining(" "));
        var addressDtoBuilder = UidRegisterAddressDto.builder()
            .street(street)
            .zip(extractSwissZipCode(address.getSwissZipCodeOrSwissZipCodeAddOnOrMunicipalityId()))
            .city(address.getTown())
            .country(address.getCountryIdISO2());
        organisationBuilder.address(addressDtoBuilder.build());
      }
    }

    return organisationBuilder.build();
  }

  @Override
  public UidRegisterOrganisationDto getByUid(UidOrganisationIdCategorieType category, BigInteger id) {
    UidStructureType uidStructureType = new UidStructureType();
    uidStructureType.setUidOrganisationId(id);
    uidStructureType.setUidOrganisationIdCategorie(UidOrganisationIdCategorieType.CHE);
    try {
      return toUidOrganisationDto(port.getByUID(uidStructureType));
    } catch (IPublicServicesGetByUIDBusinessFaultFaultFaultMessage
             | IPublicServicesGetByUIDInfrastructureFaultFaultFaultMessage
             | IPublicServicesGetByUIDSecurityFaultFaultFaultMessage e) {
      throw new ExternalWebServiceException("Uid lookup failed for UID: " + category + id + " error: " + e.getMessage(), e);
    }
  }


}
