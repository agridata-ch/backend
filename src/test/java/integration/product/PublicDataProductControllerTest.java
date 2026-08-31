package integration.product;

import static integration.testutils.TestDataIdentifiers.DataSourceSystem.UUID_5335D715;
import static integration.testutils.TestDataIdentifiers.RestClient.UUID_B1398C9D;
import static integration.testutils.TestUserEnum.PROVIDER_1;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import ch.agridata.aws.api.PdfStorageApi;
import ch.agridata.common.dto.LinkDto;
import ch.agridata.product.controller.DataProductControllerV2;
import ch.agridata.product.controller.PublicDataProductController;
import ch.agridata.product.dto.DataProductDescriptionDto;
import ch.agridata.product.dto.DataProductDocumentMetadataDto;
import ch.agridata.product.dto.DataProductDto;
import ch.agridata.product.dto.DataProductExtendedDescriptionDto;
import ch.agridata.product.dto.DataProductNameDto;
import ch.agridata.product.dto.DataProductStateEnum;
import ch.agridata.product.dto.DataProductUpdateDto;
import ch.agridata.product.dto.FlowCodeEnum;
import ch.agridata.product.dto.RestClientMethodCodeEnum;
import ch.agridata.product.persistence.DataProductDocumentRepository;
import ch.agridata.product.persistence.DocumentScanStatusEnum;
import ch.agridata.product.service.DataProductDocumentScanService;
import com.fasterxml.jackson.databind.ObjectMapper;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestUserEnum;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

/**
 * Verifies the public data product endpoints. The paginated list exposes only ACTIVE products, and the documents endpoint
 * exposes only documents of an ACTIVE product whose virus scan succeeded (scan status AVAILABLE).
 *
 * @CommentLastReviewed 2026-08-06
 */
