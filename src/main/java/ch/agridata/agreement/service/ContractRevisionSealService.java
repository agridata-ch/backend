package ch.agridata.agreement.service;

import ch.agridata.bit.api.BitSignatureApi;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

/**
 * Seals a contract revision PDF using the BIT evidence Signing API.
 *
 * @CommentLastReviewed 2026-04-09
 */
@ApplicationScoped
@RequiredArgsConstructor
public class ContractRevisionSealService {

  private final BitSignatureApi bitSignatureApi;

  public byte[] seal(UUID contractRevisionId, String adminGlobalId) {
    // TODO: Replace the stub with the actual contract revision PDF.
    byte[] pdf = createDummyPdf(contractRevisionId);
    return bitSignatureApi.sign(pdf, adminGlobalId);
  }

  private byte[] createDummyPdf(UUID contractRevisionId) {
    try (var doc = new PDDocument();
         var baos = new ByteArrayOutputStream()) {
      var page = new PDPage();
      doc.addPage(page);
      try (var cs = new PDPageContentStream(doc, page)) {
        cs.beginText();
        cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12f);
        cs.newLineAtOffset(100f, 700f);
        cs.showText("Dummy PDF for Contract Revision " + contractRevisionId);
        cs.endText();
      }
      doc.save(baos);
      return baos.toByteArray();
    } catch (IOException e) {
      throw new IllegalStateException("Failed to create dummy PDF", e);
    }
  }
}
