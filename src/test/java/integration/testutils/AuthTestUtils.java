package integration.testutils;

import io.quarkus.test.keycloak.client.KeycloakTestClient;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import java.util.List;

public class AuthTestUtils {

  private static final KeycloakTestClient keycloakClient = new KeycloakTestClient();
  private static final String TEST_USER_PASSWORD = "secret";
  private static final String CLIENT_ID = "agridata-ui";
  private static final String CLIENT_SECRET = "secret";

  public static RequestSpecification requestAs(TestUserEnum user) {
    return RestAssured.given()
        .auth().oauth2(getAccessToken(user));
  }

  private static String getAccessToken(TestUserEnum user) {
    return keycloakClient.getAccessToken(user.getUsername(), TEST_USER_PASSWORD, CLIENT_ID, CLIENT_SECRET, List.of("openid"));
  }
}

