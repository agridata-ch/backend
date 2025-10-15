package ch.agridata.datatransfer.dto;

import java.util.UUID;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Represents the response to a data transfer request. It encapsulates the requested product data, the identifier of the transfer, and the
 * related consent request.
 *
 * @CommentLastReviewed 2025-08-25
 */
@Builder
@Schema(
    description = "Response containing the requested product data and metadata about the request."
)
public record DataTransferResponse(
    @Schema(description = "Contains the requested product data.")
    Object data,

    @Schema(description = "Unique identifier for the data transfer request (uuid)")
    String dataTransferRequestId,

    @Schema(description = "Identifier of the corresponding consent request")
    UUID consentRequestId
) {
}
