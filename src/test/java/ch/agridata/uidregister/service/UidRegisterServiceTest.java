package ch.agridata.uidregister.service;

import static ch.ech.xmlns.ech_0097._5.UidOrganisationIdCategorieType.CHE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.admin.uid.xmlns.uid_wse.ArrayOfOrganisationType;
import ch.admin.uid.xmlns.uid_wse.IPublicServices;
import ch.admin.uid.xmlns.uid_wse.IPublicServicesGetByUIDBusinessFaultFaultFaultMessage;
import ch.admin.uid.xmlns.uid_wse.IPublicServicesGetByUIDInfrastructureFaultFaultFaultMessage;
import ch.admin.uid.xmlns.uid_wse.IPublicServicesGetByUIDSecurityFaultFaultFaultMessage;
import ch.agridata.common.exceptions.ExternalWebServiceException;
import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.uidregister.dto.UidRegisterOrganisationDto;
import ch.ech.xmlns.ech_0097._5.OrganisationIdentificationType;
import ch.ech.xmlns.ech_0097._5.UidStructureType;
import ch.ech.xmlns.ech_0098._5.OrganisationAddressType;
import ch.ech.xmlns.ech_0098._5.OrganisationType;
import jakarta.ws.rs.NotFoundException;
import jakarta.xml.bind.JAXBElement;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import javax.xml.namespace.QName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class UidRegisterServiceTest {

  private static final BigInteger TEST_UID = new BigInteger("123456789");
  @InjectMocks
  UidRegisterService uidRegisterService;
  @Mock
  IPublicServices port;
  @Mock
  AgridataSecurityIdentity agridataSecurityIdentity;

  // Test data providers
  private static Stream<Arguments> webServiceExceptions() {
    return Stream.of(
        Arguments.of(mock(IPublicServicesGetByUIDBusinessFaultFaultFaultMessage.class)),
        Arguments.of(mock(IPublicServicesGetByUIDInfrastructureFaultFaultFaultMessage.class)),
        Arguments.of(mock(IPublicServicesGetByUIDSecurityFaultFaultFaultMessage.class))
    );
  }

  private static Stream<Arguments> invalidResponses() {
    return Stream.of(
        Arguments.of(null, "Organisation not found for the given UID."),
        Arguments.of(createEmptyResponse(), "Organisation not found for the given UID."),
        Arguments.of(createResponseWithNullOrganisation(), "Uid Search returned no valid organisationType."),
        Arguments.of(createResponseWithNullIdentification(), "Uid Search returned no valid organisationIdentification.")
    );
  }

  private static Stream<Arguments> invalidZipCodeScenarios() {
    QName otherQName = new QName("http://www.ech.ch/xmlns/eCH-0098/5", "other");
    QName zipQName = new QName("http://www.ech.ch/xmlns/eCH-0098/5", "swissZipCode");

    return Stream.of(
        Arguments.of((List<JAXBElement<?>>) null),
        Arguments.of(Collections.emptyList()),
        Arguments.of(Arrays.asList(new JAXBElement<>(otherQName, String.class, "value"))),
        Arguments.of(Arrays.asList(new JAXBElement<>(zipQName, String.class, null)))
    );
  }

  private static ArrayOfOrganisationType createEmptyResponse() {
    ArrayOfOrganisationType response = new ArrayOfOrganisationType();
    response.getOrganisationType().clear();
    return response;
  }

  private static ArrayOfOrganisationType createResponseWithNullOrganisation() {
    ch.ech.xmlns.ech_0108._5.OrganisationType orgType = new ch.ech.xmlns.ech_0108._5.OrganisationType();
    orgType.setOrganisation(null);

    ArrayOfOrganisationType response = new ArrayOfOrganisationType();
    response.getOrganisationType().add(orgType);
    return response;
  }

  private static ArrayOfOrganisationType createResponseWithNullIdentification() {
    OrganisationType organisation = new OrganisationType();
    organisation.setOrganisationIdentification(null);

    ch.ech.xmlns.ech_0108._5.OrganisationType orgType = new ch.ech.xmlns.ech_0108._5.OrganisationType();
    orgType.setOrganisation(organisation);

    ArrayOfOrganisationType response = new ArrayOfOrganisationType();
    response.getOrganisationType().add(orgType);
    return response;
  }

  @Test
  void givenValidUidWithCompleteDataWhenGetByUidThenReturnFullOrganisationDto() throws Exception {
    // Given
    ArrayOfOrganisationType response = createCompleteResponse();
    when(port.getByUID(any(UidStructureType.class))).thenReturn(response);

    // When
    UidRegisterOrganisationDto result = uidRegisterService.getByUid(CHE, TEST_UID);

    // Then
    assertThat(result)
        .isNotNull()
        .satisfies(dto -> {
          assertThat(dto.name()).isEqualTo("Test Org");
          assertThat(dto.legalName()).isEqualTo("Test Org Ltd");
          assertThat(dto.uid()).isEqualTo("CHE" + TEST_UID);
          assertThat(dto.address()).isNotNull()
              .satisfies(address -> {
                assertThat(address.street()).isEqualTo("Main St 42");
                assertThat(address.zip()).isEqualTo("1234");
                assertThat(address.city()).isEqualTo("Test City");
                assertThat(address.country()).isEqualTo("CH");
              });
        });

    ArgumentCaptor<UidStructureType> captor = ArgumentCaptor.forClass(UidStructureType.class);
    verify(port).getByUID(captor.capture());
    assertThat(captor.getValue())
        .satisfies(uid -> {
          assertThat(uid.getUidOrganisationId()).isEqualTo(TEST_UID);
          assertThat(uid.getUidOrganisationIdCategorie()).isEqualTo(CHE);
        });
  }

  @Test
  void givenValidUidWithoutAddressWhenGetByUidThenReturnOrganisationDtoWithoutAddress() throws Exception {
    // Given
    ArrayOfOrganisationType response = createResponseWithoutAddress();
    when(port.getByUID(any(UidStructureType.class))).thenReturn(response);

    // When
    UidRegisterOrganisationDto result = uidRegisterService.getByUid(CHE, TEST_UID);

    // Then
    assertThat(result.address()).isNull();
  }

  @Test
  void givenValidUidWithPartialStreetDataWhenGetByUidThenConcatenateOnlyAvailableStreetParts() throws Exception {
    // Given
    ArrayOfOrganisationType response = createResponseWithPartialStreet();
    when(port.getByUID(any(UidStructureType.class))).thenReturn(response);

    // When
    UidRegisterOrganisationDto result = uidRegisterService.getByUid(CHE, TEST_UID);

    // Then
    assertThat(result.address().street()).isEqualTo("Main St");
  }

  @Test
  void givenUserHasNoUidWhenGetByUidOfCurrentUserThenThrowUidMissingException() throws Exception {
    // Given
    when(port.getByUID(any(UidStructureType.class))).thenThrow(new NotFoundException("No UID found for current user."));
    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn("CHE101708094");

    // When & Then
    assertThatThrownBy(() -> uidRegisterService.getByUidOfCurrentUser())
        .isInstanceOf(NotFoundException.class)
        .hasMessage("No UID found for current user.");
  }

  @ParameterizedTest
  @MethodSource("webServiceExceptions")
  void givenWebServiceExceptionWhenGetByUidThenThrowExternalWebServiceException(Exception exception) throws Exception {
    // Given
    when(port.getByUID(any(UidStructureType.class))).thenThrow(exception);

    // When & Then
    assertThatThrownBy(() -> uidRegisterService.getByUid(CHE, TEST_UID))
        .isInstanceOf(ExternalWebServiceException.class)
        .hasCause(exception);
  }

  @ParameterizedTest
  @MethodSource("invalidResponses")
  void givenInvalidResponseWhenGetByUidThenThrowNotFoundException(ArrayOfOrganisationType response, String expectedMessage)
      throws Exception {
    // Given
    when(port.getByUID(any(UidStructureType.class))).thenReturn(response);

    // When & Then
    assertThatThrownBy(() -> uidRegisterService.getByUid(CHE, TEST_UID))
        .isInstanceOf(NotFoundException.class)
        .hasMessage(expectedMessage);
  }

  @Test
  void givenValidZipCodeElementWhenExtractSwissZipCodeThenReturnZipCode() {
    // Given
    QName zipQName = new QName("http://www.ech.ch/xmlns/eCH-0098/5", "swissZipCode");
    JAXBElement<String> zipElement = new JAXBElement<>(zipQName, String.class, "1234");
    List<JAXBElement<?>> elements = Arrays.asList(zipElement);

    // When
    String result = UidRegisterService.extractSwissZipCode(elements);

    // Then
    assertThat(result).isEqualTo("1234");
  }

  @ParameterizedTest
  @MethodSource("invalidZipCodeScenarios")
  void givenInvalidZipCodeScenariosWhenExtractSwissZipCodeThenReturnNull(List<JAXBElement<?>> elements) {
    // When
    String result = UidRegisterService.extractSwissZipCode(elements);

    // Then
    assertThat(result).isNull();
  }

  // Helper methods
  private ArrayOfOrganisationType createCompleteResponse() {
    UidStructureType uidStructure = new UidStructureType();
    uidStructure.setUidOrganisationId(TEST_UID);
    uidStructure.setUidOrganisationIdCategorie(CHE);

    OrganisationIdentificationType identification = new OrganisationIdentificationType();
    identification.setOrganisationName("Test Org");
    identification.setOrganisationLegalName("Test Org Ltd");
    identification.setUid(uidStructure);

    OrganisationAddressType address = new OrganisationAddressType();
    address.setStreet("Main St");
    address.setHouseNumber("42");
    address.setTown("Test City");
    address.setCountryIdISO2("CH");

    QName zipQName = new QName("http://www.ech.ch/xmlns/eCH-0098/5", "swissZipCode");
    JAXBElement<String> zipElement = new JAXBElement<>(zipQName, String.class, "1234");
    address.getSwissZipCodeOrSwissZipCodeAddOnOrMunicipalityId().add(zipElement);

    OrganisationType organisation = new OrganisationType();
    organisation.setOrganisationIdentification(identification);
    organisation.getAddress().add(address);

    ch.ech.xmlns.ech_0108._5.OrganisationType orgType = new ch.ech.xmlns.ech_0108._5.OrganisationType();
    orgType.setOrganisation(organisation);

    ArrayOfOrganisationType response = new ArrayOfOrganisationType();
    response.getOrganisationType().add(orgType);
    return response;
  }

  private ArrayOfOrganisationType createResponseWithoutAddress() {
    ArrayOfOrganisationType response = createCompleteResponse();
    response.getOrganisationType().get(0).getOrganisation().getAddress().clear();
    return response;
  }

  private ArrayOfOrganisationType createResponseWithPartialStreet() {
    ArrayOfOrganisationType response = createCompleteResponse();
    response.getOrganisationType().get(0).getOrganisation().getAddress().get(0).setHouseNumber(null);
    return response;
  }

}
