package integration.testutils;

import static ch.agridata.common.utils.AuthenticationUtil.ADMIN_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PRODUCER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PROVIDER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.SUPPORT_ROLE;
import static io.restassured.http.ContentType.JSON;

import io.restassured.http.ContentType;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AccessTestUtils {

  private static final Map<String, TestUserEnum> ROLE_USER_MAPPING = Map.of(
      PRODUCER_ROLE, TestUserEnum.PRODUCER_B,
      CONSUMER_ROLE, TestUserEnum.CONSUMER_BIO_SUISSE,
      PROVIDER_ROLE, TestUserEnum.PROVIDER,
      SUPPORT_ROLE, TestUserEnum.SUPPORT,
      ADMIN_ROLE, TestUserEnum.ADMIN,
      "no role", TestUserEnum.GUEST
  );

  public static void assertForbiddenForAllExcept(HttpMethod httpMethod, String path, ContentType contentType,
                                                 String... allowedRoles) {
    Set<String> allowed = Set.of(allowedRoles);
    List<String> notAllowed = ROLE_USER_MAPPING.keySet().stream()
        .filter(user -> !allowed.contains(user))
        .toList();

    for (String role : notAllowed) {
      var user = ROLE_USER_MAPPING.get(role);
      int actualStatus = switch (httpMethod) {
        case GET -> AuthTestUtils.requestAs(user).when().get(path).getStatusCode();
        case POST -> AuthTestUtils.requestAs(user).contentType(contentType).when().post(path).getStatusCode();
        case PUT -> AuthTestUtils.requestAs(user).contentType(contentType).when().put(path).getStatusCode();
        case DELETE -> AuthTestUtils.requestAs(user).when().delete(path).getStatusCode();
      };

      if (actualStatus != 403) {
        throw new AssertionError(String.format(
            "Expected status 403 for user '%s' with role '%s' on %s %s, but got %d",
            user.getUsername(), role, httpMethod, path, actualStatus));
      }
    }
  }

  public static void assertForbiddenForAllExcept(HttpMethod httpMethod, String path,
                                                 String... allowedRoles) {
    assertForbiddenForAllExcept(httpMethod, path, JSON, allowedRoles);
  }

  public enum HttpMethod {
    GET,
    POST,
    PUT,
    DELETE
  }
}
