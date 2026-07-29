package ch.agridata.user.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Represents a BUR (local farm unit). It contains the identifier, farm type code, the owning UID and the start date of
 * the UID-to-BUR relation.
 *
 * @CommentLastReviewed 2025-08-25
 */

@Schema(description = "Data transfer object representing a bur")
@Builder
public record BurDto(

    String uid,
    String bur,
    FarmTypeEnum farmTypeCode,
    LocalDateTime relationSince

) {
}
