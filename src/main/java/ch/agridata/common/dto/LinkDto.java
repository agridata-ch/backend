package ch.agridata.common.dto;

import ch.agridata.common.utils.ValidationSchemaGenerator;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * API DTO for a link.
 *
 * @CommentLastReviewed 2026-06-30
 */
@Builder
public record LinkDto(
    @Size(max = 2048)
    @Pattern(
        regexp = "^https?://.*$",
        message = "Must start with http:// or https://",
        groups = ValidationSchemaGenerator.Submit.class
    )
    String url,

    @Size(max = 255)
    String displayText
) {

}
