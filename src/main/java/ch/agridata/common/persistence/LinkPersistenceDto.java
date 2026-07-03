package ch.agridata.common.persistence;

import jakarta.validation.constraints.Size;

/**
 * Persistence DTO representing a single product link stored as JSON.
 * Contains both the URL and the display text.
 *
 * @CommentLastReviewed 2026-06-30
 */
public record LinkPersistenceDto(@Size(max = 2048) String url, @Size(max = 255) String displayText) {

}
