package ch.agridata.notification.api;

import ch.agridata.notification.dto.EventTypeCodeEnum;
import ch.agridata.notification.dto.RecipientRequestDto;
import java.util.List;
import java.util.Map;

/**
 * Cross-module API for the notification module. Allows other modules to queue notifications and manage inbox entries.
 *
 * @CommentLastReviewed 2026-04-22
 */
public interface NotificationApi {

  /**
   * Queues a notification batch for the given list of recipients by event type and generic placeholders.
   */
  void queueNotification(List<RecipientRequestDto> recipients, EventTypeCodeEnum eventTypeCode, Map<String, String> genericPlaceholders);
}
