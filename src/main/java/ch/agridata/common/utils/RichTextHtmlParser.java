package ch.agridata.common.utils;

import ch.agridata.common.exceptions.RichTextParseException;
import java.io.StringReader;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import lombok.experimental.UtilityClass;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Parses stored rich-text (HTML) fragments into DOM elements so they can be marshalled verbatim into
 * an XML source tree instead of being emitted as escaped text. The rich text originates from the
 * front-end editor and uses a small, well-formed XHTML subset (p, strong, em, u, ul, ol, li). Content
 * that is not well-formed XML, or that is bare text not wrapped in markup, is rejected with a
 * {@link RichTextParseException} rather than silently rendered as-is, so non-HTML data fails fast
 * instead of leaking raw text into the PDF.
 *
 * <p>The named HTML entity {@code &nbsp;} (the only one the front-end editor emits, produced when a
 * {@code U+00A0} non-breaking space is serialized) is not defined in XML, so it is rewritten to its
 * numeric character reference {@code &#160;} before parsing.
 *
 * @CommentLastReviewed 2026-09-25
 */
@UtilityClass
public class RichTextHtmlParser {

  /**
   * Wraps the given HTML fragment in an element named {@code tagName} and returns it as a DOM element
   * whose children are the parsed rich-text nodes. A {@code null} or blank fragment yields an empty
   * element.
   *
   * @param tagName the name of the wrapping element (e.g. the language code {@code de}/{@code fr}/{@code it})
   * @param html    the HTML fragment to parse; may be {@code null}
   * @return the wrapping element with the parsed content
   * @throws RichTextParseException if the fragment is not well-formed XHTML or is bare (non-markup) text
   */
  public static Element toElement(String tagName, String html) {
    String content = html == null ? "" : html.replace("&nbsp;", "&#160;");
    DocumentBuilder builder = newSecureBuilder();

    Element element;
    try {
      String wrapped = "<" + tagName + ">" + content + "</" + tagName + ">";
      element = builder.parse(new InputSource(new StringReader(wrapped))).getDocumentElement();
    } catch (Exception e) {
      throw new RichTextParseException(
          "Rich-text value for '" + tagName + "' is not well-formed XHTML: " + content, e);
    }

    if (hasBareText(element)) {
      throw new RichTextParseException(
          "Rich-text value for '" + tagName + "' is not HTML markup: " + content);
    }
    return element;
  }

  /**
   * Returns {@code true} if the element contains non-whitespace text directly (i.e. text not wrapped in
   * a block element). The front-end editor always wraps content in block elements, so bare top-level
   * text signals a legacy plain-text value.
   */
  private static boolean hasBareText(Element element) {
    NodeList children = element.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if (child.getNodeType() == Node.TEXT_NODE && !child.getTextContent().isBlank()) {
        return true;
      }
    }
    return false;
  }

  private static DocumentBuilder newSecureBuilder() {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(false);
      factory.setExpandEntityReferences(false);
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      return factory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      throw new IllegalStateException("Failed to configure secure XML parser for rich text", e);
    }
  }
}
