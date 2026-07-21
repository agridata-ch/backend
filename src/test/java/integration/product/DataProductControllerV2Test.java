package integration.product;

import static integration.testutils.TestDataIdentifiers.DataSourceSystem.UUID_4CCBfA06;
import static integration.testutils.TestDataIdentifiers.DataSourceSystem.UUID_5335D715;
import static integration.testutils.TestDataIdentifiers.RestClient.UUID_1C438FA1;
import static integration.testutils.TestDataIdentifiers.RestClient.UUID_5D3A4A87;
import static integration.testutils.TestDataIdentifiers.RestClient.UUID_B1398C9D;
import static integration.testutils.TestUserEnum.ADMIN;
import static integration.testutils.TestUserEnum.CONSUMER_BIO_SUISSE;
import static integration.testutils.TestUserEnum.PROVIDER_1;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import ch.agridata.aws.api.PdfStorageApi;
import ch.agridata.common.dto.LinkDto;
import ch.agridata.product.controller.DataProductControllerV2;
import ch.agridata.product.dto.DataProductDescriptionDto;
import ch.agridata.product.dto.DataProductDocumentMetadataDto;
import ch.agridata.product.dto.DataProductDto;
import ch.agridata.product.dto.DataProductExtendedDescriptionDto;
import ch.agridata.product.dto.DataProductNameDto;
import ch.agridata.product.dto.DataProductStateEnum;
import ch.agridata.product.dto.DataProductUpdateDto;
import ch.agridata.product.dto.DocumentScanStatusEnum;
import ch.agridata.product.dto.FlowCodeEnum;
import ch.agridata.product.dto.RestClientMethodCodeEnum;
import ch.agridata.product.persistence.DataProductDocumentRepository;
import ch.agridata.product.service.DataProductDocumentScanService;
import com.fasterxml.jackson.databind.ObjectMapper;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestDataIdentifiers;
import integration.testutils.TestUserEnum;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import lombok.SneakyThrows;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@QuarkusTest
class DataProductControllerV2Test {
  @ConfigProperty(name = "quarkus.rest-client.agis-api.url")
  String agisApiUrl;

  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final byte[] SAMPLE_PDF = "%PDF-1.4\ndocument-actions-test\n".getBytes(StandardCharsets.UTF_8);

  // The GuardDutyScanSimulator is local-profile only and scans on a 3s schedule, so it is unsuitable for tests.
  // PdfStorageApi is mocked to stand in for S3 (upload/download). DataProductDocumentScanService is mocked so an upload
  // does not spawn a long-lived background scan-poll thread; such threads outlive the request context and later fail to
  // resolve request-scoped/config beans, spamming warnings. The document scan status is instead set directly via the
  // repository with forceScanStatus.
  @InjectMock
  PdfStorageApi pdfStorageApi;

  @InjectMock
  DataProductDocumentScanService dataProductDocumentScanService;

  @Inject
  DataProductDocumentRepository documentRepository;

  @Test
  void givenAdminUser_whenGetPaginatedProducts_thenReturnsPagedResults() {
    RequestSpecification admin = AuthTestUtils.requestAs(ADMIN);

    admin.when()
        .get(DataProductControllerV2.PATH)
        .then()
        .statusCode(200)
        .body("items.size()", greaterThan(0))
        .body("totalItems", greaterThan(0))
        .body("totalPages", greaterThan(0))
        .body("currentPage", equalTo(0))
        .body("pageSize", greaterThanOrEqualTo(1))
        .body("items[0].id?.toString().length() > 0", is(true))
        .body("items[0].name?.toString().length() > 0", is(true));
  }

  @Test
  void givenProviderUser_whenGetPaginatedProducts_thenReturnsPagedResults() {
    RequestSpecification provider = AuthTestUtils.requestAs(PROVIDER_1);

    provider.when()
        .get(DataProductControllerV2.PATH)
        .then()
        .statusCode(200)
        .body("items", is(org.hamcrest.Matchers.notNullValue()))
        .body("totalItems", greaterThanOrEqualTo(0))
        .body("totalPages", greaterThanOrEqualTo(0))
        .body("currentPage", equalTo(0));
  }

  @Test
  void givenConsumerUser_whenGetPaginatedProducts_thenReturnsForbidden() {
    RequestSpecification consumer = AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE);

