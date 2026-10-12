package ch.agridata.burregister.service;

import static ch.agridata.burregister.service.BurRegisterSoapSupportTest.BLW_FIELDS;
import static ch.agridata.burregister.service.BurRegisterSoapSupportTest.FAULT_RESPONSE;
import static ch.agridata.burregister.service.BurRegisterSoapSupportTest.NIL_RESPONSE;
import static ch.agridata.burregister.service.BurRegisterSoapSupportTest.resultResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ch.agridata.burregister.api.BurRegisterServiceApi;
import ch.agridata.burregister.dto.BurRegisterAddressDto;
import ch.agridata.burregister.mapper.BurRegisterMapperImpl;
import ch.agridata.common.exceptions.ExternalWebServiceException;
import ch.agridata.common.security.AgridataSecurityIdentity;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class BurRegisterServiceTest {

  private final BurRegisterSoapClient soapClient = mock(BurRegisterSoapClient.class);
  private final AgridataSecurityIdentity identity = mock(AgridataSecurityIdentity.class);
  private final BurRegisterServiceApi self = mock(BurRegisterServiceApi.class);
  private BurRegisterService service;

  @BeforeEach
  void setUp() {
    service =
        new BurRegisterService(identity, soapClient, new BurRegisterSoapSupport("https://bur.example"), new BurRegisterMapperImpl(), self);
  }

  private void stubResponse(String xml) {
    var response = mock(Response.class);
    when(response.readEntity(String.class)).thenReturn(xml);
    when(soapClient.getLocalUnitByUid(any())).thenReturn(response);
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"113614519", "CHE-113.614.519", "CHE12345", "CHE1136145190", "CHE11361451<"})
  void invalidUid_isRejectedBeforeCall(String uid) {
    assertThatThrownBy(() -> service.getByUid(uid)).isInstanceOf(ValidationException.class);
    verifyNoInteractions(soapClient);
  }

  @Test
  void knownUid_isMapped() {
    stubResponse(resultResponse(BLW_FIELDS));

    var result = service.getByUid("CHE113614519");

    assertThat(result.uid()).isEqualTo("CHE113614519");
    assertThat(result.name()).isEqualTo("Bundesamt für Landwirtschaft BLW");
    assertThat(result.legalName()).isEqualTo("Bundesamt für Landwirtschaft BLW");
    assertThat(result.address()).isEqualTo(new BurRegisterAddressDto("Schwarzenburgstrasse 165", "3008", "Bern", "CH"));
  }

  @Test
  void foreignOrganisation_usesForeignZipCode() {
    stubResponse(resultResponse("""
        <b:countryIdISO2>DE</b:countryIdISO2>
        <b:foreignZipCode>12345</b:foreignZipCode>
        <b:houseNumber>10</b:houseNumber>
        <b:legalName>Foreign Company Legal Name</b:legalName>
        <b:name>Foreign Company</b:name>
        <b:street>Dummy Street</b:street>
        <b:swissZipCode i:nil="true"/>
        <b:town>Dummy City</b:town>
        <b:uid><b:uidOrganisationId>123456789</b:uidOrganisationId><b:uidOrganisationIdCategorie>CHE</b:uidOrganisationIdCategorie></b:uid>"""));

    var result = service.getByUid("CHE123456789");

    assertThat(result.uid()).isEqualTo("CHE123456789");
    assertThat(result.address()).isEqualTo(new BurRegisterAddressDto("Dummy Street 10", "12345", "Dummy City", "DE"));
  }

  @Test
  void missingHouseNumber_streetWithoutTrailingSpace() {
    stubResponse(resultResponse("""
        <b:houseNumber i:nil="true"/>
        <b:name>Only Name</b:name>
        <b:street>Hauptstrasse</b:street>
        <b:uid><b:uidOrganisationId>113614519</b:uidOrganisationId><b:uidOrganisationIdCategorie>CHE</b:uidOrganisationIdCategorie></b:uid>"""));

    var result = service.getByUid("CHE113614519");

    assertThat(result.address().street()).isEqualTo("Hauptstrasse");
    assertThat(result.legalName()).isEqualTo("Only Name");
  }

  @Test
  void nilResult_isNotFound() {
    stubResponse(NIL_RESPONSE);

    assertThatThrownBy(() -> service.getByUid("CHE999999999")).isInstanceOf(NotFoundException.class);
  }

  @Test
  void noName_isNotFound() {
    stubResponse(resultResponse("<b:name i:nil=\"true\"/><b:town>Bern</b:town>"));

    assertThatThrownBy(() -> service.getByUid("CHE113614519")).isInstanceOf(NotFoundException.class);
  }

  @Test
  void fault_isExternalWebServiceError() {
    var errorResponse = mock(Response.class);
    when(errorResponse.readEntity(String.class)).thenReturn(FAULT_RESPONSE);
    when(errorResponse.getStatusInfo()).thenReturn(Response.Status.INTERNAL_SERVER_ERROR);
    var exception = new WebApplicationException(errorResponse);
    when(soapClient.getLocalUnitByUid(any())).thenThrow(exception);

    assertThatThrownBy(() -> service.getByUid("CHE113614519"))
        .isInstanceOf(ExternalWebServiceException.class)
        .hasMessageContaining("CHE113614519")
        .hasMessageContaining("Access denied");
  }
}
