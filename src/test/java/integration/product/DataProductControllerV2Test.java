package integration.product;

import static integration.testutils.TestUserEnum.ADMIN;
import static integration.testutils.TestUserEnum.CONSUMER_BIO_SUISSE;
import static integration.testutils.TestUserEnum.PROVIDER_1;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;

import ch.agridata.product.controller.DataProductControllerV2;
import ch.agridata.product.dto.DataProductDescriptionDto;
import ch.agridata.product.dto.DataProductDto;
import ch.agridata.product.dto.DataProductNameDto;
import ch.agridata.product.dto.DataProductStateEnum;
import ch.agridata.product.dto.DataProductUpdateDto;
import ch.agridata.product.dto.FlowCodeEnum;
import ch.agridata.product.dto.RestClientMethodCodeEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestDataIdentifiers;
import integration.testutils.TestUserEnum;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@QuarkusTest
class DataProductControllerV2Test {
  private static final ObjectMapper MAPPER = new ObjectMapper();

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
    var agisDataSourceSystemId = UUID.fromString("5335d715-e95c-4777-a424-ab73f2ff5618");
    var agisRestClientId = UUID.fromString("5d3a4a87-63fc-4428-8044-313d222efe1d");
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
    var tvdDataSourceSystemId = UUID.fromString("4ccbfa06-a547-4a76-9dfc-61c22a4ea8ce");
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
    var tvdRestClientId = UUID.fromString("1c438fa1-1112-4ee9-b1af-2d96acf385f0");
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
    var agisDataSourceSystemId = UUID.fromString("5335d715-e95c-4777-a424-ab73f2ff5618");
    var tvdRestClientId = UUID.fromString("1c438fa1-1112-4ee9-b1af-2d96acf385f0");
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
    var agisDataSourceSystemId = UUID.fromString("5335d715-e95c-4777-a424-ab73f2ff5618");
    var agisRestClientId = UUID.fromString("5d3a4a87-63fc-4428-8044-313d222efe1d");
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
  @EnumSource(value = TestUserEnum.class, names = {"PROVIDER_1", "ADMIN"})
  void givenAccessibleDraftDataProduct_whenGetDataProduct_thenReturnDataProduct(TestUserEnum user) {
    DataProductUpdateDto dataProductRequest = DataProductUpdateDto.builder().restClientPathTemplate("/test").build();
    DataProductDto existingDataProduct = AuthTestUtils.requestAs(PROVIDER_1).given().contentType(ContentType.JSON)
        .body(MAPPER.writeValueAsString(dataProductRequest)).when().post(DataProductControllerV2.PATH)
        .then().statusCode(201).extract().as(DataProductDto.class);
    DataProductDto fetchedDataProduct =
        AuthTestUtils.requestAs(user).when().get(DataProductControllerV2.PATH + "/" + existingDataProduct.id()).then().statusCode(200)
            .extract().as(DataProductDto.class);
    assertThat(fetchedDataProduct.restClientPathTemplate()).isEqualTo(existingDataProduct.restClientPathTemplate());
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
}

