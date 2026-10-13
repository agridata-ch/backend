package ch.agridata.user.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Data transfer object representing an agb revision without its text. It is used as the response
 * to the agb revision overview endpoint.
 *
 * @CommentLastReviewed 2026-10-01
 */

@Schema(description = "Overview of an agb revision without the agb text")
public record AgbRevisionOverviewDto(
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

    @Schema(description = "Date and time until which the agb revision is valid. Empty if open-ended")
    LocalDateTime validTo,

    @Schema(description = "Date and time from which the agb revision enforces consent")
    LocalDateTime enforceConsentFrom
) {

}
