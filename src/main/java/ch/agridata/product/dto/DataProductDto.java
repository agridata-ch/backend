package ch.agridata.product.dto;

import ch.agridata.common.dto.TranslationDto;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.UUID;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Represents a data product with identifiers, source system, name, description, and source-specific product ID. It provides the essential
 * structure for client-facing product listings.
 *
 * @CommentLastReviewed 2025-08-25
 */
@Builder
public record DataProductDto(
    @Schema(
        description = "Unique identifier of the product",
        examples = {"3fa85f64-5717-4562-b3fc-2c963f66afb7"}
    )
    @NotNull
    UUID id,

    @Schema(
        description = "From which data source system this product originates",
        examples = {"AGIS"}
    )
    DataSourceSystemEnum dataSourceSystemCode,

    @Schema(
        description = "How this product is categorized",
        examples = {"R01"}
    )
    TranslationDto name,

    @Schema(
        description = "Description of the product"
    )
    TranslationDto description
)

    implements Serializable {
}
