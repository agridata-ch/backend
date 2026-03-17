package ch.agridata.agreement.dto;

import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Represents an admin update request for the valid redirect URI regex of a data request.
 *
 * @CommentLastReviewed 2026-03-16
 */
public record DataRequestValidRedirectUriRegexUpdateDto(
    @Schema(
        description = "Regex used to validate redirect_uri values for this data request. Must compile as regex successfully.",
        examples = {"^https:\\/\\/([A-Za-z0-9-]+\\.)*bio-suisse\\.ch(\\/.*)?$"}
    )
    @Size(max = 255)
    String validRedirectUriRegex
) {
}
