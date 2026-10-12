package ch.agridata.burregister.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class BurRegisterSoapSupportTest {

  static final String URL = "https://bur.example/QueryServiceV1X8.svc";

  static final String NIL_RESPONSE = """
      <s:Envelope xmlns:s="http://www.w3.org/2003/05/soap-envelope"><s:Body>
      <GetLocalUnitByUidResponse xmlns="http://burweb2.admin.ch/">
      <GetLocalUnitByUidResult i:nil="true" xmlns:i="http://www.w3.org/2001/XMLSchema-instance"/>
      </GetLocalUnitByUidResponse></s:Body></s:Envelope>""";

  static final String FAULT_RESPONSE = """
      <s:Envelope xmlns:s="http://www.w3.org/2003/05/soap-envelope"><s:Body><s:Fault>
      <s:Code><s:Value>s:Sender</s:Value></s:Code>
      <s:Reason><s:Text xml:lang="de-CH">Access denied</s:Text></s:Reason>
      </s:Fault></s:Body></s:Envelope>""";

  /**
   * Builds a response envelope around the given result fields, in the shape returned by BurWeb.
   */
  static String resultResponse(String fields) {
    return """
        <s:Envelope xmlns:s="http://www.w3.org/2003/05/soap-envelope"><s:Body>
        <GetLocalUnitByUidResponse xmlns="http://burweb2.admin.ch/">
        <GetLocalUnitByUidResult xmlns:b="http://schemas.datacontract.org/2004/07/CH.Admin.BIT.BurWeb.Services.External.V1_8"
            xmlns:i="http://www.w3.org/2001/XMLSchema-instance">
        %s
        </GetLocalUnitByUidResult></GetLocalUnitByUidResponse></s:Body></s:Envelope>""".formatted(fields);
  }

  static final String BLW_FIELDS = """
      <b:countryIdISO2>CH</b:countryIdISO2>
      <b:foreignZipCode i:nil="true"/>
      <b:houseNumber>165</b:houseNumber>
      <b:legalName>Bundesamt für Landwirtschaft BLW</b:legalName>
      <b:name>Bundesamt für Landwirtschaft BLW</b:name>
      <b:street>Schwarzenburgstrasse</b:street>
      <b:swissZipCode>3008</b:swissZipCode>
      <b:town>Bern</b:town>
      <b:uid><b:uidOrganisationId>113614519</b:uidOrganisationId><b:uidOrganisationIdCategorie>CHE</b:uidOrganisationIdCategorie></b:uid>
      <b:uidMainUnit><b:uidOrganisationId>999999999</b:uidOrganisationId><b:uidOrganisationIdCategorie>CHE</b:uidOrganisationIdCategorie></b:uidMainUnit>""";

  private final BurRegisterSoapSupport support = new BurRegisterSoapSupport(URL);

  @Test
  void buildEnvelope_containsUidActionAndTo() {
    var envelope = support.buildEnvelope("CHE113614519");

    assertThat(envelope)
        .contains("<bur:uid>CHE113614519</bur:uid>")
        .contains("<wsa:Action soap:mustUnderstand=\"1\">http://burweb2.admin.ch/IQueryServiceV1X8/GetLocalUnitByUid</wsa:Action>")
        .contains("<wsa:To soap:mustUnderstand=\"1\">" + URL + "</wsa:To>")
        .contains("xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\"");
  }

  @Test
  void extractResult_fault_throwsWithReason() {
    assertThatThrownBy(() -> support.extractResult(FAULT_RESPONSE))
        .isInstanceOf(SoapFaultException.class)
        .hasMessage("Access denied");
  }

  @Test
  void extractResult_nilResult_isEmpty() {
    assertThat(support.extractResult(NIL_RESPONSE)).isEmpty();
  }

  @Test
  void extractResult_blwResponse_returnsResult() {
    var result = support.extractResult(resultResponse(BLW_FIELDS)).orElseThrow();

    assertThat(result.getTown()).isEqualTo("Bern");
    assertThat(result.getForeignZipCode()).isNull();
    assertThat(result.getUid().getUidOrganisationId()).isEqualTo("113614519");
  }

  @Test
  void extractResult_noXml_throwsIllegalState() {
    assertThatThrownBy(() -> support.extractResult("<html>Unauthorized"))
        .isInstanceOf(IllegalStateException.class);
  }
}
