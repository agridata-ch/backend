package ch.agridata.product.dto;

import ch.agridata.common.dto.TranslationDto;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Compact public view of a data source system, containing its identifier, display name and the data provider.
 *
 * @CommentLastReviewed 2026-10-26
 */
@Builder
public record PublicDataSourceSystemSummaryDto(
    @Schema(description = "Unique identifier of the source")
    @NotNull
    UUID id,

    @Schema(description = "Display name of the data source")
    TranslationDto name,

    @Schema(description = "Reference to the data provider")
    PublicDataProviderSummaryDto dataProvider
) {

}