@QuarkusTest
class PublicDataProductControllerTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final byte[] SAMPLE_PDF = "%PDF-1.4\npublic-documents-test\n".getBytes(StandardCharsets.UTF_8);

  @InjectMock
  PdfStorageApi pdfStorageApi;

  @InjectMock
  DataProductDocumentScanService dataProductDocumentScanService;

  @Inject
  DataProductDocumentRepository documentRepository;

  @Test
  void givenUnauthenticatedRequest_whenGetPublicProducts_thenReturnsOnlyActiveProducts() {
    createActiveDataProduct(PROVIDER_1);

    given()
        .when()
        .get(PublicDataProductController.PATH)
        .then()
        .statusCode(200)
        .body("items.size()", greaterThan(0))
        .body("totalItems", greaterThan(0))
        .body("currentPage", equalTo(0))
        .body("items.stateCode", everyItem(equalTo("ACTIVE")));
  }

  @Test
  void givenActiveProduct_whenSearchByUniqueName_thenProductIsReturned() {
    String uniqueName = "PublicListActive-" + UUID.randomUUID();
    createActiveDataProduct(PROVIDER_1, uniqueName);

    given()
        .queryParam("searchTerm", uniqueName)
        .when()
        .get(PublicDataProductController.PATH)
        .then()
        .statusCode(200)
        .body("totalItems", equalTo(1))
        .body("items[0].name.de", equalTo(uniqueName))
        .body("items[0].stateCode", equalTo("ACTIVE"))
        .body("items[0].flowCode", equalTo(FlowCodeEnum.UNBOUND_BUR_BASED_POST_VALIDATION.name()))
        .body("items[0].restClient", nullValue())
        .body("items[0].restClientPathTemplate", nullValue())
        .body("items[0].restClientRequestTemplate", nullValue())
        .body("items[0].restClientChangeDetectionPathTemplate", nullValue())
        .body("items[0].restClientMethodCode", nullValue());
  }

  @Test
  void givenDraftProduct_whenSearchByUniqueName_thenProductIsNotReturned() {
    String uniqueName = "PublicListDraft-" + UUID.randomUUID();
    createNamedDraft(PROVIDER_1, uniqueName);

    given()
        .queryParam("searchTerm", uniqueName)
        .when()
        .get(PublicDataProductController.PATH)
        .then()
        .statusCode(200)
        .body("totalItems", equalTo(0));
  }

  @Test
  void givenActiveProduct_whenGetPublicProduct_thenReturnsProduct() {
    String uniqueName = "PublicSingleActive-" + UUID.randomUUID();
    UUID productId = createActiveDataProduct(PROVIDER_1, uniqueName);

    given()
        .when()
        .get(PublicDataProductController.PATH + "/" + productId)
        .then()
        .statusCode(200)
        .body("id", equalTo(productId.toString()))
        .body("stateCode", equalTo("ACTIVE"))
        .body("name.de", equalTo(uniqueName))
        .body("consentRequired", notNullValue());
  }

  @Test
  void givenDraftProduct_whenGetPublicProduct_thenNotFound() {
    UUID productId = createNamedDraft(PROVIDER_1, "PublicSingleDraft-" + UUID.randomUUID());

    given()
        .when()
        .get(PublicDataProductController.PATH + "/" + productId)
        .then()
        .statusCode(404);
  }

  @Test
  void givenUnknownProduct_whenGetPublicProduct_thenNotFound() {
    given()
        .when()
        .get(PublicDataProductController.PATH + "/" + UUID.randomUUID())
        .then()
        .statusCode(404);
  }

  @Test
  void givenPageSizeOfOne_whenGetPublicProducts_thenRespectsPageSize() {
    createActiveDataProduct(PROVIDER_1);
    createActiveDataProduct(PROVIDER_1);

    given()
        .queryParam("size", 1)
        .when()
        .get(PublicDataProductController.PATH)
        .then()
        .statusCode(200)
        .body("pageSize", equalTo(1))
        .body("items.size()", equalTo(1))
        .body("totalItems", greaterThanOrEqualTo(2))
        .body("totalPages", greaterThanOrEqualTo(2));
  }

  @Test
  void givenInvalidSortField_whenGetPublicProducts_thenBadRequest() {
    given()
        .queryParam("sortBy", "invalidField")
        .when()
        .get(PublicDataProductController.PATH)
        .then()
        .statusCode(400);
  }

  @Test
  void givenActiveProductWithMixedScanStatuses_whenGetPublicDocuments_thenReturnsOnlyAvailable() {
    UUID productId = createActiveDataProduct(PROVIDER_1);

    DataProductDocumentMetadataDto available = uploadDocument(PROVIDER_1, productId, "available.pdf");
    forceScanStatus(available.id(), DocumentScanStatusEnum.AVAILABLE);
    forceScanStatus(uploadDocument(PROVIDER_1, productId, "pending.pdf").id(), DocumentScanStatusEnum.PENDING_SCAN);
    forceScanStatus(uploadDocument(PROVIDER_1, productId, "rejected.pdf").id(), DocumentScanStatusEnum.REJECTED);
    forceScanStatus(uploadDocument(PROVIDER_1, productId, "failed.pdf").id(), DocumentScanStatusEnum.SCAN_FAILED);

    given()
        .when()
        .get(PublicDataProductController.PATH + "/" + productId + "/documents")
        .then()
        .statusCode(200)
        .body("size()", equalTo(1))
        .body("[0].id", equalTo(available.id().toString()))
        .body("[0].fileName", equalTo("available.pdf"))
        .body("[0].scanStatus", equalTo("AVAILABLE"));
  }

  @Test
  void givenActiveProductWithoutAvailableDocuments_whenGetPublicDocuments_thenReturnsEmptyList() {
    UUID productId = createActiveDataProduct(PROVIDER_1);
    forceScanStatus(uploadDocument(PROVIDER_1, productId, "pending.pdf").id(), DocumentScanStatusEnum.PENDING_SCAN);

    given()
        .when()
        .get(PublicDataProductController.PATH + "/" + productId + "/documents")
        .then()
        .statusCode(200)
        .body("size()", equalTo(0));
  }

  @SneakyThrows
  @Test
  void givenNonActiveProductWithAvailableDocument_whenGetPublicDocuments_thenNotFound() {
    UUID productId = createDraft(PROVIDER_1, DataProductUpdateDto.builder().build());
    forceScanStatus(uploadDocument(PROVIDER_1, productId, "available.pdf").id(), DocumentScanStatusEnum.AVAILABLE);

    given()
        .when()
        .get(PublicDataProductController.PATH + "/" + productId + "/documents")
        .then()
        .statusCode(404);
  }

  @Test
  void givenUnknownProduct_whenGetPublicDocuments_thenNotFound() {
    given()
        .when()
        .get(PublicDataProductController.PATH + "/" + UUID.randomUUID() + "/documents")
        .then()
        .statusCode(404);
  }

  private UUID createActiveDataProduct(TestUserEnum user) {
    return createActiveDataProduct(user, "Product Name");
  }

  private UUID createActiveDataProduct(TestUserEnum user, String name) {
    UUID id = createNamedDraft(user, name);
    activateDataProduct(user, id);
    return id;
  }

  private UUID createNamedDraft(TestUserEnum user, String name) {
    return createDraft(user, getDataProductUpdateDto(UUID_5335D715.uuid(), UUID_B1398C9D.uuid(), name));
  }

  @SneakyThrows
  private UUID createDraft(TestUserEnum user, DataProductUpdateDto dto) {
    return AuthTestUtils.requestAs(user)
        .given()
        .contentType(ContentType.JSON)
        .body(MAPPER.writeValueAsString(dto))
        .when()
        .post(DataProductControllerV2.PATH)
        .then()
        .statusCode(201)
        .extract().as(DataProductDto.class)
        .id();
  }

  @SneakyThrows
  private void activateDataProduct(TestUserEnum user, UUID id) {
    AuthTestUtils.requestAs(user)
        .given()
        .contentType(ContentType.JSON)
        .body(MAPPER.writeValueAsString(DataProductStateEnum.ACTIVE))
        .when()
        .put(DataProductControllerV2.PATH + "/" + id + "/status")
        .then()
        .statusCode(200);
  }

  private DataProductDocumentMetadataDto uploadDocument(TestUserEnum user, UUID productId, String fileName) {
    return AuthTestUtils.requestAs(user)
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .multiPart("document", fileName, SAMPLE_PDF, "application/pdf")
        .when()
        .post(DataProductControllerV2.PATH + "/" + productId + "/documents")
        .then()
        .statusCode(201)
        .extract().as(DataProductDocumentMetadataDto.class);
  }

  private void forceScanStatus(UUID documentId, DocumentScanStatusEnum status) {
    QuarkusTransaction.requiringNew()
        .run(() -> documentRepository.update("scanStatus = ?1 where id = ?2", status, documentId));
  }

  private static DataProductUpdateDto getDataProductUpdateDto(UUID dataSourceSystemId, UUID restClientId, String name) {
    return DataProductUpdateDto.builder()
        .dataSourceSystemId(dataSourceSystemId)
        .name(new DataProductNameDto(name, name, name))
        .description(new DataProductDescriptionDto("Beschreibung Deutsch", "Desciption Francais", "Descriptione Italiano"))
        .links(List.of(new LinkDto("https://example1.com", "Example Link 1"), new LinkDto("https://example2.com", "Example Link 2")))
        .extendedDescription(new DataProductExtendedDescriptionDto("", "", "Descrizione tecnica italiano"))
        .restClientId(restClientId)
        .flowCode(FlowCodeEnum.UNBOUND_BUR_BASED_POST_VALIDATION)
        .restClientPathTemplate("path/template")
        .restClientChangeDetectionPathTemplate("change/detection/path/template")
        .restClientMethodCode(RestClientMethodCodeEnum.GET)
        .restClientRequestTemplate("{\"someKey\":\"someValue\"}")
        .build();
  }
}
