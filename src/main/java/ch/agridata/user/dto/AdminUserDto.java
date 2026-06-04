package ch.agridata.user.dto;

import ch.agridata.common.dto.SupportedLanguage;
import java.util.UUID;

/**
 * Represents an admin user with their system identifier, email address, and preferred language.
 *
 * @CommentLastReviewed 2026-06-03
 */
public record AdminUserDto(UUID userId, String email, SupportedLanguage language) {
}
