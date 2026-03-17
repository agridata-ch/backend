package ch.agridata.agreement.dto;

import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Represents a minimal definition of a data request with fields that are necessary for UI/UX purposes.
 *
 * @CommentLastReviewed 2026-03-17
 */

@Schema(description = "Data transfer object representing a minimal version of a data request for UI/UX use.")
@Builder
public record DataRequestContextDto(
    @Schema(
        description = "Base64-encoded logo of the data consumer",
        examples = {"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA..."}
    )
    String dataConsumerLogoBase64
) {
}
