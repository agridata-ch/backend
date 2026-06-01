package integration.notification;

import static integration.testutils.TestUserEnum.PRODUCER_A;
import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.notification.controller.NotificationController;
import ch.agridata.notification.dto.InboxEntryDto;
import integration.testutils.AuthTestUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for the notification inbox endpoints.
 *
 * @CommentLastReviewed 2026-05-06
 */
@QuarkusTest
@RequiredArgsConstructor
class NotificationInboxTest {

  @Test
  void givenAuthenticatedProducer_whenGetInbox_thenReturns200() {
    var result = AuthTestUtils.requestAs(PRODUCER_A)
        .when()
        .get(NotificationController.PATH + "/inbox")
        .then()
        .statusCode(200)
        .extract()
        .as(new TypeRef<PageResponseDto<InboxEntryDto>>() {
        });

    assertThat(result).isNotNull();
    assertThat(result.items()).isNotNull();
  }

  @Test
  void givenAuthenticatedProducer_whenMarkAsRead_thenReturns204or200() {
    int status = AuthTestUtils.requestAs(PRODUCER_A).contentType(MediaType.APPLICATION_JSON).body("""
        {
          "inboxIds": ["d0000000-0000-0000-0000-000000000001"]
        }
        """).when().put(NotificationController.PATH + "/inbox/mark-as-read").getStatusCode();

    assertThat(status).isIn(200, 204);
  }

  @Test
  void givenAuthenticatedProducer_whenMarkAsUnread_thenReturns204or200() {
    int status = AuthTestUtils.requestAs(PRODUCER_A).contentType(MediaType.APPLICATION_JSON).body("""
        {
          "inboxIds": ["d0000000-0000-0000-0000-000000000001"]
        }
        """).when().put(NotificationController.PATH + "/inbox/mark-as-unread").getStatusCode();

    assertThat(status).isIn(200, 204);
  }

  @Test
  void givenNoToken_whenGetInbox_thenUnauthorized() {
    RestAssured.given().when().get(NotificationController.PATH + "/inbox").then().statusCode(401);
  }

  @Test
  void givenNoToken_whenMarkAsRead_thenUnauthorized() {
    RestAssured.given().contentType(MediaType.APPLICATION_JSON).body("""
        {
          "inboxIds": ["d0000000-0000-0000-0000-000000000001"]
        }
        """).when().put(NotificationController.PATH + "/inbox/mark-as-read").then().statusCode(401);
  }

  @Test
  void givenNoToken_whenMarkAsUnread_thenUnauthorized() {
    RestAssured.given().contentType(MediaType.APPLICATION_JSON).body("""
        {
          "inboxIds": ["d0000000-0000-0000-0000-000000000001"]
        }
        """).when().put(NotificationController.PATH + "/inbox/mark-as-unread").then().statusCode(401);
  }
}
