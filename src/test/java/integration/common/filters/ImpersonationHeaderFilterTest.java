package integration.common.filters;

import static ch.agridata.common.filters.ImpersonationHeaderFilter.IMPERSONATION_HEADER;
import static ch.agridata.user.controller.UserController.PATH;
import static integration.testutils.TestUserEnum.PRODUCER_032;
import static integration.testutils.TestUserEnum.SUPPORT;
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

    verify(agridataSecurityIdentity).setImpersonatedKtIdP(null);
  }

  @Test
  void givenImpersonationHeader_whenGetUserInfo_thenImpersonationActive() {
    // First log in as the producer so the user data is persisted in the database
    AuthTestUtils.requestAs(PRODUCER_032)
        .header("Content-Type", MediaType.APPLICATION_JSON)
        .when()
        .get(USER_INFO_PATH)
        .then()
        .statusCode(200);

    // Then act as the support user and impersonate the previously created producer
    AuthTestUtils.requestAs(SUPPORT)
        .header("Content-Type", MediaType.APPLICATION_JSON)
        .header(IMPERSONATION_HEADER, PRODUCER_032.getKtIdP())
        .when()
        .get(USER_INFO_PATH)
        .then()
        .statusCode(200);

    verify(agridataSecurityIdentity).setImpersonatedKtIdP(PRODUCER_032.getKtIdP());
  }
}
