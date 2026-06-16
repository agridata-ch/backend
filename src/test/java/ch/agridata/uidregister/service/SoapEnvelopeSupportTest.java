package ch.agridata.uidregister.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.admin.uid.xmlns.uid_wse.GetByUID;
import ch.admin.uid.xmlns.uid_wse.GetByUIDResponse;
import ch.ech.xmlns.ech_0097._5.UidOrganisationIdCategorieType;
import ch.ech.xmlns.ech_0097._5.UidStructureType;
import ch.ech.xmlns.ech_0098._5.OrganisationAddressType;
import ch.ech.xmlns.ech_0108._5.OrganisationType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

class SoapEnvelopeSupportTest {

  private static final BigInteger BLW_UID = new BigInteger("146680598");

  private SoapEnvelopeSupport support;

  private static String loadResponse(String fileName) throws IOException {
    try (InputStream in = SoapEnvelopeSupportTest.class
        .getResourceAsStream("/wiremock/__files/uid-register-api/" + fileName)) {
      assertThat(in).as("test fixture %s", fileName).isNotNull();
      return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  private static GetByUID getByUidRequest(BigInteger id) {
    UidStructureType uid = new UidStructureType();
    uid.setUidOrganisationId(id);
    uid.setUidOrganisationIdCategorie(UidOrganisationIdCategorieType.CHE);
    GetByUID request = new GetByUID();
    request.setUid(uid);
    return request;
  }

  @BeforeEach
  void setUp() {
    support = new SoapEnvelopeSupport();
  }

  @Test
  void givenRequestWhenWrapThenProducesSoapEnvelopeWithQualifiedNamespaces() {
    String envelope = support.wrap(getByUidRequest(BLW_UID));

    // The soapenv wrapper is emitted by our own template, so it is stable.
    assertThat(envelope)
        .startsWith("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body>")
        .endsWith("</soapenv:Body></soapenv:Envelope>")
        .doesNotContain("<?xml");

    // JAXB assigns namespace prefixes non-deterministically, so assert on the parsed structure
    // (local name + namespace URI + text), never on a specific prefix style.
    Document document = parseNamespaceAware(envelope);

    assertThat(document.getElementsByTagNameNS("http://www.uid.admin.ch/xmlns/uid-wse", "GetByUID").getLength())
        .as("GetByUID bound to the UID-WSE namespace")
        .isEqualTo(1);
    assertThat(firstElementText(document, "uidOrganisationIdCategorie")).isEqualTo("CHE");
    assertThat(firstElementText(document, "uidOrganisationId")).isEqualTo("146680598");
  }

  private static Document parseNamespaceAware(String xml) {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      return factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      throw new IllegalStateException("Failed to parse wrapped envelope", e);
    }
  }

  private static String firstElementText(Document document, String localName) {
    NodeList nodes = document.getElementsByTagNameNS("*", localName);
    assertThat(nodes.getLength()).as("element %s present", localName).isPositive();
    return nodes.item(0).getTextContent();
  }

  @Test
  void givenRealResponseWhenUnwrapBodyThenParsesOrganisation() throws IOException {
    String responseXml = loadResponse("CHE146680598-blw.xml");

    GetByUIDResponse response = support.unwrapBody(responseXml, GetByUIDResponse.class);

    assertThat(response.getGetByUIDResult().getOrganisationType()).hasSize(1);
    OrganisationType organisationType = response.getGetByUIDResult().getOrganisationType().getFirst();
    var identification = organisationType.getOrganisation().getOrganisationIdentification();

    assertThat(identification.getUid().getUidOrganisationId()).isEqualTo(BLW_UID);
    assertThat(identification.getUid().getUidOrganisationIdCategorie()).isEqualTo(UidOrganisationIdCategorieType.CHE);
    assertThat(identification.getOrganisationName()).contains("Bundesamt für Landwirtschaft", "(BLW)");
  }

  @Test
  void givenRealResponseWhenUnwrapBodyThenBindsAddressIncludingZipElementRefs() throws IOException {
    String responseXml = loadResponse("CHE146680598-blw.xml");

    GetByUIDResponse response = support.unwrapBody(responseXml, GetByUIDResponse.class);

    OrganisationAddressType legalAddress = response.getGetByUIDResult().getOrganisationType().getFirst()
        .getOrganisation().getAddress().getFirst();

    assertThat(legalAddress.getStreet()).isEqualTo("Schwarzenburgstrasse");
    assertThat(legalAddress.getTown()).isEqualTo("Liebefeld");
    // The swissZipCode is bound through ObjectFactory element references; resolving it proves the broad JAXB context.
    assertThat(UidRegisterService.extractSwissZipCode(
        legalAddress.getSwissZipCodeOrSwissZipCodeAddOnOrMunicipalityId())).isEqualTo("3097");
  }

  @Test
  void givenFaultEnvelopeWhenUnwrapBodyThenThrowsSoapFaultExceptionWithFaultString() {
    String fault = """
        <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
          <soapenv:Body>
            <soapenv:Fault>
              <faultcode>soapenv:Server</faultcode>
              <faultstring>Business fault: UID not found</faultstring>
            </soapenv:Fault>
          </soapenv:Body>
        </soapenv:Envelope>""";

    assertThatThrownBy(() -> support.unwrapBody(fault, GetByUIDResponse.class))
        .isInstanceOf(SoapFaultException.class)
        .hasMessage("Business fault: UID not found");
  }

  @Test
  void givenFaultWithoutFaultStringWhenUnwrapBodyThenThrowsSoapFaultException() {
    String fault = """
        <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
          <soapenv:Body>
            <soapenv:Fault><faultcode>soapenv:Server</faultcode></soapenv:Fault>
          </soapenv:Body>
        </soapenv:Envelope>""";

    assertThatThrownBy(() -> support.unwrapBody(fault, GetByUIDResponse.class))
        .isInstanceOf(SoapFaultException.class);
  }

  @Test
  void givenEnvelopeWithoutBodyWhenUnwrapBodyThenThrowsIllegalState() {
    String noBody = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"></soapenv:Envelope>";

    assertThatThrownBy(() -> support.unwrapBody(noBody, GetByUIDResponse.class))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("did not contain a Body");
  }

  @Test
  void givenEmptyBodyWhenUnwrapBodyThenThrowsIllegalState() {
    String emptyBody = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">"
        + "<soapenv:Body></soapenv:Body></soapenv:Envelope>";

    assertThatThrownBy(() -> support.unwrapBody(emptyBody, GetByUIDResponse.class))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("did not contain a payload element");
  }

  @Test
  void givenMalformedXmlWhenUnwrapBodyThenThrowsIllegalState() {
    assertThatThrownBy(() -> support.unwrapBody("this is not xml <<<", GetByUIDResponse.class))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Failed to parse SOAP response envelope");
  }

  @Test
  void givenResponseWithDoctypeWhenUnwrapBodyThenRejectsToPreventXxe() {
    String withDoctype = """
        <?xml version="1.0"?>
        <!DOCTYPE foo [<!ENTITY xxe "expanded">]>
        <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
          <soapenv:Body><foo>&xxe;</foo></soapenv:Body>
        </soapenv:Envelope>""";

    assertThatThrownBy(() -> support.unwrapBody(withDoctype, GetByUIDResponse.class))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Failed to parse SOAP response envelope");
  }
}