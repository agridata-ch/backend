package ch.agridata.agreement.dto;

import ch.agridata.common.dto.TranslationDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.Builder;

/**
 * Agreement-module representation of a data source system reference. Used in responses to avoid
 * direct coupling to the product module's DTO contracts.
 *
 * @CommentLastReviewed 2026-03-25
 */
@Builder
public record DataSourceSystemReferenceDto(
    @Schema(description = "Unique identifier of the source")
    UUID id,

    @Schema(description = "Stable technical code of the source", examples = {"AGIS"})
    String code,

    @Schema(description = "Display name of the data source")
    TranslationDto name,

    @Schema(description = "Reference to the data provider")
    DataProviderReferenceDto dataProvider
) {

}
