package ch.agridata.agreement.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Represents a complete contract revision.
 *
 * @CommentLastReviewed: 2026-03-17
 */

@Builder
public record ContractRevisionDto(
    @Schema(
        description = "Unique identifier of the data request",
        examples = {"24945B85-6E53-4059-8FB4-C0B0AC4FFD47"}
    )
    @NotNull
    UUID id,

    @Schema(
        description = "Identifier of the associated data request",
        examples = {"0D74F63F-9491-4B9B-80BA-0CA43A1A3A6F"}
    )
    @NotNull
    UUID dataRequestId,

    @Schema(
        description = "LegalName of the data consumer taken from the uid register",
        examples = {"Bio Suisse, Vereinigung Schweizer Biolandbau-Organisationen"}
    )
    @NotNull
    String dataConsumerName,


    @Schema(
        description = "City of the data consumer",
        examples = {"Basel"}
    )
    @NotNull
    String dataConsumerCity,

    @Schema(
        description = "Name of the data provider",
        examples = {"BLW"}
    )
    @NotNull
    String dataProviderName,

    @Schema(
        description = "Contextual information from the parent data request (e.g. logo, reduced metadata)"
    )
    @NotNull
    DataRequestContextDto dataRequestContext,

    @Schema(
        description = "List of signatures of the data consumer"
    )
    List<ContractRevisionSignatureDto> consumerSignatures
) {
}
