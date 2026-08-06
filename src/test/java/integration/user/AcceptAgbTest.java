package integration.user;

import static ch.agridata.auditing.api.ActionEnum.AGBS_ACCEPTED;
import static ch.agridata.auditing.api.EntityTypeEnum.AGB_REVISION;
import static integration.testutils.TestUserEnum.CONSUMER_BIO_SUISSE;
import static integration.testutils.TestUserEnum.PROVIDER_1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import ch.agridata.auditing.persistence.AuditLogEntity;
import ch.agridata.user.controller.UserController;
import ch.agridata.user.dto.UserInfoDto;
import integration.auditing.utils.AuditLogTestUtils;
import integration.testutils.AuthTestUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import jakarta.ws.rs.core.Response;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for the AGB acceptance endpoint.
 *
 * @CommentLastReviewed 2026-07-20
 */
@QuarkusTest
@RequiredArgsConstructor
class AcceptAgbTest {

  private static final UUID CURRENT_AGB_REVISION_ID = UUID.fromString("ec502832-e140-4dcf-8b9e-5252192d031a");

  private final AuditLogTestUtils auditLogTestUtils;

  private static String acceptPath(UUID agbRevisionId) {
    return UserController.PATH + "/agb-revisions/" + agbRevisionId + "/accept";
  }

  @Test
  void givenConsumer_whenAcceptAgb_thenUserStampedAndActionAudited() {
    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .when()
        .post(acceptPath(CURRENT_AGB_REVISION_ID))
        .then()
        .statusCode(RestResponse.StatusCode.NO_CONTENT);

    UserInfoDto userInfo = AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .when()
        .get(UserController.PATH + "/user-info")
        .then()
        .statusCode(200)
        .body("lastAcceptedAgbDate", notNullValue())
        .extract()
        .as(new TypeRef<UserInfoDto>() {
        });

    assertThat(userInfo.lastAcceptedAgbRevisionId()).isEqualTo(CURRENT_AGB_REVISION_ID);
    assertThat(userInfo.lastAcceptedAgbDate()).isNotNull();

    AuditLogEntity latest = auditLogTestUtils.getLatestAuditLogEntry();
    assertThat(latest.getActionCode()).isEqualTo(AGBS_ACCEPTED.name());
    assertThat(latest.getEntityTypeCode()).isEqualTo(AGB_REVISION.name());
    assertThat(latest.getActorTypeCode()).isEqualTo(AuditLogEntity.ActorTypeEnum.USER);
    assertThat(latest.getActorId()).isEqualTo(userInfo.userId().toString());
    assertThat(latest.getEntityId()).isNotNull();
  }

  @Test
  void givenProvider_whenAcceptAgb_thenCreated() {
    AuthTestUtils.requestAs(PROVIDER_1)
        .when()
        .post(acceptPath(CURRENT_AGB_REVISION_ID))
        .then()
        .statusCode(RestResponse.StatusCode.NO_CONTENT);
  }

  @Test
  void givenStaleRevisionId_whenAcceptAgb_thenConflict() {
    AuthTestUtils.requestAs(CONSUMER_BIO_SUISSE)
        .when()
        .post(acceptPath(UUID.randomUUID()))
        .then()
        .statusCode(Response.Status.CONFLICT.getStatusCode());
  }
}
