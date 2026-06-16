package ch.agridata.uidregister.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Builds and parses SOAP 1.1 envelopes for the UID-Register client without relying on a full SOAP stack.
 * The request body is JAXB-marshalled and wrapped into a minimal envelope; the response envelope
 * is parsed with a hardened DOM parser, SOAP faults are detected, and the body payload is JAXB-unmarshalled.
 *
 * <p>The {@link JAXBContext} spans every generated model package (UID-WSE and the referenced eCH schemas)
 * so that element references declared in the {@code ObjectFactory} classes resolve correctly.</p>
 *
 * @CommentLastReviewed 2026-06-16
 */
@ApplicationScoped
public class SoapEnvelopeSupport {

  private static final String SOAP_ENVELOPE_NS = "http://schemas.xmlsoap.org/soap/envelope/";

  private static final String ENVELOPE_PREFIX =
      "<soapenv:Envelope xmlns:soapenv=\"" + SOAP_ENVELOPE_NS + "\"><soapenv:Body>";
  private static final String ENVELOPE_SUFFIX = "</soapenv:Body></soapenv:Envelope>";

  private static final String CONTEXT_PATH = String.join(":",
      "ch.admin.uid.xmlns.uid_wse",
      "ch.admin.uid.xmlns.uid_wse._5",
      "ch.admin.uid.xmlns.uid_wse_shared._2",
      "ch.ech.xmlns.ech_0007._6",
      "ch.ech.xmlns.ech_0010._7",
      "ch.ech.xmlns.ech_0044._4",
      "ch.ech.xmlns.ech_0046._5",
      "ch.ech.xmlns.ech_0097._5",
      "ch.ech.xmlns.ech_0098._5",
      "ch.ech.xmlns.ech_0108._5",
      "ch.ech.xmlns.ech_0116._4");

  private final JAXBContext jaxbContext;
  private final DocumentBuilderFactory documentBuilderFactory;

  public SoapEnvelopeSupport() {
    try {
      this.jaxbContext = JAXBContext.newInstance(CONTEXT_PATH);
    } catch (JAXBException e) {
      throw new IllegalStateException("Failed to initialize JAXB context for UID-Register model", e);
    }
    this.documentBuilderFactory = createHardenedDocumentBuilderFactory();
  }

  /**
   * Marshals the given JAXB request element and wraps it in a SOAP 1.1 envelope body.
   *
   * @param requestBody a JAXB root element (e.g. {@code GetByUID})
   * @return the serialized SOAP envelope
   */
  public String wrap(Object requestBody) {
    try {
      var marshaller = jaxbContext.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
      var writer = new StringWriter();
      marshaller.marshal(requestBody, writer);
      return ENVELOPE_PREFIX + writer + ENVELOPE_SUFFIX;
    } catch (JAXBException e) {
      throw new IllegalStateException("Failed to marshal SOAP request body", e);
    }
  }

  /**
   * Parses a SOAP response envelope and unmarshals its body payload to the requested type.
   *
   * @param responseXml the raw SOAP response
   * @param type        the expected body payload type (e.g. {@code GetByUIDResponse})
   * @return the unmarshalled body payload
   * @throws SoapFaultException if the response is a SOAP fault envelope
   */
  public <T> T unwrapBody(String responseXml, Class<T> type) {
    var bodyChild = extractBodyChild(responseXml);
    try {
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      JAXBElement<T> element = unmarshaller.unmarshal(bodyChild, type);
      return element.getValue();
    } catch (JAXBException e) {
      throw new IllegalStateException("Failed to unmarshal SOAP response body to " + type.getSimpleName(), e);
    }
  }

  private Element extractBodyChild(String responseXml) {
    Document document = parse(responseXml);

    NodeList faults = document.getElementsByTagNameNS(SOAP_ENVELOPE_NS, "Fault");
    if (faults.getLength() > 0) {
      throw new SoapFaultException(extractFaultString((Element) faults.item(0)));
    }

    NodeList bodies = document.getElementsByTagNameNS(SOAP_ENVELOPE_NS, "Body");
    if (bodies.getLength() == 0) {
      throw new IllegalStateException("SOAP response did not contain a Body element");
    }
    NodeList children = bodies.item(0).getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if (child.getNodeType() == Node.ELEMENT_NODE) {
        return (Element) child;
      }
    }
    throw new IllegalStateException("SOAP response Body did not contain a payload element");
  }

  private static String extractFaultString(Element fault) {
    // SOAP 1.1 fault: faultstring is unqualified inside the Fault element.
    NodeList faultStrings = fault.getElementsByTagName("faultstring");
    if (faultStrings.getLength() > 0) {
      return faultStrings.item(0).getTextContent();
    }
    return fault.getTextContent();
  }

  private Document parse(String xml) {
    try {
      return documentBuilderFactory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    } catch (ParserConfigurationException | org.xml.sax.SAXException | java.io.IOException e) {
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
