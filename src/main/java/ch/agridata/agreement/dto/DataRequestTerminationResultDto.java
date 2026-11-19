package ch.agridata.agreement.dto;

import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Reports the outcome of a data request termination run. It conveys the number of data requests that were terminated, the number that
 * failed and how long the run took.
 *
 * @CommentLastReviewed 2026-11-10
 */

@Schema(description = "Data transfer object representing the result of a data request termination run")
@Builder
public record DataRequestTerminationResultDto(

    @Schema(description = "Number of data requests that were terminated during this run", examples = {"3"})
    int terminatedCount,

    @Schema(description = "Number of data requests that could not be terminated during this run", examples = {"0"})
    int failedCount,

    @Schema(description = "Duration of the run in milliseconds", examples = {"1234"})
    long durationMs
) {
}
