package ch.agridata.product.dto;

import ch.agridata.common.dto.TranslationDto;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Compact public view of a data product used in the paginated catalog listing. It contains only the fields rendered on a listing card.
 * Technical information and further details are served by the detail endpoint.
 *
 * @CommentLastReviewed 2026-10-26
 */
@Builder
public record PublicDataProductSummaryDto(
    @Schema(
        description = "Unique identifier of the product",
        examples = {"3fa85f64-5717-4562-b3fc-2c963f66afb7"}
    )
    @NotNull
    UUID id,

    @Schema(
        description = "Data source system from which this product originates"
    )
    PublicDataSourceSystemSummaryDto dataSourceSystem,

    @Schema(
        description = "Localized name of the product"
    )
    TranslationDto name,

    @Schema(
        description = "Description of the product"
    )
    TranslationDto description,

    @Schema(
        description = "Timestamp indicating when the product was marked as deprecated.",
        examples = {"2026-03-06T00:00:00"}
    )
    LocalDateTime deprecatedSince,

    @Schema(
        description = "State of the data product",
        implementation = DataProductStateEnum.class,
        examples = {"ACTIVE"}
    )
    @NotNull
    DataProductStateEnum stateCode,

    @Schema(
        description = "If a consent is required for this data product",
        examples = "true"
    )
    @NotNull
    Boolean consentRequired,

    @Schema(
        description = "If a payment is required for this data product",
        examples = "true"
    )
    @NotNull
    Boolean paymentRequired
)

    implements Serializable {
}
