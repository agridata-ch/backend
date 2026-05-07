package ch.agridata.notification.dto;

import ch.agridata.common.dto.TranslationDto;

/**
 * A notification template with all placeholders already substituted so consumers
 * can work with finished strings instead of template + placeholder map.
 *
 * @CommentLastReviewed 2026-05-08
 */
public record ResolvedNotificationTextsDto(
    TranslationDto webappTitle,
    TranslationDto webappText,
    TranslationDto emailSubject,
    TranslationDto emailText,
    TranslationDto mobileText
) {
}
