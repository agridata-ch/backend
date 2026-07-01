package integration.product;

import static integration.testutils.TestUserEnum.PROVIDER_1;

import ch.agridata.aws.api.PdfStorageApi;
import ch.agridata.product.controller.DataProductControllerV2;
import ch.agridata.product.dto.DataProductDto;
import ch.agridata.product.dto.DataProductUpdateDto;
import ch.agridata.product.service.DataProductDocumentScanService;
import com.fasterxml.jackson.databind.ObjectMapper;
import integration.testutils.AuthTestUtils;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.MediaType;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

/**
 * Verifies the upload size limit for data product documents. A deliberately tiny
 * {@code agridata.product.documents.max-size-bytes} is configured via a dedicated profile so the limit can be
 * exercised without transferring a payload large enough to also trip Quarkus' HTTP body-size limit. PdfStorageApi is
 * mocked so no real S3 interaction occurs for the within-limit upload.
 */
@QuarkusTest
@TestProfile(DataProductDocumentUploadSizeLimitTest.SmallUploadLimitProfile.class)
class DataProductDocumentUploadSizeLimitTest {

  private static final long MAX_SIZE_BYTES = 64;
  private static final ObjectMapper MAPPER = new ObjectMapper();

  @InjectMock
  PdfStorageApi pdfStorageApi;

  // Mocked so a successful upload does not spawn a long-lived background scan-poll thread that outlives the test.
  @InjectMock
  DataProductDocumentScanService dataProductDocumentScanService;

  public static class SmallUploadLimitProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
      return Map.of("agridata.product.documents.max-size-bytes", Long.toString(MAX_SIZE_BYTES));
    }
  }

  @Test
  void givenDocumentExceedingSizeLimit_whenUpload_thenBadRequest() {
    UUID productId = createDraft();

    AuthTestUtils.requestAs(PROVIDER_1)
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .multiPart("document", "big.pdf", oversizedPdf(), "application/pdf")
        .when()
        .post(DataProductControllerV2.PATH + "/" + productId + "/documents")
        .then()
        .statusCode(400);
  }

  @Test
  void givenDocumentWithinSizeLimit_whenUpload_thenCreated() {
    UUID productId = createDraft();
    byte[] withinLimit = "%PDF-1.4\n".getBytes(StandardCharsets.UTF_8);

    AuthTestUtils.requestAs(PROVIDER_1)
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .multiPart("document", "small.pdf", withinLimit, "application/pdf")
        .when()
        .post(DataProductControllerV2.PATH + "/" + productId + "/documents")
        .then()
        .statusCode(201);
  }

  @SneakyThrows
  private UUID createDraft() {
    return AuthTestUtils.requestAs(PROVIDER_1)
        .given()
        .contentType(ContentType.JSON)
        .body(MAPPER.writeValueAsString(DataProductUpdateDto.builder().build()))
        .when()
        .post(DataProductControllerV2.PATH)
        .then()
        .statusCode(201)
        .extract().as(DataProductDto.class)
        .id();
  }

  // A valid PDF header padded past the configured limit, so only the size gate (checked before the
  // PDF-signature and document-count checks) can reject it.
  private static byte[] oversizedPdf() {
    byte[] payload = new byte[(int) MAX_SIZE_BYTES + 1];
    byte[] header = "%PDF-1.4\n".getBytes(StandardCharsets.UTF_8);
    System.arraycopy(header, 0, payload, 0, header.length);
    return payload;
  }
}
