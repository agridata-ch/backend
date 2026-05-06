package ch.agridata.user.dto;

import java.util.UUID;

/**
 * Represents an admin user with their system identifier and email address.
 *
 * @CommentLastReviewed 2026-05-07
 */
public record AdminUserDto(UUID userId, String email) {
}
