package integration.user;

import static ch.agridata.user.controller.UserController.PATH;
import static ch.agridata.user.filters.ImpersonationHeaderFilter.IMPERSONATION_HEADER;
import static integration.testutils.TestUserEnum.ADMIN;
import static integration.testutils.TestUserEnum.PRODUCER_B;
import static integration.testutils.TestUserEnum.SUPPORT;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import ch.agridata.common.security.AgridataSecurityIdentity;
import integration.testutils.AuthTestUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ImpersonationHeaderFilterTest {
  @InjectSpy
  AgridataSecurityIdentity agridataSecurityIdentity;

  private static final String USER_INFO_PATH = PATH + "/user-info";

  @Test
  void givenNoImpersonationHeader_whenGetUserInfo_thenImpersonationNotActive() {
    AuthTestUtils.requestAs(SUPPORT)
        .header("Content-Type", MediaType.APPLICATION_JSON)
        .when()
        .get(USER_INFO_PATH)
        .then()
        .statusCode(200);

    verify(agridataSecurityIdentity).setImpersonatedAgateLoginId(null);
    verify(agridataSecurityIdentity).setImpersonatedKtIdP(null);
  }

  @Test
  void givenImpersonationHeader_whenGetUserInfo_thenImpersonationActive() {
    // First log in as the producer so the user data is persisted in the database
    AuthTestUtils.requestAs(PRODUCER_B)
        .header("Content-Type", MediaType.APPLICATION_JSON)
        .when()
        .get(USER_INFO_PATH)
        .then()
        .statusCode(200);

    // Then act as the support user and impersonate the previously created producer
    AuthTestUtils.requestAs(SUPPORT)
        .header("Content-Type", MediaType.APPLICATION_JSON)
        .header(IMPERSONATION_HEADER, PRODUCER_B.getAgateLoginId())
        .when()
        .get(USER_INFO_PATH)
        .then()
        .statusCode(200);

    verify(agridataSecurityIdentity).setImpersonatedAgateLoginId(PRODUCER_B.getAgateLoginId());
    verify(agridataSecurityIdentity).setImpersonatedKtIdP(PRODUCER_B.getKtIdP());
  }

  @Test
  void givenInvalidImpersonationHeader_whenGetUserInfo_thenExceptionThrown() {
    // First log in as the producer so the user data is persisted in the database
    AuthTestUtils.requestAs(ADMIN)
        .header("Content-Type", MediaType.APPLICATION_JSON)
        .when()
        .get(USER_INFO_PATH)
        .then()
        .statusCode(200);

    // Then act as the support user and try to impersonate the admin
    AuthTestUtils.requestAs(SUPPORT)
        .header("Content-Type", MediaType.APPLICATION_JSON)
        .header(IMPERSONATION_HEADER, ADMIN.getAgateLoginId())
        .when()
        .get(USER_INFO_PATH)
        .then()
        .statusCode(400);

    verify(agridataSecurityIdentity, never()).setImpersonatedAgateLoginId(anyString());
    verify(agridataSecurityIdentity, never()).setImpersonatedKtIdP(anyString());
    verify(agridataSecurityIdentity, atLeastOnce()).setImpersonatedAgateLoginId(null);
    verify(agridataSecurityIdentity, atLeastOnce()).setImpersonatedKtIdP(null);
  }
}
