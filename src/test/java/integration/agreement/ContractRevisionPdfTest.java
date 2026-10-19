package integration.agreement;

import static ch.agridata.user.service.ImpersonationHeaderFilter.IMPERSONATION_HEADER;
import static integration.agreement.DataRequestTestFactory.createReadyForSigningByConsumerDataRequestFor;
import static integration.agreement.DataRequestTestFactory.createReadyForSigningByProviderDataRequest;
import static integration.agreement.DataRequestTestFactory.signContractRevision;
import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.agreement.controller.ContractRevisionController;
import ch.agridata.agreement.dto.DataRequestDto;
import ch.agridata.agreement.dto.SignatureSlotCodeEnum;
import ch.agridata.user.controller.UserController;
import integration.testutils.AuthTestUtils;
import integration.testutils.TestDataIdentifiers;
import integration.testutils.TestUserEnum;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.specification.RequestSpecification;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@QuarkusTest
@RequiredArgsConstructor
class ContractRevisionPdfTest {

  private static final String PDF_PATH = ContractRevisionController.PATH + "/{id}/pdf";
  private static final String PRODUCER_UID = TestDataIdentifiers.Uid.CHE101000001.name();

  private final EntityManager entityManager;
  private final Flyway flyway;

  @BeforeEach
  void setUp() {
    flyway.migrate();
  }

