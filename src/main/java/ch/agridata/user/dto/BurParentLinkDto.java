package ch.agridata.user.dto;

import java.time.LocalDateTime;

/**
 * Links a child farm to its parent farm together with the start date of that parent-child relation.
 *
 * @CommentLastReviewed 2026-08-10
 */
public record BurParentLinkDto(String parentBer, LocalDateTime validSince) {
}
