package ch.agridata.burregister.service;

import ch.agridata.burregister.dto.BurLocalUnitDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Builds and parses SOAP 1.2 envelopes for the BurWeb {@code GetLocalUnitByUid} call without a SOAP stack. The request is a string
 * template with WS-Addressing headers (required by BurWeb); the response is parsed with a hardened DOM parser, SOAP 1.2 faults are
 * detected and the result element is unmarshalled into a {@link BurLocalUnitDto}.
 *
 * @CommentLastReviewed 2026-10-12
 */
@ApplicationScoped
public class BurRegisterSoapSupport {

  private static final String SOAP12_NS = "http://www.w3.org/2003/05/soap-envelope";
  private static final String BUR_SERVICE_NS = "http://burweb2.admin.ch/";

  private static final String ENVELOPE_TEMPLATE = """
      <soap:Envelope xmlns:soap="%s" xmlns:wsa="http://www.w3.org/2005/08/addressing" xmlns:bur="%s">\
      <soap:Header>\
      <wsa:Action soap:mustUnderstand="1">%s</wsa:Action>\
      <wsa:To soap:mustUnderstand="1">%s</wsa:To>\
      </soap:Header>\
      <soap:Body><bur:GetLocalUnitByUid><bur:uid>%s</bur:uid></bur:GetLocalUnitByUid></soap:Body>\
      </soap:Envelope>""";

  private final String endpointUrl;
  private final DocumentBuilderFactory documentBuilderFactory = createHardenedDocumentBuilderFactory();
  private final JAXBContext jaxbContext;

  public BurRegisterSoapSupport(@ConfigProperty(name = "quarkus.rest-client.bur-register.url") String endpointUrl) {
    this.endpointUrl = endpointUrl;
    try {
      this.jaxbContext = JAXBContext.newInstance(BurLocalUnitDto.class);
    } catch (JAXBException e) {
      throw new IllegalStateException("Failed to initialize JAXB context for BUR model", e);
    }
  }

  /**
   * Builds the {@code GetLocalUnitByUid} request envelope. The UID must already be validated, it is inserted without escaping.
   */
  public String buildEnvelope(String uid) {
    return ENVELOPE_TEMPLATE.formatted(SOAP12_NS, BUR_SERVICE_NS, BurRegisterSoapClient.GET_LOCAL_UNIT_BY_UID_ACTION, endpointUrl, uid);
  }

  /**
   * Parses the response envelope and returns the local unit, or empty if the result is absent or nil (unknown UID).
   *
   * @throws SoapFaultException    if the response is a SOAP 1.2 fault
   * @throws IllegalStateException if the response cannot be parsed
   */
  public Optional<BurLocalUnitDto> extractResult(String responseXml) {
    Document document = parse(responseXml);

    var faults = document.getElementsByTagNameNS(SOAP12_NS, "Fault");
    if (faults.getLength() > 0) {
      var fault = (Element) faults.item(0);
      var reasons = fault.getElementsByTagNameNS(SOAP12_NS, "Text");
      throw new SoapFaultException(reasons.getLength() > 0 ? reasons.item(0).getTextContent() : fault.getTextContent());
    }

    var results = document.getElementsByTagNameNS(BUR_SERVICE_NS, "GetLocalUnitByUidResult");
    if (results.getLength() == 0) {
      return Optional.empty();
    }
    var result = (Element) results.item(0);
    if ("true".equals(result.getAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "nil"))) {
      return Optional.empty();
    }
    try {
      return Optional.of(jaxbContext.createUnmarshaller().unmarshal(result, BurLocalUnitDto.class).getValue());
    } catch (JAXBException e) {
      throw new IllegalStateException("Failed to unmarshal BUR result", e);
    }
  }

  private Document parse(String xml) {
    try {
      return documentBuilderFactory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    } catch (ParserConfigurationException | SAXException | IOException e) {
      throw new IllegalStateException("Failed to parse SOAP response envelope", e);
    }
  }

  private static DocumentBuilderFactory createHardenedDocumentBuilderFactory() {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      factory.setExpandEntityReferences(false);
      return factory;
    } catch (ParserConfigurationException e) {
      throw new IllegalStateException("Failed to configure hardened XML parser for SOAP responses", e);
    }
  }
}
