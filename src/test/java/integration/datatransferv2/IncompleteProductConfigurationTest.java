package integration.datatransferv2;

import static integration.testutils.TestDataIdentifiers.DataProduct.UUID_C661EA48;
import static integration.testutils.TestUserEnum.ADMIN;
import static integration.testutils.TestUserEnum.CONSUMER_BIO_SUISSE;
import static org.hamcrest.Matchers.containsString;

import ch.agridata.datatransferv2.controller.DataTransferController;
import ch.agridata.product.controller.DataProductControllerV2;
import ch.agridata.product.dto.DataProductUpdateDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestDataIdentifiers.Uid;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openapitools.jackson.nullable.JsonNullable;

/**
 * Integration tests covering active products whose technical configuration is incomplete. Every technical field is optional, so a
 * product can be activated (or patched) without the rest client, flow, method or path template that a data transfer needs. Those
 * endpoints must answer with a readable 400 instead of failing deep inside a flow with a NullPointerException.
 *
 * <p>Product under test: c661ea48 (AGIS person data, UID_BASED_PRE_VALIDATION, has a change detection path template)
 *
 * @CommentLastReviewed 2026-09-11
 */
@QuarkusTest
@RequiredArgsConstructor
class IncompleteProductConfigurationTest {

  private static final UUID PRODUCT_ID = UUID_C661EA48.uuid();

  private final Flyway flyway;
  private final ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    flyway.migrate();
  }

  @ParameterizedTest
  @ValueSource(strings = {"restClient", "flowCode", "restClientMethodCode", "restClientPathTemplate"})
  void givenActiveProductWithMissingTransferField_whenDataTransfer_thenBadRequestNamingTheField(String missingField) {
    clearField(missingField);

    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .pathParam("productId", PRODUCT_ID.toString())
        .queryParam("uid", Uid.CHE102000002.name())
        .when().get(DataTransferController.PATH + "/product/{productId}/data")
        .then()
        .statusCode(400)
        .body("debugMessage", containsString(missingField));
  }

  @ParameterizedTest
  @ValueSource(strings = {"restClient", "flowCode"})
  void givenActiveProductWithMissingTransferField_whenGetModifiedProducers_thenBadRequestNamingTheField(String missingField) {
    clearField(missingField);

    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .queryParam("since", "1970-01-01")
        .when().get(DataTransferController.PATH + "/product/" + PRODUCT_ID + "/modified-producers")
        .then()
        .statusCode(400)
        .body("debugMessage", containsString(missingField));
  }

  @SneakyThrows
  private void clearField(String missingField) {
    var builder = DataProductUpdateDto.builder();
    switch (missingField) {
      case "restClient" -> builder.restClientId(JsonNullable.of(null));
      case "flowCode" -> builder.flowCode(JsonNullable.of(null));
      case "restClientMethodCode" -> builder.restClientMethodCode(JsonNullable.of(null));
      case "restClientPathTemplate" -> builder.restClientPathTemplate(JsonNullable.of(null));
      default -> throw new IllegalArgumentException("Unknown field: " + missingField);
    }

    AuthTestUtils.requestAs(ADMIN)
        .contentType(ContentType.JSON)
        .when()
        .body(objectMapper.writeValueAsString(builder.build()))
        .patch(DataProductControllerV2.PATH + "/" + PRODUCT_ID)
        .then()
        .statusCode(200);
  }
}
