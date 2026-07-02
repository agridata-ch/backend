package integration.product;

import static integration.testutils.TestUserEnum.CONSUMER_BIO_SUISSE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import ch.agridata.product.controller.DataProviderController;
import ch.agridata.product.dto.DataSourceSystemDto;
import ch.agridata.product.dto.RestClientDto;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestDataIdentifiers;
import integration.testutils.TestUserEnum;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.specification.RequestSpecification;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@QuarkusTest
class DataProviderControllerTest {
  @Test
  void givenValidRequest_whenGetDataProviders_thenValidDataProviders() {
    RequestSpecification consumer = AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE);

    consumer.when()
        .get(DataProviderController.PATH)
        .then()
        .statusCode(200)
        .body("size()", greaterThan(0)) // ensure at least one item is returned
        .body("every { it.id?.toString().length() > 0 }", is(true))
        .body("every { it.name?.toString().length() > 0 }", is(true))
        .body("every { it.code?.toString().length() > 0 }", is(true));
  }

  @Test
  void givenValidRequest_whenGetDataProvider_thenValidDataProvider() {
    RequestSpecification consumer = AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE);

    consumer.when()
        .get(DataProviderController.PATH + "/" + TestDataIdentifiers.DataProvider.UUID_E37B148B.uuid())
        .then()
        .statusCode(200)
        .body("id", notNullValue())
        .body("id.toString()", not(emptyOrNullString()))
        .body("name", notNullValue())
        .body("name.toString()", not(emptyOrNullString()))
        .body("code", notNullValue())
        .body("code.toString()", not(emptyOrNullString()));
  }

  @Test
  void givenUnknownId_whenGetDataProvider_then404() {
    RequestSpecification consumer = AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE);
    UUID unknownId = UUID.fromString("00000000-0000-0000-0000-000000000000");

    consumer.when()
        .get(DataProviderController.PATH + "/" + unknownId)
        .then()
        .statusCode(404);
  }

  @ParameterizedTest
  @EnumSource(value = TestUserEnum.class, names = {"PROVIDER_1", "ADMIN"})
  void givenValidDataProviderId_whenGetDataSourceSystems_thenReturnDataSourceSystemDtos(TestUserEnum user) {
    UUID knownId = TestDataIdentifiers.DataProvider.UUID_61404B83.uuid();

    List<DataSourceSystemDto> dataSourceSystemDtos =
        AuthTestUtils.requestAs(user).when().get(DataProviderController.PATH + "/" + knownId.toString() + "/dataSourceSystems").then()
            .statusCode(200).extract().as(new TypeRef<>() {
            });

    assertThat(dataSourceSystemDtos).hasSize(2);
  }

  @ParameterizedTest
  @EnumSource(value = TestUserEnum.class, names = {"PROVIDER_1", "ADMIN"})
  void givenUnkownDataProviderId_whenGetDataSourceSystems_thenReturnDataSourceSystemDtos(TestUserEnum user) {
    UUID unknownId = UUID.fromString("00000000-0000-0000-0000-000000000000");

    AuthTestUtils.requestAs(user).when().get(DataProviderController.PATH + "/" + unknownId + "/dataSourceSystems").then()
        .statusCode(404);
  }

  @ParameterizedTest
  @EnumSource(value = TestUserEnum.class, names = {"PROVIDER_1", "ADMIN"})
  void givenKnownDataProviderId_whenGetRestClients_thenReturnRestClientDtos(TestUserEnum user) {
    UUID knownId = TestDataIdentifiers.DataProvider.UUID_61404B83.uuid();

    List<RestClientDto> restClientDtos =
        AuthTestUtils.requestAs(user).when().get(DataProviderController.PATH + "/" + knownId.toString() + "/restClients").then()
            .statusCode(200).extract().as(new TypeRef<>() {
            });

    assertThat(restClientDtos).hasSize(2);
  }

  @ParameterizedTest
  @EnumSource(value = TestUserEnum.class, names = {"PROVIDER_1", "ADMIN"})
  void givenUnkownRestClientId_whenGetRestClients_thenReturnRestClientDtos(TestUserEnum user) {
    UUID unknownId = UUID.fromString("00000000-0000-0000-0000-000000000000");

    AuthTestUtils.requestAs(user).when().get(DataProviderController.PATH + "/" + unknownId + "/restClients").then()
        .statusCode(404);
  }

}
