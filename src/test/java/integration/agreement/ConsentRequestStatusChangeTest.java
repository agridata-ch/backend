package integration.agreement;

import static ch.agridata.agreement.dto.ConsentRequestStateEnum.DECLINED;
import static ch.agridata.agreement.dto.ConsentRequestStateEnum.GRANTED;
import static ch.agridata.agreement.dto.ConsentRequestStateEnum.OPENED;
import static ch.agridata.auditing.api.ActionEnum.CONSENT_REQUEST_DECLINED;
import static ch.agridata.auditing.api.ActionEnum.CONSENT_REQUEST_GRANTED;
import static ch.agridata.auditing.api.ActionEnum.CONSENT_REQUEST_REOPENED;
import static ch.agridata.auditing.api.EntityTypeEnum.CONSENT_REQUEST;
import static integration.testutils.TestUserEnum.PRODUCER_032;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import ch.agridata.agreement.controller.ConsentRequestController;
import ch.agridata.agreement.dto.ConsentRequestProducerViewDto;
import ch.agridata.agreement.dto.ConsentRequestStateEnum;
import integration.auditing.utils.AuditLogTestUtils;
import integration.testutils.AuthTestUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class ConsentRequestStatusChangeTest {

  private final AuditLogTestUtils auditLogTestUtils;
  private final Flyway flyway;

  @BeforeEach
  void setUp() {
    // will make sure testdata is reset between tests
    flyway.migrate();
  }

  @Test
  void givenApplicationRunning_whenApiCalled_thenStatusOk() {
    AuthTestUtils.requestAs(PRODUCER_032).when().get(ConsentRequestController.PATH).then()
        .statusCode(200);
  }

  @Test
  void givenOpenedConsentRequest_whenUpdatedToGrantedAndUpdatedToDeclined_thenStatusOk() {
    var requestId = findConsentRequest(
        dto -> OPENED.equals(dto.stateCode()) && dto.lastStateChangeDate() == null).id();

    updateConsentRequestStatus(requestId, GRANTED, 204);
    updateConsentRequestStatus(requestId, DECLINED, 204);

    var updatedRequest = findConsentRequest(dto -> dto.id().equals(requestId));
    assertThat(updatedRequest.stateCode()).isEqualTo(DECLINED);
    assertThat(updatedRequest.lastStateChangeDate().toLocalDate()).isToday();
    assertThat(auditLogTestUtils.getLatestAuditLogEntry(1)).satisfies(log -> {
      assertThat(log.getEntityTypeCode()).isEqualTo(CONSENT_REQUEST.name());
      assertThat(log.getEntityId()).isEqualTo(updatedRequest.id());
      assertThat(log.getActionCode()).isEqualTo(CONSENT_REQUEST_GRANTED.name());
    });
    assertThat(auditLogTestUtils.getLatestAuditLogEntry()).satisfies(log -> {
      assertThat(log.getEntityTypeCode()).isEqualTo(CONSENT_REQUEST.name());
      assertThat(log.getEntityId()).isEqualTo(updatedRequest.id());
      assertThat(log.getActionCode()).isEqualTo(CONSENT_REQUEST_DECLINED.name());
    });
  }

  @Test
  void givenGrantedConsentRequest_whenUpdatedToOpened_thenErrorReturned() {
    var requestId = findConsentRequest(dto -> GRANTED.equals(dto.stateCode())).id();

    updateConsentRequestStatus(requestId, OPENED, 400);
    assertThat(auditLogTestUtils.getLatestAuditLogEntry()).isNull();
  }

  @Test
  void givenOpenedConsentRequest_whenUpdatedToGrantedAndRevertedToOpened_thenStatusOk() {
    var requestId = findConsentRequest(
        dto -> OPENED.equals(dto.stateCode()) && dto.lastStateChangeDate() == null).id();

    updateConsentRequestStatus(requestId, GRANTED, 204);
    updateConsentRequestStatus(requestId, OPENED, 204);

    var updatedRequest = findConsentRequest(dto -> dto.id().equals(requestId));
    assertThat(updatedRequest.stateCode()).isEqualTo(OPENED);
    assertThat(updatedRequest.lastStateChangeDate().toLocalDate()).isToday();
    assertThat(auditLogTestUtils.getLatestAuditLogEntry(1)).satisfies(log -> {
      assertThat(log.getEntityTypeCode()).isEqualTo(CONSENT_REQUEST.name());
      assertThat(log.getEntityId()).isEqualTo(updatedRequest.id());
      assertThat(log.getActionCode()).isEqualTo(CONSENT_REQUEST_GRANTED.name());
    });
    assertThat(auditLogTestUtils.getLatestAuditLogEntry()).satisfies(log -> {
      assertThat(log.getEntityTypeCode()).isEqualTo(CONSENT_REQUEST.name());
      assertThat(log.getEntityId()).isEqualTo(updatedRequest.id());
      assertThat(log.getActionCode()).isEqualTo(CONSENT_REQUEST_REOPENED.name());
    });
  }

  private ConsentRequestProducerViewDto findConsentRequest(Predicate<ConsentRequestProducerViewDto> filter) {
    return AuthTestUtils.requestAs(PRODUCER_032).accept(ContentType.JSON).when()
        .get(ConsentRequestController.PATH).then().statusCode(200)
        .extract()
        .as(new TypeRef<List<ConsentRequestProducerViewDto>>() {
        }).stream().filter(filter).findFirst().orElseThrow(NotFoundException::new);
  }

  private void updateConsentRequestStatus(UUID id, ConsentRequestStateEnum newStatus,
                                          int expectedStatusCode) {
    AuthTestUtils.requestAs(PRODUCER_032).contentType(ContentType.JSON)
        .body(String.format("\"%s\"", newStatus)).when()
        .put(ConsentRequestController.PATH + "/" + id + "/status")
        .then().statusCode(expectedStatusCode);
  }
}
