package integration.testutils;

import static io.restassured.http.ContentType.JSON;

import io.restassured.http.ContentType;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class AccessTestUtils {

  public static void assertForbiddenForAllExcept(HttpMethod httpMethod, String path, ContentType contentType,
                                                 TestUserEnum... allowedUsers) {
    Set<TestUserEnum> allowed = Set.of(allowedUsers);
    List<TestUserEnum> notAllowed = Arrays.stream(TestUserEnum.values())
        .filter(user -> !allowed.contains(user))
        .toList();

    for (TestUserEnum user : notAllowed) {
      int actualStatus = switch (httpMethod) {
        case GET -> AuthTestUtils.requestAs(user).when().get(path).getStatusCode();
        case POST -> AuthTestUtils.requestAs(user).contentType(contentType).when().post(path).getStatusCode();
        case PUT -> AuthTestUtils.requestAs(user).contentType(contentType).when().put(path).getStatusCode();
        case DELETE -> AuthTestUtils.requestAs(user).when().delete(path).getStatusCode();
      };

      if (actualStatus != 403) {
        throw new AssertionError(String.format(
            "Expected status 403 for user '%s' on %s %s, but got %d",
            user.getUsername(), httpMethod, path, actualStatus));
      }
    }
  }

  public static void assertForbiddenForAllExcept(HttpMethod httpMethod, String path,
                                                 TestUserEnum... allowedUsers) {
    assertForbiddenForAllExcept(httpMethod, path, JSON, allowedUsers);
  }

  public enum HttpMethod {
    GET,
    POST,
    PUT,
    DELETE
  }
}
