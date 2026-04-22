package ch.agridata.product.dto;

import ch.agridata.common.dto.TranslationDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;

/**
 * Represents a data provider with identifiers, code, and name. It provides the essential
 * structure for client-facing product listings.
 *
 * @CommentLastReviewed 2026-02-06
 */
@Builder
public record DataProviderDto(
    @Schema(description = "Unique identifier of the provider")
    @NotNull
    UUID id,

    @Schema(description = "Stable technical code of the provider", examples = {"BLW"})
    String code,

    @Schema(description = "Display name of the provider")
    TranslationDto name,

    @Schema(description = "UID of the data provider", examples = {"CHE123456789"})
    String uid
) {

}

