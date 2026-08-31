package ch.agridata.product.dto;

import ch.agridata.common.dto.LinkDto;
import ch.agridata.common.dto.TranslationDto;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Public, client-facing view of a data product. It exposes only the display information required to list publicly available data products
 * and deliberately omits internal integration details (REST client, URL, request/path templates).
 *
 * @CommentLastReviewed 2026-08-06
 */
@Builder
public record PublicDataProductDto(
    @Schema(
        description = "Unique identifier of the product",
        examples = {"3fa85f64-5717-4562-b3fc-2c963f66afb7"}
    )
    @NotNull
    UUID id,

    @Schema(
        description = "Data source system from which this product originates"
    )
    DataSourceSystemDto dataSourceSystem,

    @Schema(
        description = "Code of the flow used to fetch the data product"
    )
    FlowCodeEnum flowCode,

    @Schema(
        description = "Localized name of the product"
    )
    TranslationDto name,

    @Schema(
        description = "Description of the product"
    )
    TranslationDto description,

    @Schema(
        description = "List of relevant product links"
    )
    List<LinkDto> links,

    @Schema(
        description = "Extended product details"
    )
    TranslationDto extendedDescription,

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
    Boolean consentRequired
)

    implements Serializable {
}
