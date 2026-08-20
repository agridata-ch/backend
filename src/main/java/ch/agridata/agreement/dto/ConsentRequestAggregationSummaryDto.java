package ch.agridata.agreement.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Provides a reduced producer-facing summary of an aggregation of consent requests, containing only the fields needed for list display.
 *
 * @CommentLastReviewed 2026-08-12
 */
@Schema(description = "Reduced data transfer object representing an aggregation of consent requests, for list display")
@Builder
public record ConsentRequestAggregationSummaryDto(
    @Schema(
        description = "Unique identifier of the aggregation. Corresponds to the underlying data request ID.",
        examples = {"3fa85f64-5717-4562-b3fc-2c963f66afa6"}
    )
    @NotNull
    UUID id,

    @Schema(
        description = "Aggregated state derived from the states of the underlying consent requests"
    )
    ConsentRequestAggregationStateEnum stateCode,

    @Schema(
        description = "Date when the latest consent request was made",
        examples = {"2025-05-19"},
        type = SchemaType.STRING,
        format = "date"
    )
    LocalDate requestDate,

    @Schema(
        description = "Indicates whether the aggregated state originates from migrated consent requests",
        examples = {"true"}
    )
    boolean showStateAsMigrated,

    @Schema(
        description = "Date and time when the state was changed last",
        examples = {"2025-06-16T11:04:51.823889"},
        type = SchemaType.STRING,
        format = "date-time"
    )
    LocalDateTime lastStateChangeDate,

    @Schema(
        description = "Reduced details of the underlying data request"
    )
    DataRequestSummaryDto dataRequest,

    @Schema(
        description = "List of consent requests included in this aggregation"
    )
    List<ConsentRequestStateDto> consentRequests
) {
  /**
   * Provides the state of a consent request.
   *
   * @CommentLastReviewed 2026-08-12
   */
  public record ConsentRequestStateDto(
      @Schema(
          description = "Unique identifier of the consent request",
          examples = {"3fa85f64-5717-4562-b3fc-2c963f66afa6"}
      )
      @NotNull
      UUID id,

      @Schema(
          description = "State of the consent request",
          examples = {"GRANTED"}
      )
      ConsentRequestStateEnum stateCode
  ) {
  }
}
