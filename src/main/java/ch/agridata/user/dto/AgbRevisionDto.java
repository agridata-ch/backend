package ch.agridata.user.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Data transfer object representing an agb revision. It is used as the response
 * to the agb revision endpoint.
 *
 * @CommentLastReviewed 2026-07-16
 */

@Schema(description = "Data transfer object representing an agb revision")
@Builder
public record AgbRevisionDto(
    @Schema(
        description = "Unique identifier of the agb revision",
        examples = {"3fa85f64-5717-4562-b3fc-2c963f66afb7"}
    )
    UUID id,

    @Schema(
        description = "Version of the agb revision",
        examples = {"1.0"}
    )
    String version,

    @Schema(description = "Date and time from which the agb revision is valid")
    LocalDateTime validFrom,

    @Schema(description = "Date and time from which the agb revision enforces consent")
    LocalDateTime enforceConsentFrom,

    @Schema(description = "Data transfer object representing an agb text")
    AgbTextDto agbText

) {

}
