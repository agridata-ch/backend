package ch.agridata.agreement.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Provides a view of a consent request with the most fundamental information.
 *
 * @CommentLastReviewed 2026-02-26
 */

@Schema(description = "Data transfer object representing a consent request")
@Builder
public record ConsentRequestFundamentalViewDto(

    @Schema(
        description = "Unique identifier of the consent request",
        examples = {"3fa85f64-5717-4562-b3fc-2c963f66afa6"}
    )
    @NotNull
    UUID id,

    @Schema(
        description = "Unique identifier of the corresponding data request",
        examples = {"3fa85f64-5717-4562-b3fc-2c963f66afb7"}
    )
    @NotNull
    UUID dataRequestId,

    @Schema(
        description = "UID of the data producer",
        examples = {"CHE123456789"}
    )
    String dataProducerUid,

    @Schema(
        description = "BUR of the data producer",
        examples = {"A123456"}
    )
    String dataProducerBur,

    @Schema(
        description = "Start of the data period that is granted by this consent",
        examples = {"2024-06-16"},
        type = SchemaType.STRING,
        format = "date"
    )
    LocalDate grantedDataPeriodFrom,

    @Schema(
        description = "End of the data period that is granted by this consent",
        examples = {"2025-06-16"},
        type = SchemaType.STRING,
        format = "date"
    )
    LocalDate grantedDataPeriodTo,

    @Schema(
        description = "State of the consent request",
        examples = {"GRANTED"}
    )
    ConsentRequestStateEnum stateCode

) {
}
