package ch.agridata.product.dto;

import ch.agridata.common.dto.TranslationDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;

/**
 * Represents a data source system with identifiers, code, and name. It provides the essential
 * structure for client-facing product listings.
 *
 * @CommentLastReviewed 2026-02-06
 */
@Builder
public record DataSourceSystemDto(
    @Schema(description = "Unique identifier of the source")
    @NotNull
    UUID id,

    @Schema(description = "Stable technical code of the source", examples = {"AGIS"})
    String code,

    @Schema(description = "Display name of the data source")
    TranslationDto name,

    @Schema(description = "Reference to the data provider")
    @NotNull
    DataProviderDto dataProvider
) {

}
