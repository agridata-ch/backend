package ch.agridata.agreement.service;

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
 * @CommentLastReviewed 2026-05-06
 */
@ApplicationScoped
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationApi api;
  private final UserApi userApi;

  public void queueDataRequestInReview() {
    List<RecipientRequestDto> recipients = userApi.getAdminUserIds().stream().map(id -> new RecipientRequestDto(id, null)).toList();
    api.queueNotification(recipients, EventTypeCodeEnum.DATA_REQUEST_READY_FOR_REVIEW, Map.of());
  }
}
