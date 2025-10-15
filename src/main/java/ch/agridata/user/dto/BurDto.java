package ch.agridata.user.dto;

import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Represents a BUR (local farm unit). It contains identifier and farm type code.
 *
 * @CommentLastReviewed 2025-08-25
 */

@Schema(description = "Data transfer object representing a bur")
@Builder
public record BurDto(

    String bur,
    FarmTypeEnum farmTypeCode

) {
}
