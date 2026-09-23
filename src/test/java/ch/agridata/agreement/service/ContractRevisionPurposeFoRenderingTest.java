package ch.agridata.agreement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.agridata.agreement.dto.ContractRevisionPdfDto;
import ch.agridata.agreement.dto.ContractRevisionPdfPurposeDto;
import ch.agridata.common.exceptions.RichTextParseException;
import ch.agridata.common.utils.RichTextHtmlParser;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.util.JAXBSource;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.junit.jupiter.api.Test;

/**
 * Verifies that the rich-text (HTML) purpose is transformed by the contract-revision stylesheet into
 * formatted XSL-FO (paragraphs, bold, italic, bulleted and numbered lists) instead of escaped source
 * text. Runs the real stylesheet against a JAXB-marshalled DTO and inspects the FO output, so it needs
 * neither FOP nor a database.
 *
 * @CommentLastReviewed 2026-09-22
 */
class ContractRevisionPurposeFoRenderingTest {

  private static final String PURPOSE_HTML =
      "<p>Intro <strong>fett</strong> und <em>kursiv</em> und <u>unterstrichen</u></p>"
          + "<ul><li><p>Aufzaehlungspunkt</p></li></ul>"
          + "<ol><li><p>Erster</p></li><li><p>Zweiter</p></li></ol>";

  @Test
  void givenHtmlPurpose_whenTransformed_thenRendersFormattedFo() throws Exception {
    String fo = transformToFo(PURPOSE_HTML);

    // Paragraph text and inline formatting.
    assertThat(fo).contains("Intro")
        .contains("font-weight=\"bold\"")
        .contains("font-style=\"italic\"")
        .contains("text-decoration=\"underline\"")
        .contains("fett")
        .contains("kursiv")
        .contains("unterstrichen")
        // List item texts are rendered as list-item bodies, not as escaped markup.
        .contains("Aufzaehlungspunkt")
        .contains("Erster")
        .contains("Zweiter")
        // The markup must not leak into the output as escaped (or raw) source text.
        .doesNotContain("&lt;")
        .doesNotContain("<p>Intro")
        .doesNotContain("<ul>")
        .doesNotContain("<ol>");
  }

  @Test
  void givenPlainTextPurpose_whenParsed_thenFailsFast() {
    assertThatThrownBy(() -> RichTextHtmlParser.toElement("de", "Nur einfacher Text"))
        .isInstanceOf(RichTextParseException.class);
  }

  private static String transformToFo(String purposeHtml) throws Exception {
    ContractRevisionPdfDto dto = ContractRevisionPdfDto.builder()
        .requestPurpose(new ContractRevisionPdfPurposeDto(List.of(
            RichTextHtmlParser.toElement("de", purposeHtml),
            RichTextHtmlParser.toElement("fr", purposeHtml),
            RichTextHtmlParser.toElement("it", purposeHtml))))
        .build();

    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    transformerFactory.setURIResolver(classpathResolver());

    try (InputStream xsltIn = ContractRevisionPurposeFoRenderingTest.class.getClassLoader()
        .getResourceAsStream("pdf/contractRevision.fo.xsl")) {
      Transformer transformer = transformerFactory.newTransformer(new StreamSource(xsltIn));

      Source xmlSource = new JAXBSource(JAXBContext.newInstance(ContractRevisionPdfDto.class), dto);
      StringWriter writer = new StringWriter();
      transformer.transform(xmlSource, new StreamResult(writer));

      return writer.toString();
    }
  }

  private static URIResolver classpathResolver() {
    return (href, base) -> new StreamSource(
        ContractRevisionPurposeFoRenderingTest.class.getClassLoader().getResourceAsStream("pdf/" + href));
  }
}
