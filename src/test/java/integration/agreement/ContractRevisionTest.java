package integration.agreement;

import static integration.testutils.TestUserEnum.CONSUMER_BIO_SUISSE;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import ch.agridata.agreement.controller.ContractRevisionController;
import ch.agridata.agreement.dto.DataRequestDto;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestUserEnum;
import io.quarkus.test.junit.QuarkusTest;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class ContractRevisionTest {

  private final Flyway flyway;

  @BeforeEach
  void setUp() {
    flyway.migrate();
  }

  @Test
  void givenExistingContractRevisionOfCurrentConsumer_whenGetById_thenReturnContractRevision() {
    DataRequestDto dataRequest = DataRequestTestFactory.createReadyForSigningDataRequest();
    UUID contractRevisionId = dataRequest.currentContractRevisionId();

    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE).given()
        .when()
        .get(ContractRevisionController.PATH + "/" + contractRevisionId)
        .then()
        .statusCode(200)
        .body("id", equalTo(contractRevisionId.toString()))
        .body("dataRequestId", equalTo(dataRequest.id().toString()))
        .body("createdAt", notNullValue());
  }

  @Test
  void givenContractRevisionOfAnotherConsumer_whenGetById_thenReturn404() {
    UUID contractRevisionId = DataRequestTestFactory.createContractRevisionAndReturnId();

    AuthTestUtils.requestAs(TestUserEnum.CONSUMER_IP_SUISSE).given()
        .when()
        .get(ContractRevisionController.PATH + "/" + contractRevisionId)
        .then()
        .statusCode(404);
  }

  @Test
  void givenNonExistingContractRevision_whenGetById_thenReturn404() {
    UUID unknownId = UUID.randomUUID();

    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE).given()
        .when()
        .get(ContractRevisionController.PATH + "/" + unknownId)
        .then()
        .statusCode(404);
  }
}
