package ch.agridata.agreement.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

/**
 * Verifies that the {@link FopFactory} produced by {@link ContractRevisionFopConfig} can render a
 * PDF/A-2b document, exercising the classpath resolution of the ICC output profile and the embedded
 * fonts declared in {@code pdf/fop.xconf}. This is the exact path that failed in the packaged
 * (fast-jar) deployment while working locally.
 *
 * @CommentLastReviewed 2026-07-15
 */
class ContractRevisionFopConfigTest {

  private static final String MINIMAL_FO = """
      <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
        <fo:layout-master-set>
          <fo:simple-page-master master-name="page" page-width="210mm" page-height="297mm">
            <fo:region-body/>
          </fo:simple-page-master>
        </fo:layout-master-set>
        <fo:page-sequence master-reference="page">
          <fo:flow flow-name="xsl-region-body">
            <fo:block font-family="LiberationSans">PDF/A rendering works</fo:block>
          </fo:flow>
        </fo:page-sequence>
      </fo:root>
      """;

  @Test
  void fopFactory_rendersPdfWithEmbeddedIccAndFonts() throws Exception {
    FopFactory fopFactory = new ContractRevisionFopConfig().fopFactory();
    Document foDocument = parseFo();

    byte[] pdf;
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);

      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      Source source = new DOMSource(foDocument);
      Result result = new SAXResult(fop.getDefaultHandler());

      transformer.transform(source, result);
      pdf = out.toByteArray();
    }

    assertThat(pdf).isNotEmpty();
    assertThat(new String(pdf, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
  }

  private static Document parseFo() throws Exception {
    var documentBuilderFactory = DocumentBuilderFactory.newInstance();
    documentBuilderFactory.setNamespaceAware(true);

    return documentBuilderFactory.newDocumentBuilder()
        .parse(new ByteArrayInputStream(MINIMAL_FO.getBytes(StandardCharsets.UTF_8)));
  }
}
