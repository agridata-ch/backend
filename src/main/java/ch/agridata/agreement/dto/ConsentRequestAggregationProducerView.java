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
 * Provides a producer-facing aggregated view of consent requests. It groups consent requests by their underlying data request and
 * includes aggregated state information, timestamps, and detailed consent request entries.
 *
 * @CommentLastReviewed 2026-02-04
 */

@Schema(description = "Data transfer object representing an aggregation of consent requests")
@Builder
public record ConsentRequestAggregationProducerView(
    @Schema(
        description = "Unique identifier of the aggregation. Corresponds to the underlying data request ID.",
        examples = {"3fa85f64-5717-4562-b3fc-2c963f66afa6"}
    )
    @NotNull
    UUID id,

    @Schema(
        description = "UID of the data producer",
        examples = {"CHE123456789"}
    )
    String dataProducerUid,

    @Schema(
        description = "Aggregated state derived from the states of the underlying consent requests"
    )
    ConsentRequestAggregationStateEnum stateCode,

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
        description = "Date when the request was made",
        examples = {"2025-05-19"},
        type = SchemaType.STRING,
        format = "date"
    )
    LocalDate requestDate,

    @Schema(
        description = "Details of the underlying data request",
        implementation = DataRequestDto.class
    )
    DataRequestDto dataRequest,
    List<ConsentRequestProducerViewDto> consentRequests
) {
}
