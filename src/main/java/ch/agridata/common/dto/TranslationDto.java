package ch.agridata.common.dto;

import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * Represents multilingual translation content with fields for supported languages. It enforces length validation for consistency.
 *
 * @CommentLastReviewed 2025-08-25
 */

@Builder
public record TranslationDto(@Size(max = 4000) String de, @Size(max = 4000) String fr,
                             @Size(max = 4000) String it) {

}
