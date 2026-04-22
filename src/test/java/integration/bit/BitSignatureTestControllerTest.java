package integration.bit;

import static integration.testutils.TestUserEnum.ADMIN;

import integration.testutils.AuthTestUtils;
import io.quarkus.test.junit.QuarkusTest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class BitSignatureTestControllerTest {

  private static final String SIGN_PATH = "/api/bit/v1/test/sign";
  private static final String ADMIN_GLOBAL_ID = "test-admin-global-id";

  @Test
  void givenValidPdf_whenTestSign_thenReturn200() throws IOException {
    byte[] pdf = createMinimalPdf();

    AuthTestUtils.requestAs(ADMIN)
        .queryParam("adminGlobalId", ADMIN_GLOBAL_ID)
        .multiPart("file", "test.pdf", pdf, "application/pdf")
        .when().post(SIGN_PATH)
        .then().statusCode(200);
  }

  private byte[] createMinimalPdf() throws IOException {
    try (var doc = new PDDocument(); var baos = new ByteArrayOutputStream()) {
      doc.addPage(new PDPage());
      doc.save(baos);
      return baos.toByteArray();
    }
  }
}
