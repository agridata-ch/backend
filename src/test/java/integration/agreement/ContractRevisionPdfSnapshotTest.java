package integration.agreement;

import static integration.agreement.DataRequestTestFactory.createReadyForSigningByConsumerDataRequestFor;
import static org.assertj.core.api.Assertions.assertThat;

import integration.testutils.AuthTestUtils;
import integration.testutils.TestUserEnum;
import io.quarkus.test.junit.QuarkusTest;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.junit.jupiter.api.Test;

/**
 * Visual snapshot test for {@link ch.agridata.agreement.service.ContractRevisionPdfService}.
 * Renders each PDF page and compares it pixel-for-pixel against a stored reference.
 * On failure, diff images (red pixels = changed area) are written to {@code target/contract-revision-pdf-snapshot-test/}.
 *
 * <p>To create or update the reference snapshot, run with {@code -DupdatePdfSnapshot=true}.
 *
 * @CommentLastReviewed 2026-04-20
 */
@QuarkusTest
class ContractRevisionPdfSnapshotTest {

  private static final String PDF_PATH = ch.agridata.agreement.controller.ContractRevisionController.PATH + "/{id}/pdf";
  private static final Path SNAPSHOT_PATH = Path.of("src/test/resources/pdf/contract-revision-snapshot.pdf");
  private static final Path DIFF_DIR = Path.of("target/contract-revision-pdf-snapshot-test/diffs");

  @Test
  void givenExistingRevision_whenGetPdf_thenMatchesSnapshot() throws Exception {
    UUID revisionId = createReadyForSigningByConsumerDataRequestFor(TestUserEnum.CONSUMER_BLV_1).currentContractRevisionId();
    byte[] pdfBytes = AuthTestUtils.requestAs(TestUserEnum.CONSUMER_BLV_1)
        .pathParam("id", revisionId)
        .when().get(PDF_PATH)
        .then()
        .statusCode(200)
        .extract().asByteArray();

    if (Boolean.getBoolean("updatePdfSnapshot")) {
      Files.createDirectories(SNAPSHOT_PATH.getParent());
      Files.write(SNAPSHOT_PATH, pdfBytes);
      return;
    }

    byte[] snapshotBytes = Files.readAllBytes(SNAPSHOT_PATH);
    List<String> diffs = compareRenderedPages(snapshotBytes, pdfBytes);
    assertThat(diffs)
        .as("PDF differs from snapshot at %s\nRun with -DupdatePdfSnapshot=true to update.", SNAPSHOT_PATH)
        .isEmpty();
  }

  private static List<String> compareRenderedPages(byte[] expected, byte[] actual) throws Exception {
    List<String> diffs = new ArrayList<>();
    try (PDDocument expectedDoc = Loader.loadPDF(expected);
         PDDocument actualDoc = Loader.loadPDF(actual)) {

      int expectedPages = expectedDoc.getNumberOfPages();
      int actualPages = actualDoc.getNumberOfPages();
      if (expectedPages != actualPages) {
        diffs.add(String.format("Page count differs: expected %d, actual %d", expectedPages, actualPages));
        return diffs;
      }

      PDFRenderer expectedRenderer = new PDFRenderer(expectedDoc);
      PDFRenderer actualRenderer = new PDFRenderer(actualDoc);

      for (int i = 0; i < expectedPages; i++) {
        BufferedImage expectedImg = expectedRenderer.renderImageWithDPI(i, 96);
        BufferedImage actualImg = actualRenderer.renderImageWithDPI(i, 96);
        int diffCount = writeDiffImageIfChanged(i + 1, expectedImg, actualImg);
        if (diffCount > 0) {
          diffs.add(String.format("Page %d: %d pixels differ – see %s/page-%d-diff.png", i + 1, diffCount, DIFF_DIR, i + 1));
        }
      }
    }
    return diffs;
  }

  private static int writeDiffImageIfChanged(int pageNum, BufferedImage expected, BufferedImage actual) throws IOException {
    int width = Math.max(expected.getWidth(), actual.getWidth());
    int height = Math.max(expected.getHeight(), actual.getHeight());
    BufferedImage diff = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    int diffCount = 0;

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int expRgb = (x < expected.getWidth() && y < expected.getHeight()) ? expected.getRGB(x, y) : 0xFFFFFF;
        int actRgb = (x < actual.getWidth() && y < actual.getHeight()) ? actual.getRGB(x, y) : 0xFFFFFF;
        if (expRgb != actRgb) {
          diff.setRGB(x, y, 0xFFFF0000); // red
          diffCount++;
        } else {
          diff.setRGB(x, y, actRgb);
        }
      }
    }

    if (diffCount > 0) {
      Files.createDirectories(DIFF_DIR);
      ImageIO.write(diff, "PNG", DIFF_DIR.resolve("page-" + pageNum + "-diff.png").toFile());
    }

    return diffCount;
  }
}
