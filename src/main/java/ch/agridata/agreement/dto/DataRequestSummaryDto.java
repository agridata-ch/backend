package ch.agridata.agreement.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Provides a reduced view of a data request
 *
 * @CommentLastReviewed 2026-08-12
 */

@Schema(description = "Reduced data transfer object representing a data request")
@Builder
public record DataRequestSummaryDto(
    @Schema(
        description = "Unique identifier of the data request",
        examples = {"3fa85f64-5717-4562-b3fc-2c963f66afb7"}
    )
    @NotNull
    UUID id,

    @Schema(
        description = "Shorter name of the data consumer defined by the data consumer used when displaying the request to the producer",
        examples = {"Bio Suisse"}
    )
    String dataConsumerDisplayName,

    @Schema(
        description = "Base64-encoded logo of the data consumer",
        examples = {"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA..."}
    )
    String dataConsumerLogoBase64,

    @Schema(
        description = "City of the data consumer",
        examples = {"Basel"}
    )
    String dataConsumerCity,

    @Schema(
        description = "Title of the data request"
    )
    DataRequestTitleDto title
) {
}
