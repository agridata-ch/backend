package ch.agridata.agreement.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Provides a producer-facing view of a consent request. It includes identifiers, state information, request dates, and details of the
 * underlying data request.
 *
 * @CommentLastReviewed 2026-07-29
 * @deprecated Replaced by {@link ConsentRequestProducerViewV2Dto}
 */

@Schema(description = "Data transfer object representing a consent request")
@Builder
@Deprecated(since = "1.15")
public record ConsentRequestProducerViewDto(

    @Schema(
        description = "Unique identifier of the consent request",
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
        description = "BUR of the data producer; null for UID-based consent requests",
        examples = {"99910003"}
    )
    String dataProducerBur,

    @Schema(
        description = "State of the consent request",
        examples = {"GRANTED"}
    )
    ConsentRequestStateEnum stateCode,

    @Schema(
        description = "If the state should be shown as migrated",
        examples = {"true"}
    )
    boolean showStateAsMigrated,

    @Schema(
        description = "Date and time when the state was changed last",
        examples = {"2025-06-16T11:04:51.823889"},
        type = SchemaType.STRING,
        format = "date"
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
    DataRequestDto dataRequest

) {
}
