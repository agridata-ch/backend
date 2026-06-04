package ch.agridata.product.dto;

import ch.agridata.common.dto.TranslationDto;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
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
        description = "Code of data source system from which this product originates",
        examples = {"AGIS"}
    )
    String dataSourceSystemCode,

    @Schema(
        description = "Data source system from which this product originates"
    )
    DataSourceSystemDto dataSourceSystem,

    @Schema(
        description = "Rest client used to fetch the data product"
    )
    RestClientDto restClient,

    @Schema(
        description = "Template for the path where the data product is fetched from"
    )
    String restClientPathTemplate,

    @Schema(
        description = "Template of the request body for fetching the data product"
    )
    String restClientRequestTemplate,

    @Schema(
        description = "Http-Method used to fetch the data product"
    )
    RestClientMethodCodeEnum restClientMethodCode,

    @Schema(
        description = "Code of the flow used to fetch the data product"
    )
    FlowCodeEnum flowCode,

    @Schema(
        description = "How this product is categorized",
        examples = {"R01"}
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
        examples = {"DRAFT"}
    )
    @NotNull
    DataProductStateEnum stateCode,

    @Schema(
        description = "Template for the path for retrieving updates to the data product",
        examples = "v1/animal-updates/{{uid}}?since={{LAST_CHANGED_SINCE}}"
    )
    String restClientChangeDetectionPathTemplate
)

    implements Serializable {
}