  @Test
  void givenExistingRevision_whenGetPdfAsAdmin_thenReturn200AndPdf() {
    UUID revisionId = createReadyForSigningByConsumerDataRequestFor(TestUserEnum.CONSUMER_BLV_1).currentContractRevisionId();
    byte[] response = AuthTestUtils.requestAs(TestUserEnum.ADMIN)
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
  void givenExistingRevision_whenGetPdfAsProvider_thenReturn200AndPdf() {
    UUID revisionId = createReadyForSigningByProviderDataRequest(TestUserEnum.CONSUMER_BLV_1, TestUserEnum.CONSUMER_BLV_2)
        .currentContractRevisionId();
    byte[] response = AuthTestUtils.requestAs(TestUserEnum.PROVIDER_1)
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
  void givenNonExistingRevision_whenGetPdf_thenReturn404() {
    // Act & Assert
    AuthTestUtils.requestAs(TestUserEnum.CONSUMER_BLV_1)
        .pathParam("id", UUID.randomUUID())
        .when().get(PDF_PATH)
        .then()
        .statusCode(404);
  }

  @ParameterizedTest
  @EnumSource(ProducerAccess.class)
  void givenCurrentRevisionOfAggregatedDataRequest_whenGetPdfAsProducer_thenReturn200AndPdf(ProducerAccess access) {
    DataRequestDto dataRequest = createReadyForSigningByConsumerDataRequestFor(TestUserEnum.CONSUMER_BLV_1);
    insertConsentRequest(dataRequest.id(), PRODUCER_UID, "OPENED");
    UUID revisionId = dataRequest.currentContractRevisionId();

    byte[] response = access.requestAs(TestUserEnum.PRODUCER_A)
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

  @ParameterizedTest
  @EnumSource(ProducerAccess.class)
  void givenConsentRequestForSecondUidOfProducer_whenGetPdfAsProducer_thenReturn200(ProducerAccess access) {
    DataRequestDto dataRequest = createReadyForSigningByConsumerDataRequestFor(TestUserEnum.CONSUMER_BLV_1);
    insertConsentRequest(dataRequest.id(), TestDataIdentifiers.Uid.CHE102000002.name(), "OPENED");

    access.requestAs(TestUserEnum.PRODUCER_B)
        .pathParam("id", dataRequest.currentContractRevisionId())
        .when().get(PDF_PATH)
        .then()
        .statusCode(200)
        .contentType("application/pdf");
  }

  @ParameterizedTest
  @EnumSource(ProducerAccess.class)
  void givenRevisionWithoutConsentRequestOfProducer_whenGetPdfAsProducer_thenReturn404(ProducerAccess access) {
    UUID revisionId = createReadyForSigningByConsumerDataRequestFor(TestUserEnum.CONSUMER_BLV_1).currentContractRevisionId();

    access.requestAs(TestUserEnum.PRODUCER_A)
        .pathParam("id", revisionId)
        .when().get(PDF_PATH)
        .then()
        .statusCode(404);
  }

  @ParameterizedTest
  @EnumSource(ProducerAccess.class)
  void givenWithdrawnConsentRequestOfProducer_whenGetPdfAsProducer_thenReturn404(ProducerAccess access) {
    DataRequestDto dataRequest = createReadyForSigningByConsumerDataRequestFor(TestUserEnum.CONSUMER_BLV_1);
    insertConsentRequest(dataRequest.id(), PRODUCER_UID, "WITHDRAWN");

    access.requestAs(TestUserEnum.PRODUCER_A)
        .pathParam("id", dataRequest.currentContractRevisionId())
        .when().get(PDF_PATH)
        .then()
        .statusCode(404);
  }

  @ParameterizedTest
  @EnumSource(ProducerAccess.class)
  void givenConsentRequestOfOtherProducer_whenGetPdfAsProducer_thenReturn404(ProducerAccess access) {
    var otherProducerUid = TestDataIdentifiers.Uid.CHE102000001.name();
    DataRequestDto dataRequest = createReadyForSigningByConsumerDataRequestFor(TestUserEnum.CONSUMER_BLV_1);
    insertConsentRequest(dataRequest.id(), otherProducerUid, "OPENED");

    access.requestAs(TestUserEnum.PRODUCER_A)
        .pathParam("id", dataRequest.currentContractRevisionId())
        .when().get(PDF_PATH)
        .then()
        .statusCode(404);
  }

  @ParameterizedTest
  @EnumSource(ProducerAccess.class)
  void givenOutdatedRevisionOfAggregatedDataRequest_whenGetPdfAsProducer_thenReturn404(ProducerAccess access) {
    DataRequestDto initial = createReadyForSigningByConsumerDataRequestFor(TestUserEnum.CONSUMER_BLV_1);
    UUID outdatedRevisionId = initial.currentContractRevisionId();
    signContractRevision(outdatedRevisionId, TestUserEnum.CONSUMER_BLV_1, SignatureSlotCodeEnum.DATA_CONSUMER_01)
        .then().statusCode(200);
    insertConsentRequest(initial.id(), PRODUCER_UID, "OPENED");

    access.requestAs(TestUserEnum.PRODUCER_A)
        .pathParam("id", outdatedRevisionId)
        .when().get(PDF_PATH)
        .then()
        .statusCode(404);
  }

  /**
   * Ways of calling the endpoint on behalf of a data producer: as the producer itself or as a support user impersonating them.
   */
  enum ProducerAccess {
    DIRECT,
    IMPERSONATED_BY_SUPPORT;

    RequestSpecification requestAs(TestUserEnum producer) {
      return switch (this) {
        case DIRECT -> AuthTestUtils.requestAs(producer);
        case IMPERSONATED_BY_SUPPORT -> {
          loginAs(producer);
          yield AuthTestUtils.requestAs(TestUserEnum.SUPPORT).header(IMPERSONATION_HEADER, producer.getAgateLoginId());
        }
      };
    }
  }

  /**
   * Logs in as the given user so their user data is persisted, which is required before a support user can impersonate them.
   */
  private static void loginAs(TestUserEnum user) {
    AuthTestUtils.requestAs(user)
        .when().get(UserController.PATH + "/user-info")
        .then()
        .statusCode(200);
  }

  /**
   * Inserts a consent request in its own committed transaction so the running endpoint sees it. Uses native SQL to bypass the
   * auditing listener, which requires an authenticated user.
   */
  private void insertConsentRequest(UUID dataRequestId, String dataProducerUid, String stateCode) {
    QuarkusTransaction.requiringNew().run(() -> entityManager.createNativeQuery("""
            INSERT INTO consent_request
              (id, archived, created_at, modified_at, data_producer_uid, data_producer_bur, state_code, data_request_id)
            VALUES
              (:id, false, NOW(), NOW(), :uid, NULL, :stateCode, :dataRequestId)
            """)
        .setParameter("id", UUID.randomUUID())
        .setParameter("uid", dataProducerUid)
        .setParameter("stateCode", stateCode)
        .setParameter("dataRequestId", dataRequestId)
        .executeUpdate());
  }
}
