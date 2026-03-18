package ch.agridata.agreement.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Represents a signature on a contract revision.
 *
 * @CommentLastReviewed: 2026-03-19
 */

public record ContractRevisionSignatureDto(
    @Schema(
        description = "Slot in which the signature is stored",
        examples = {"DATA_CONSUMER_01"}
    )
    @NotNull
    SignatureSlotCodeEnum signatureSlotCode,
    @Schema(
        description = "ID of the user",
        examples = {"43FAB890-492E-4EEB-9B65-F4A4FB3DD74E"}
    )
    @NotNull
    UUID userId,
    @Schema(
        description = "ID of the user of the signature",
        examples = {"John Doe"}
    )
    @NotNull
    String name,
    @Schema(
        description = "Timestamp of when the signature was added",
        examples = {"2026-03-19T12:00:00"}
    )
    @NotNull
    LocalDateTime timestamp
) {
}
