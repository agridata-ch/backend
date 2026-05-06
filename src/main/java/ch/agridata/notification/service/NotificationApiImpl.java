package ch.agridata.notification.service;

import ch.agridata.notification.api.NotificationApi;
import ch.agridata.notification.dto.EventTypeCodeEnum;
import ch.agridata.notification.dto.RecipientRequestDto;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Marks one or multiple inbox entries as read for a user, and queues notifications.
 * Implements {@link NotificationApi} for cross-module access.
 *
 * @CommentLastReviewed 2026-04-22
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class NotificationApiImpl implements NotificationApi {

  private final NotificationBatchService batchService;

  @Override
  public void queueNotification(
      List<RecipientRequestDto> recipients,
      EventTypeCodeEnum eventTypeCode,
      Map<String, String> genericPlaceholders
  ) {
    batchService.queueNotification(recipients, eventTypeCode, genericPlaceholders);
  }
}
