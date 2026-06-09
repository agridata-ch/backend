package integration.agreement;

import static integration.agreement.DataRequestTestFactory.createDataRequest;
import static integration.agreement.DataRequestTestFactory.getDataRequestDto;
import static integration.agreement.DataRequestTestFactory.setStatusAs;
import static integration.agreement.DataRequestTestFactory.updateDataRequest;
import static integration.testutils.TestUserEnum.ADMIN;
import static integration.testutils.TestUserEnum.CONSUMER_BIO_SUISSE;
import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.agreement.dto.DataRequestStateEnum;
import ch.agridata.notification.dto.EventTypeCodeEnum;
import ch.agridata.notification.persistence.NotificationBatchRepository;
import ch.agridata.notification.persistence.NotificationBatchStatusEnum;
import integration.testutils.AuthTestUtils;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

/**
 * Integration tests verifying that notification batches are queued on data-request state transitions.
 *
 * @CommentLastReviewed 2026-05-06
 */
@QuarkusTest
@RequiredArgsConstructor
class DataRequestNotificationTest {

  private final NotificationBatchRepository notificationBatchRepository;

  @Test
  void givenValidDraftRequest_whenConsumerSubmits_thenPendingNotificationBatchIsQueued() {
    // Trigger admin user creation so UserService.getAdminUserIds() has at least one result
    AuthTestUtils.requestAs(ADMIN).when().get("/api/user/v1/user-info");

    String id = createDataRequest().then().statusCode(201).extract().path("id");
    updateDataRequest(id, getDataRequestDto().build()).then().statusCode(200);

    setStatusAs(id, DataRequestStateEnum.IN_REVIEW, CONSUMER_BIO_SUISSE).then().statusCode(200);

    var batches = notificationBatchRepository.findAll().list();
    assertThat(batches).anyMatch(b -> b.getTemplate()
        .getEventTypeCode()
        .equals(EventTypeCodeEnum.DATA_REQUEST_READY_FOR_REVIEW.name())
        && b.getStatusCode() == NotificationBatchStatusEnum.PENDING);
  }
}
