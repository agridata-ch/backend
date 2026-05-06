package ch.agridata.agreement.service;

import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.notification.api.NotificationApi;
import ch.agridata.notification.dto.EventTypeCodeEnum;
import ch.agridata.notification.dto.RecipientRequestDto;
import ch.agridata.user.api.UserApi;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

/**
 * Queues notifications for agreement-related events.
 *
 * @CommentLastReviewed 2026-05-08
 */
@ApplicationScoped
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationApi api;
  private final UserApi userApi;

  public void queueDataRequestInReview(DataRequestEntity request) {

    Map<String, String> placeholders = Map.of(
        "data_request_title_de", request.getTitle().de(),
        "data_request_title_fr", request.getTitle().fr(),
        "data_request_title_it", request.getTitle().it(),
        "dataConsumer", request.getDataConsumerDisplayName()
    );

    List<RecipientRequestDto> recipients = userApi.getAdminUsers()
        .stream()
        .map(admin -> new RecipientRequestDto(admin.userId(), admin.email()))
        .toList();
    api.queueNotification(recipients, EventTypeCodeEnum.DATA_REQUEST_READY_FOR_REVIEW, placeholders);
  }
}
