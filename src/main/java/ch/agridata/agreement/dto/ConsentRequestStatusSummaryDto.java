package ch.agridata.agreement.dto;

import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Provides KPI counts (total/open/granted/declined) of consent requests for a data request, broken down by UID/BUR mode.
 *
 * @CommentLastReviewed 2026-09-14
 */
@Schema(description = "KPI summary of consent requests for a data request, broken down by mode (UID/BUR)")
@Builder
public record ConsentRequestStatusSummaryDto(
    StateCountsDto uid,

    @Schema(description = "Null when the data request has no BUR-based consent requests")
    StateCountsDto bur
) {

  /**
   * Provides counts of consent requests by status, for a single mode (UID or BUR).
   *
   * @CommentLastReviewed 2026-09-14
   */
  @Schema(description = "Counts of consent requests by status, for a single mode (UID or BUR)")
  @Builder
  public record StateCountsDto(long total, long open, long granted, long declined) {
  }
}
