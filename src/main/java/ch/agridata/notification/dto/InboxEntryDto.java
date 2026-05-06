package ch.agridata.notification.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a user-facing inbox notification entry.
 *
 * @CommentLastReviewed 2026-04-22
 */
public record InboxEntryDto(
    UUID id,
    UUID recipientId,
    UUID userId,
    boolean isRead,
    LocalDateTime createdAt
) {

}
