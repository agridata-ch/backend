package integration.common.security;

import static ch.agridata.common.utils.AuthenticationUtil.PROVIDER_ROLE;
import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.user.controller.UserController;
import ch.agridata.user.dto.UserInfoDto;
import integration.testutils.AuthTestUtils;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

/**
 * Service account tokens lack the openid scope, so Keycloak rejects UserInfo requests for them. They must be authenticated
 * without UserInfo, see {@code ServiceAccountTenantConfigResolver}.
 */
@QuarkusTest
class ServiceAccountAuthenticationTest {

  private static final String SERVICE_ACCOUNT_CLIENT_ID = "provider-service-account";

  @Test
  void givenServiceAccountTokenWithoutOpenidScope_whenGetUserInfo_thenUserWithoutPersonalDataReturned() {
    var userInfo = AuthTestUtils.requestAsServiceAccount(SERVICE_ACCOUNT_CLIENT_ID)
        .when().get(UserController.PATH + "/user-info")
        .then().statusCode(200)
        .extract().as(UserInfoDto.class);

    assertThat(userInfo.agateLoginId()).isEqualTo(SERVICE_ACCOUNT_CLIENT_ID);
    assertThat(userInfo.rolesAtLastLogin()).contains(PROVIDER_ROLE);
    assertThat(userInfo.email()).isNull();
    assertThat(userInfo.givenName()).isNull();
  }
}
