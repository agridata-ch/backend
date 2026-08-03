package integration.product;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.product.controller.PublicDataSourceSystemController;
import ch.agridata.product.dto.DataSourceSystemDto;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import java.util.List;
import org.junit.jupiter.api.Test;

@QuarkusTest
class PublicDataSourceSystemControllerTest {

  @Test
  void givenUnauthenticatedRequest_whenGetPublicDataSourceSystems_thenReturnDataSourceSystems() {
    List<DataSourceSystemDto> dataSourceSystems = given()
        .when()
        .get(PublicDataSourceSystemController.PATH)
        .then()
        .statusCode(200)
        .extract().as(new TypeRef<>() {
        });

    assertThat(dataSourceSystems)
        .isNotEmpty()
        .allSatisfy(dto -> {
          assertThat(dto.id()).isNotNull();
          assertThat(dto.code()).isNotBlank();
          assertThat(dto.dataProvider()).isNotNull();
          assertThat(dto.dataProvider().id()).isNotNull();
          assertThat(dto.legalBasis()).isNotNull();
        });
  }
}
