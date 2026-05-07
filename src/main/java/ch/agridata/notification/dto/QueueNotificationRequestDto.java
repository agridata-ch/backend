package ch.agridata.notification.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * Request payload to queue a notification for a list of recipients. Each recipient may be
 * identified by a system user ID, an e-mail address, or both.
 *
 * @CommentLastReviewed 2026-04-22
 */
public record QueueNotificationRequestDto(
    @NotEmpty List<@Valid RecipientRequestDto> recipients,
    @NotNull EventTypeCodeEnum eventTypeCode,
    Map<String, String> placeholders
) {

}
