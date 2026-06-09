package integration.product;

import static integration.testutils.TestUserEnum.ADMIN;
import static integration.testutils.TestUserEnum.CONSUMER_BIO_SUISSE;
import static integration.testutils.TestUserEnum.PROVIDER_1;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;

import ch.agridata.product.controller.DataProductControllerV2;
import integration.testutils.AuthTestUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;

@QuarkusTest
class DataProductControllerV2Test {

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
}

