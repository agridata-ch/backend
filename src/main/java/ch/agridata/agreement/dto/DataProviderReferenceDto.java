package ch.agridata.agreement.dto;

import ch.agridata.common.dto.TranslationDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.Builder;

/**
 * Agreement-module representation of a data provider reference. Used in responses to avoid
 * direct coupling to the product module's DTO contracts.
 *
 * @CommentLastReviewed 2026-03-25
 */
@Builder
public record DataProviderReferenceDto(
    @Schema(description = "Unique identifier of the provider")
    UUID id,

    @Schema(description = "Stable technical code of the provider", examples = {"BLW"})
    String code,

    @Schema(description = "Display name of the provider")
    TranslationDto name,

    @Schema(description = "UID of the data provider", examples = {"CHE123456789"})
    String uid
) {

}
