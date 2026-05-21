package ch.agridata.notification.dto;

import java.util.List;
import java.util.UUID;

/**
 * Request payload to mark one or more inbox entries as read.
 *
 * @CommentLastReviewed 2026-04-22
 */
public record MarkAsReadRequestDto(
    List<UUID> inboxIds
) {

}
