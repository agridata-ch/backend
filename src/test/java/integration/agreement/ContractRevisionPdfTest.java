package integration.agreement;

import static integration.agreement.DataRequestTestFactory.createReadyForSigningByConsumerDataRequestFor;
import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.agreement.controller.ContractRevisionController;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestUserEnum;
import io.quarkus.test.junit.QuarkusTest;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ContractRevisionPdfTest {

  private static final String PDF_PATH = ContractRevisionController.PATH + "/{id}/pdf";

  @Test
  void givenExistingRevision_whenGetPdfAsConsumer_thenReturn200AndPdf() {
    UUID revisionId = createReadyForSigningByConsumerDataRequestFor(TestUserEnum.CONSUMER_BLV_1).currentContractRevisionId();
    byte[] response = AuthTestUtils.requestAs(TestUserEnum.CONSUMER_BLV_1)
        .pathParam("id", revisionId)
        .when().get(PDF_PATH)
        .then()
        .statusCode(200)
        .contentType("application/pdf")
        .header("Content-Disposition", String.format("attachment; filename=\"contract-revision-%s.pdf\"", revisionId))
        .extract().asByteArray();

    assertThat(response).isNotEmpty();
    assertThat(new String(response, 0, 4)).isEqualTo("%PDF");
  }

  @Test
  void givenExistingRevision_whenGetPdfAsNonOwningConsumer_thenReturn404() {
    UUID revisionId = createReadyForSigningByConsumerDataRequestFor(TestUserEnum.CONSUMER_BLV_1).currentContractRevisionId();
    AuthTestUtils.requestAs(TestUserEnum.CONSUMER_BIO_SUISSE)
        .pathParam("id", revisionId)
        .when().get(PDF_PATH)
        .then()
        .statusCode(404);
  }

  @Test
  void givenNonExistingRevision_whenGetPdf_thenReturn404() {
    // Act & Assert
    AuthTestUtils.requestAs(TestUserEnum.CONSUMER_BLV_1)
        .pathParam("id", UUID.randomUUID())
        .when().get(PDF_PATH)
        .then()
        .statusCode(404);
  }

}
