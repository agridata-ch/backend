package ch.agridata.agreement.dto;

import java.time.LocalDate;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Reports the outcome of a manually triggered consent request cleanup. It conveys the evaluated
 * time window, the number of terminated consent requests and how long the run took.
 *
 * @CommentLastReviewed 2026-09-07
 */

@Schema(description = "Data transfer object representing the result of a consent request cleanup run")
@Builder
public record ConsentRequestCleanupResultDto(

    @Schema(
        description = "First day of the evaluated time window (inclusive)",
        examples = {"2026-09-05"}
    )
    LocalDate fromInclusive,

    @Schema(
        description = "Last day of the evaluated time window (inclusive)",
        examples = {"2026-09-06"}
    )
    LocalDate toInclusive,

    @Schema(
        description = "Number of consent requests that were terminated during this run",
        examples = {"8"}
    )
    long terminatedConsentRequestCount,

    @Schema(
        description = "Duration of the cleanup run in milliseconds",
        examples = {"1234"}
    )
    long durationMs
) {
}
