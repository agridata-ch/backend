package ch.agridata.notification.dto;

import ch.agridata.common.dto.TranslationDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a user-facing inbox notification entry.
 *
 * @CommentLastReviewed 2026-05-08
 */
public record InboxEntryDto(
    UUID id,
    TranslationDto title,
    TranslationDto text,
    UUID userId,
    @JsonProperty("isRead") boolean isRead,
    LocalDateTime createdAt,
    TargetTypeCodeEnum targetType,
    UUID targetId
) {

}