    consumer.when()
        .get(DataProductControllerV2.PATH)
        .then()
        .statusCode(403);
  }

  @Test
  void givenAdminUser_whenGetPaginatedProductsWithPageSize_thenRespectsPageSize() {
    RequestSpecification admin = AuthTestUtils.requestAs(ADMIN);

    admin.queryParam("size", 5)
        .when()
        .get(DataProductControllerV2.PATH)
        .then()
        .statusCode(200)
        .body("pageSize", equalTo(5))
        .body("items.size()", greaterThanOrEqualTo(0));
  }

  @Test
  void givenAdminUser_whenGetPaginatedProductsWithPageNumber_thenReturnsCorrectPage() {
    RequestSpecification admin = AuthTestUtils.requestAs(ADMIN);

    admin.queryParam("page", 1)
        .when()
        .get(DataProductControllerV2.PATH)
        .then()
        .statusCode(200)
        .body("currentPage", equalTo(1));
  }

  @Test
  void givenAdminUser_whenGetPaginatedProductsWithInvalidSortField_thenReturnsBadRequest() {
    RequestSpecification admin = AuthTestUtils.requestAs(ADMIN);

    admin.queryParam("sortBy", "invalidField")
        .when()
        .get(DataProductControllerV2.PATH)
        .then()
        .statusCode(400);
  }

  @Test
  void givenAdmin_whenSortByAscendingProductName_thenReturns200() {
    RequestSpecification admin = AuthTestUtils.requestAs(ADMIN);

    admin.queryParam("sortBy", "productName")
        .when()
        .get(DataProductControllerV2.PATH)
        .then()
        .statusCode(200)
        .body("items.size()", greaterThan(0))
        .body("items[0].name.de", equalTo("01 Lebensmittelsicherheit (Pflanzliche Primärproduktion)"));
  }

  @Test
  void givenAdmin_whenSortByDescendingProductName_thenReturns200() {
    RequestSpecification admin = AuthTestUtils.requestAs(ADMIN);

    admin.queryParam("sortBy", "-productName")
        .when()
        .get(DataProductControllerV2.PATH)
        .then()
        .statusCode(200)
        .body("items.size()", greaterThan(0))
        .body("items[0].name.de", equalTo("Ziegendetail Zucht"));
  }

  @Test
  void givenAdmin_whenAcceptLanguageIt_thenReturns200() {
    RequestSpecification admin = AuthTestUtils.requestAs(ADMIN);

    admin.header("Accept-Language", "it")
        .queryParam("sortBy", "productName")
        .when()
        .get(DataProductControllerV2.PATH)
        .then()
        .statusCode(200)
        .body("items[0].name.it", equalTo("01 Sicurezza alimentare (produzione primaria animale)"));
  }

  @Test
  void givenAdmin_whenSortByEmpty_thenReturns200() {
    RequestSpecification admin = AuthTestUtils.requestAs(ADMIN);

    admin.queryParam("sortBy", "")
        .when()
        .get(DataProductControllerV2.PATH)
        .then()
        .statusCode(200);
  }

  @SneakyThrows
  @ParameterizedTest
  @EnumSource(value = TestUserEnum.class, names = {"PROVIDER_1", "ADMIN"})
  void givenProviderAndFilledDataProductUpdate_whenAddNewDataProductDraft_thenReturnsDataProduct(TestUserEnum user) {
    var agisDataSourceSystemId = UUID_5335D715.uuid();
    var agisRestClientId = UUID_5D3A4A87.uuid();
    DataProductUpdateDto requestDataProductUpdate = getDataProductUpdateDto(agisDataSourceSystemId, agisRestClientId);

    DataProductDto responseDataProductDto = AuthTestUtils.requestAs(user)
        .given()
        .contentType(ContentType.JSON)
        .body(MAPPER.writeValueAsString(requestDataProductUpdate))
        .when()
        .post(DataProductControllerV2.PATH)
        .then()
        .statusCode(201)
        .extract().as(DataProductDto.class);

    assertThat(responseDataProductDto.id()).isNotNull();
    assertThat(responseDataProductDto.dataSourceSystem().id()).isEqualTo(requestDataProductUpdate.dataSourceSystemId());
    assertThat(responseDataProductDto.name().de()).isEqualTo(requestDataProductUpdate.name().de());
    assertThat(responseDataProductDto.description().de()).isEqualTo(requestDataProductUpdate.description().de());
    assertThat(responseDataProductDto.links()).isEqualTo(requestDataProductUpdate.links());
    assertThat(responseDataProductDto.extendedDescription().it()).isEqualTo(requestDataProductUpdate.extendedDescription().it());
    assertThat(responseDataProductDto.stateCode()).isEqualTo(DataProductStateEnum.DRAFT);
    assertThat(responseDataProductDto.flowCode()).isEqualTo(requestDataProductUpdate.flowCode());
    assertThat(responseDataProductDto.restClient().id()).isEqualTo(requestDataProductUpdate.restClientId());
    assertThat(responseDataProductDto.restClientMethodCode()).isEqualTo(requestDataProductUpdate.restClientMethodCode());
    assertThat(responseDataProductDto.restClientPathTemplate()).isEqualTo(requestDataProductUpdate.restClientPathTemplate());
    assertThat(responseDataProductDto.restClientRequestTemplate()).isEqualTo(requestDataProductUpdate.restClientRequestTemplate());
  }

  private static DataProductUpdateDto getDataProductUpdateDto(UUID agisDataSourceSystemId, UUID agisRestClientId) {
    return DataProductUpdateDto.builder()
        .dataSourceSystemId(agisDataSourceSystemId)
        .name(new DataProductNameDto("Name Deutsch", "Nom Francais", "Nome Italiano"))
        .description(new DataProductDescriptionDto("Beschreibung Deutsch", "Desciption Francais", "Descriptione Italiano"))
        .links(List.of(new LinkDto("https://example1.com", "Example Link 1"), new LinkDto("https://example2.com", "Example Link 2")))
        .extendedDescription(new DataProductExtendedDescriptionDto("", "", "Descrizione tecnica italiano"))
        .restClientId(agisRestClientId)
        .flowCode(FlowCodeEnum.UNBOUND_POST_VALIDATION)
        .restClientPathTemplate("path/template")
        .restClientChangeDetectionPathTemplate("change/detection/path/template")
        .restClientMethodCode(RestClientMethodCodeEnum.GET)
        .restClientRequestTemplate("{\"someKey\":\"someValue\"}")
        .build();
  }

  @SneakyThrows
  @Test
  void givenProviderAndEmptyDataProductUpdate_whenAddNewDataProductDraft_thenReturnDataProductDto() {
    DataProductUpdateDto requestDataProductUpdate = DataProductUpdateDto.builder().build();
    AuthTestUtils.requestAs(PROVIDER_1)
        .given()
        .contentType(ContentType.JSON)
        .body(MAPPER.writeValueAsString(requestDataProductUpdate))
        .when()
        .post(DataProductControllerV2.PATH)
        .then()
        .statusCode(201);
  }

  @SneakyThrows
  @Test
  void givenProviderAndDataSourceSystemOfAnotherProvider_whenAddNewDataProductDraft_thenReturnNotFound() {
    var tvdDataSourceSystemId = UUID_4CCBfA06.uuid();
    DataProductUpdateDto requestDataProductUpdate = DataProductUpdateDto.builder()
        .dataSourceSystemId(tvdDataSourceSystemId)
        .build();

    AuthTestUtils.requestAs(PROVIDER_1)
        .given()
        .contentType(ContentType.JSON)
        .body(MAPPER.writeValueAsString(requestDataProductUpdate))
        .when()
        .post(DataProductControllerV2.PATH)
        .then()
        .statusCode(404);
  }

  @SneakyThrows
  @Test
  void givenProviderAndRestClientOfAnotherProvider_whenAddNewDataProductDraft_thenReturnNotFound() {
    var tvdRestClientId = UUID_1C438FA1.uuid();
    DataProductUpdateDto requestDataProductUpdate = DataProductUpdateDto.builder()
        .restClientId(tvdRestClientId)
        .build();

    AuthTestUtils.requestAs(PROVIDER_1)
        .given()
        .contentType(ContentType.JSON)
        .body(MAPPER.writeValueAsString(requestDataProductUpdate))
        .when()
        .post(DataProductControllerV2.PATH)
        .then()
        .statusCode(404);
  }

  @SneakyThrows
  @Test
  void givenAdminAndRestClientAndDataSourceSystemFromDifferentProviders_whenAddNewDataProductDraft_thenReturnBadRequest() {
    var agisDataSourceSystemId = UUID_5335D715.uuid();
    var tvdRestClientId = UUID_1C438FA1.uuid();
    DataProductUpdateDto requestDataProductUpdate = DataProductUpdateDto.builder()
        .dataSourceSystemId(agisDataSourceSystemId)
        .restClientId(tvdRestClientId)
        .build();

    AuthTestUtils.requestAs(ADMIN)
        .given()
        .contentType(ContentType.JSON)
        .body(MAPPER.writeValueAsString(requestDataProductUpdate))
        .when()
        .post(DataProductControllerV2.PATH)
        .then()
        .statusCode(400);
  }

  @SneakyThrows
  @ParameterizedTest
  @EnumSource(value = TestUserEnum.class, names = {"PROVIDER_1", "ADMIN"})
  void givenActiveDataProduct_whenUpdateDataProduct_thenReturnBadRequest(TestUserEnum user) {
    UUID activeProductId = TestDataIdentifiers.DataProduct.UUID_085E4B72.uuid();
    DataProductUpdateDto requestDto = DataProductUpdateDto.builder().build();
    AuthTestUtils.requestAs(user)
        .given()
        .contentType(ContentType.JSON)
        .body(MAPPER.writeValueAsString(requestDto))
        .when()
        .put(DataProductControllerV2.PATH + "/" + activeProductId)
        .then()
        .statusCode(400);
  }

  @SneakyThrows
  @ParameterizedTest
  @EnumSource(value = TestUserEnum.class, names = {"PROVIDER_1", "ADMIN"})
  void givenDraftDataProduct_whenUpdateDataProduct_thenReturnDataProduct(TestUserEnum user) {
    DataProductUpdateDto existingDataProductRequest = DataProductUpdateDto.builder().build();
    DataProductDto existingDataProduct = AuthTestUtils.requestAs(user).given().contentType(ContentType.JSON)
        .body(MAPPER.writeValueAsString(existingDataProductRequest)).when().post(DataProductControllerV2.PATH)
        .then().statusCode(201).extract().as(DataProductDto.class);
    DataProductUpdateDto updateRequest = DataProductUpdateDto.builder()
        .name(new DataProductNameDto("Name Deutsch", "Nom Francais", "Nome Italiano"))
        .build();
    DataProductDto updatedDataProduct = AuthTestUtils.requestAs(user).given().contentType(ContentType.JSON)
        .body(MAPPER.writeValueAsString(updateRequest)).when().put(DataProductControllerV2.PATH + "/" + existingDataProduct.id()).then()
        .statusCode(200).extract().as(DataProductDto.class);

    assertThat(updatedDataProduct.name().de()).isEqualTo(updateRequest.name().de());
  }

  @SneakyThrows
  @ParameterizedTest
  @EnumSource(value = TestUserEnum.class, names = {"PROVIDER_1", "ADMIN"})
  void givenValidDataProductDraft_whenActivateDataProduct_thenReturnDataProduct(TestUserEnum user) {
    var agisDataSourceSystemId = UUID_5335D715.uuid();
    var agisRestClientId = UUID_5D3A4A87.uuid();
    DataProductUpdateDto dataProductRequest = getDataProductUpdateDto(agisDataSourceSystemId, agisRestClientId);
    DataProductDto existingDataProduct = AuthTestUtils.requestAs(user).given().contentType(ContentType.JSON)
        .body(MAPPER.writeValueAsString(dataProductRequest)).when().post(DataProductControllerV2.PATH)
        .then().statusCode(201).extract().as(DataProductDto.class);

    DataProductDto updatedDataProduct = AuthTestUtils.requestAs(user).given().contentType(ContentType.JSON)
        .body(MAPPER.writeValueAsString(DataProductStateEnum.ACTIVE)).when()
        .put(DataProductControllerV2.PATH + "/" + existingDataProduct.id() + "/status").then().statusCode(200).extract()
        .as(DataProductDto.class);

    assertThat(updatedDataProduct.stateCode()).isEqualTo(DataProductStateEnum.ACTIVE);
  }

  @SneakyThrows
  @ParameterizedTest
  @EnumSource(value = TestUserEnum.class, names = {"PROVIDER_1", "ADMIN"})
  void givenInvalidDataProductDraft_whenActivateDataProduct_thenReturnBadRequest(TestUserEnum user) {
    DataProductUpdateDto dataProductRequest = DataProductUpdateDto.builder().build();
    DataProductDto existingDataProduct = AuthTestUtils.requestAs(user).given().contentType(ContentType.JSON)
        .body(MAPPER.writeValueAsString(dataProductRequest)).when().post(DataProductControllerV2.PATH)
        .then().statusCode(201).extract().as(DataProductDto.class);

    AuthTestUtils.requestAs(user).given().contentType(ContentType.JSON)
        .body(MAPPER.writeValueAsString(DataProductStateEnum.ACTIVE)).when()
        .put(DataProductControllerV2.PATH + "/" + existingDataProduct.id() + "/status").then().statusCode(400);
  }

  @SneakyThrows
  @ParameterizedTest
  @EnumSource(value = TestUserEnum.class, names = {"PROVIDER_1", "ADMIN"})
  void givenActiveDataProduct_whenActivateDataProduct_thenReturnBadRequest(TestUserEnum user) {
    UUID activeDataProductUid = TestDataIdentifiers.DataProduct.UUID_085E4B72.uuid();

    AuthTestUtils.requestAs(user).given().contentType(ContentType.JSON)
        .body(MAPPER.writeValueAsString(DataProductStateEnum.ACTIVE)).when()
        .put(DataProductControllerV2.PATH + "/" + activeDataProductUid + "/status").then().statusCode(400);
  }

  @SneakyThrows
  @ParameterizedTest
  @EnumSource(value = ch.agridata.product.persistence.DocumentScanStatusEnum.class, names = {"PENDING_SCAN", "REJECTED", "SCAN_FAILED"})
  void givenDraftWithUnscannedDocument_whenActivate_thenBadRequest(ch.agridata.product.persistence.DocumentScanStatusEnum scanStatus) {
    var agisDataSourceSystemId = UUID_5335D715.uuid();
    var agisRestClientId = UUID_5D3A4A87.uuid();
    DataProductUpdateDto dataProductRequest = getDataProductUpdateDto(agisDataSourceSystemId, agisRestClientId);
    DataProductDto existingDataProduct = AuthTestUtils.requestAs(PROVIDER_1).given().contentType(ContentType.JSON)
        .body(MAPPER.writeValueAsString(dataProductRequest)).when().post(DataProductControllerV2.PATH)
        .then().statusCode(201).extract().as(DataProductDto.class);
    DataProductDocumentMetadataDto document = uploadDocument(PROVIDER_1, existingDataProduct.id(), "report.pdf", SAMPLE_PDF);
    forceScanStatus(document.id(), scanStatus);

    AuthTestUtils.requestAs(PROVIDER_1).given().contentType(ContentType.JSON)
        .body(MAPPER.writeValueAsString(DataProductStateEnum.ACTIVE)).when()
        .put(DataProductControllerV2.PATH + "/" + existingDataProduct.id() + "/status").then().statusCode(400);
  }

  @SneakyThrows
  @Test
  void givenDraftWithAvailableDocument_whenActivate_thenActivated() {
    var agisDataSourceSystemId = UUID_5335D715.uuid();
    var agisRestClientId = UUID_5D3A4A87.uuid();
    DataProductUpdateDto dataProductRequest = getDataProductUpdateDto(agisDataSourceSystemId, agisRestClientId);
    DataProductDto existingDataProduct = AuthTestUtils.requestAs(PROVIDER_1).given().contentType(ContentType.JSON)
        .body(MAPPER.writeValueAsString(dataProductRequest)).when().post(DataProductControllerV2.PATH)
        .then().statusCode(201).extract().as(DataProductDto.class);
    DataProductDocumentMetadataDto document = uploadDocument(PROVIDER_1, existingDataProduct.id(), "report.pdf", SAMPLE_PDF);
    forceScanStatus(document.id(), ch.agridata.product.persistence.DocumentScanStatusEnum.AVAILABLE);

    DataProductDto activated = AuthTestUtils.requestAs(PROVIDER_1).given().contentType(ContentType.JSON)
        .body(MAPPER.writeValueAsString(DataProductStateEnum.ACTIVE)).when()
        .put(DataProductControllerV2.PATH + "/" + existingDataProduct.id() + "/status").then().statusCode(200).extract()
        .as(DataProductDto.class);

    assertThat(activated.stateCode()).isEqualTo(DataProductStateEnum.ACTIVE);
  }

  @SneakyThrows
  @ParameterizedTest
  @EnumSource(value = TestUserEnum.class, names = {"PROVIDER_1", "ADMIN"})
  void givenAccessibleDraftDataProduct_whenGetDataProduct_thenReturnDataProduct(TestUserEnum user) {
    DataProductUpdateDto dataProductRequest =
        DataProductUpdateDto.builder().restClientPathTemplate("/test").restClientId(UUID_B1398C9D.uuid()).build();
    DataProductDto existingDataProduct = AuthTestUtils.requestAs(PROVIDER_1).given().contentType(ContentType.JSON)
        .body(MAPPER.writeValueAsString(dataProductRequest)).when().post(DataProductControllerV2.PATH)
        .then().statusCode(201).extract().as(DataProductDto.class);
    DataProductDto fetchedDataProduct =
        AuthTestUtils.requestAs(user).when().get(DataProductControllerV2.PATH + "/" + existingDataProduct.id()).then().statusCode(200)
            .extract().as(DataProductDto.class);
    assertThat(fetchedDataProduct.restClientPathTemplate()).isEqualTo(existingDataProduct.restClientPathTemplate());
    assertThat(fetchedDataProduct.restClient().url()).isEqualTo(agisApiUrl);
  }

  @SneakyThrows
  @Test
  void givenProviderAndInaccessibleDataProduct_whenGetDataProduct_thenReturnNotFound() {
    DataProductUpdateDto dataProductRequest = DataProductUpdateDto.builder().restClientPathTemplate("/test").build();
    DataProductDto existingDataProduct = AuthTestUtils.requestAs(ADMIN).given().contentType(ContentType.JSON)
        .body(MAPPER.writeValueAsString(dataProductRequest)).when().post(DataProductControllerV2.PATH)
        .then().statusCode(201).extract().as(DataProductDto.class);
    AuthTestUtils.requestAs(PROVIDER_1).when().get(DataProductControllerV2.PATH + "/" + existingDataProduct.id()).then().statusCode(404);
  }

  @Test
  void givenProviderOwnsProduct_whenUploadValidPdf_thenCreatedWithPendingScan() {
    UUID productId = createDraft(PROVIDER_1);

    DataProductDocumentMetadataDto document = uploadDocument(PROVIDER_1, productId, "report.pdf", SAMPLE_PDF);

    assertThat(document.id()).isNotNull();
    assertThat(document.fileName()).isEqualTo("report.pdf");
    assertThat(document.sizeBytes()).isEqualTo((long) SAMPLE_PDF.length);
    assertThat(document.scanStatus()).isEqualTo(DocumentScanStatusEnum.PENDING_SCAN);
  }

  @Test
  void givenAdmin_whenUploadValidPdf_thenCreated() {
    UUID productId = createDraft(ADMIN);

    DataProductDocumentMetadataDto document = uploadDocument(ADMIN, productId, "admin.pdf", SAMPLE_PDF);

    assertThat(document.id()).isNotNull();
  }

  @Test
  void givenMissingMultipartPart_whenUpload_thenBadRequest() {
    UUID productId = createDraft(PROVIDER_1);

    AuthTestUtils.requestAs(PROVIDER_1)
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .multiPart("wrongPart", "report.pdf", SAMPLE_PDF, "application/pdf")
        .when()
        .post(DataProductControllerV2.PATH + "/" + productId + "/documents")
        .then()
        .statusCode(400);
  }

  @Test
  void givenNonPdfContent_whenUpload_thenBadRequest() {
    UUID productId = createDraft(PROVIDER_1);

    AuthTestUtils.requestAs(PROVIDER_1)
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .multiPart("document", "report.pdf", "not-a-pdf".getBytes(StandardCharsets.UTF_8), "application/pdf")
        .when()
        .post(DataProductControllerV2.PATH + "/" + productId + "/documents")
        .then()
        .statusCode(400);
  }

  @Test
  void givenWrongContentType_whenUpload_thenBadRequest() {
    UUID productId = createDraft(PROVIDER_1);

    AuthTestUtils.requestAs(PROVIDER_1)
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .multiPart("document", "report.pdf", SAMPLE_PDF, "text/plain")
        .when()
        .post(DataProductControllerV2.PATH + "/" + productId + "/documents")
        .then()
        .statusCode(400);
  }

  @Test
  void givenProviderDoesNotOwnProduct_whenUpload_thenNotFound() {
    UUID productId = createDraft(ADMIN);

    AuthTestUtils.requestAs(PROVIDER_1)
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .multiPart("document", "report.pdf", SAMPLE_PDF, "application/pdf")
        .when()
        .post(DataProductControllerV2.PATH + "/" + productId + "/documents")
        .then()
        .statusCode(404);
  }

  @Test
  void givenDataProductAtDocumentLimit_whenUploadAnotherDocument_thenBadRequest() {
    UUID productId = createDraft(PROVIDER_1);
    // A data product accepts at most 5 documents; fill the quota, then the next upload must be rejected.
    for (int i = 0; i < 5; i++) {
      uploadDocument(PROVIDER_1, productId, "report-" + i + ".pdf", SAMPLE_PDF);
    }

    AuthTestUtils.requestAs(PROVIDER_1)
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .multiPart("document", "report-6.pdf", SAMPLE_PDF, "application/pdf")
        .when()
        .post(DataProductControllerV2.PATH + "/" + productId + "/documents")
        .then()
        .statusCode(400);
  }

  @ParameterizedTest
  @EnumSource(value = TestUserEnum.class, names = {"PROVIDER_1", "ADMIN"})
  void givenUploadedDocument_whenGetMetadataList_thenReturnsDocument(TestUserEnum user) {
    UUID productId = createDraft(user);
    DataProductDocumentMetadataDto document = uploadDocument(user, productId, "report.pdf", SAMPLE_PDF);

    AuthTestUtils.requestAs(user)
        .when()
        .get(DataProductControllerV2.PATH + "/" + productId + "/documents")
        .then()
        .statusCode(200)
        .body("size()", equalTo(1))
        .body("[0].id", equalTo(document.id().toString()))
        .body("[0].fileName", equalTo("report.pdf"));
  }

  @Test
  void givenNoDocuments_whenGetMetadataList_thenReturnsEmptyList() {
    UUID productId = createDraft(PROVIDER_1);

    AuthTestUtils.requestAs(PROVIDER_1)
        .when()
        .get(DataProductControllerV2.PATH + "/" + productId + "/documents")
        .then()
        .statusCode(200)
        .body("size()", equalTo(0));
  }

  @Test
  void givenProviderDoesNotOwnProduct_whenGetMetadataList_thenNotFound() {
    UUID productId = createDraft(ADMIN);
    uploadDocument(ADMIN, productId, "report.pdf", SAMPLE_PDF);

    AuthTestUtils.requestAs(PROVIDER_1)
        .when()
        .get(DataProductControllerV2.PATH + "/" + productId + "/documents")
        .then()
        .statusCode(404);
  }

  @ParameterizedTest
  @EnumSource(value = TestUserEnum.class, names = {"PROVIDER_1", "ADMIN"})
  void givenUploadedDocument_whenGetMetadata_thenReturnsPendingScan(TestUserEnum user) {
    UUID productId = createDraft(user);
    DataProductDocumentMetadataDto document = uploadDocument(user, productId, "report.pdf", SAMPLE_PDF);

    DataProductDocumentMetadataDto metadata = AuthTestUtils.requestAs(user)
        .when()
        .get(DataProductControllerV2.PATH + "/" + productId + "/documents/" + document.id())
        .then()
        .statusCode(200)
        .extract().as(DataProductDocumentMetadataDto.class);

    assertThat(metadata.id()).isEqualTo(document.id());
    assertThat(metadata.fileName()).isEqualTo("report.pdf");
    assertThat(metadata.scanStatus()).isEqualTo(DocumentScanStatusEnum.PENDING_SCAN);
  }

  @Test
  void givenAvailableDocument_whenGetMetadata_thenReturnsAvailable() {
    UUID productId = createDraft(PROVIDER_1);
    DataProductDocumentMetadataDto document = uploadDocument(PROVIDER_1, productId, "report.pdf", SAMPLE_PDF);
    forceScanStatus(document.id(), ch.agridata.product.persistence.DocumentScanStatusEnum.AVAILABLE);

    DataProductDocumentMetadataDto metadata = AuthTestUtils.requestAs(PROVIDER_1)
        .when()
        .get(DataProductControllerV2.PATH + "/" + productId + "/documents/" + document.id())
        .then()
        .statusCode(200)
        .extract().as(DataProductDocumentMetadataDto.class);

    assertThat(metadata.scanStatus()).isEqualTo(DocumentScanStatusEnum.AVAILABLE);
  }

  @Test
  void givenUnknownDocument_whenGetMetadata_thenNotFound() {
    UUID productId = createDraft(PROVIDER_1);

    AuthTestUtils.requestAs(PROVIDER_1)
        .when()
        .get(DataProductControllerV2.PATH + "/" + productId + "/documents/" + UUID.randomUUID())
        .then()
        .statusCode(404);
  }

  @Test
  void givenProviderDoesNotOwnProduct_whenGetMetadata_thenNotFound() {
    UUID productId = createDraft(ADMIN);
    DataProductDocumentMetadataDto document = uploadDocument(ADMIN, productId, "report.pdf", SAMPLE_PDF);

    AuthTestUtils.requestAs(PROVIDER_1)
        .when()
        .get(DataProductControllerV2.PATH + "/" + productId + "/documents/" + document.id())
        .then()
        .statusCode(404);
  }

  @ParameterizedTest
  @EnumSource(value = TestUserEnum.class, names = {"PROVIDER_1", "ADMIN"})
  void givenAvailableDocument_whenDownload_thenReturnsContent(TestUserEnum user) {
    when(pdfStorageApi.download(anyString(), anyString())).thenReturn(SAMPLE_PDF);
    UUID productId = createDraft(user);
    DataProductDocumentMetadataDto document = uploadDocument(user, productId, "report.pdf", SAMPLE_PDF);
    forceScanStatus(document.id(), ch.agridata.product.persistence.DocumentScanStatusEnum.AVAILABLE);

    byte[] content = AuthTestUtils.requestAs(user)
        .when()
        .get(DataProductControllerV2.PATH + "/" + productId + "/documents/" + document.id() + "/download")
        .then()
        .statusCode(200)
        .header("Content-Disposition", containsString("report.pdf"))
        .extract().asByteArray();

    assertThat(new String(content, StandardCharsets.UTF_8)).isEqualTo(new String(SAMPLE_PDF, StandardCharsets.UTF_8));
  }

  @Test
  void givenPendingScanDocument_whenDownload_thenForbidden() {
    UUID productId = createDraft(PROVIDER_1);
    DataProductDocumentMetadataDto document = uploadDocument(PROVIDER_1, productId, "report.pdf", SAMPLE_PDF);

    AuthTestUtils.requestAs(PROVIDER_1)
        .when()
        .get(DataProductControllerV2.PATH + "/" + productId + "/documents/" + document.id() + "/download")
        .then()
        .statusCode(403);
  }

  @Test
  void givenRejectedDocument_whenDownload_thenForbidden() {
    UUID productId = createDraft(PROVIDER_1);
    DataProductDocumentMetadataDto document = uploadDocument(PROVIDER_1, productId, "malware.pdf", SAMPLE_PDF);
    forceScanStatus(document.id(), ch.agridata.product.persistence.DocumentScanStatusEnum.REJECTED);

    AuthTestUtils.requestAs(PROVIDER_1)
        .when()
        .get(DataProductControllerV2.PATH + "/" + productId + "/documents/" + document.id() + "/download")
        .then()
        .statusCode(403);
  }

  @Test
  void givenUnknownDocument_whenDownload_thenNotFound() {
    UUID productId = createDraft(PROVIDER_1);

    AuthTestUtils.requestAs(PROVIDER_1)
        .when()
        .get(DataProductControllerV2.PATH + "/" + productId + "/documents/" + UUID.randomUUID() + "/download")
        .then()
        .statusCode(404);
  }

  @Test
  void givenConsumer_whenDownload_thenForbidden() {
    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .when()
        .get(DataProductControllerV2.PATH + "/" + UUID.randomUUID() + "/documents/" + UUID.randomUUID() + "/download")
        .then()
        .statusCode(403);
  }

  @ParameterizedTest
  @EnumSource(value = TestUserEnum.class, names = {"PROVIDER_1", "ADMIN"})
  void givenUploadedDocument_whenDelete_thenNoContentAndGone(TestUserEnum user) {
    UUID productId = createDraft(user);
    DataProductDocumentMetadataDto document = uploadDocument(user, productId, "report.pdf", SAMPLE_PDF);

    AuthTestUtils.requestAs(user)
        .when()
        .delete(DataProductControllerV2.PATH + "/" + productId + "/documents/" + document.id())
        .then()
        .statusCode(204);

    AuthTestUtils.requestAs(user)
        .when()
        .get(DataProductControllerV2.PATH + "/" + productId + "/documents/" + document.id())
        .then()
        .statusCode(404);
  }

  @Test
  void givenProviderDoesNotOwnProduct_whenDelete_thenNotFound() {
    UUID productId = createDraft(ADMIN);
    DataProductDocumentMetadataDto document = uploadDocument(ADMIN, productId, "report.pdf", SAMPLE_PDF);

    AuthTestUtils.requestAs(PROVIDER_1)
        .when()
        .delete(DataProductControllerV2.PATH + "/" + productId + "/documents/" + document.id())
        .then()
        .statusCode(404);
  }

  @Test
  void givenConsumer_whenDelete_thenForbidden() {
    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .when()
        .delete(DataProductControllerV2.PATH + "/" + UUID.randomUUID() + "/documents/" + UUID.randomUUID())
        .then()
        .statusCode(403);
  }

  @SneakyThrows
  private UUID createDraft(TestUserEnum user) {
    return AuthTestUtils.requestAs(user)
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

  private DataProductDocumentMetadataDto uploadDocument(TestUserEnum user, UUID productId, String fileName, byte[] content) {
    return AuthTestUtils.requestAs(user)
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .multiPart("document", fileName, content, "application/pdf")
        .when()
        .post(DataProductControllerV2.PATH + "/" + productId + "/documents")
        .then()
        .statusCode(201)
        .extract().as(DataProductDocumentMetadataDto.class);
  }

  private void forceScanStatus(UUID documentId, ch.agridata.product.persistence.DocumentScanStatusEnum status) {
    QuarkusTransaction.requiringNew()
        .run(() -> documentRepository.update("scanStatus = ?1 where id = ?2", status, documentId));
  }
}

