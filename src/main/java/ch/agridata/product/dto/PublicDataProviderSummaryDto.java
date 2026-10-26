package ch.agridata.product.dto;

import ch.agridata.common.dto.TranslationDto;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Compact public view of a data provider, containing its identifier and display name.
 *
 * @CommentLastReviewed 2026-10-26
 */
@Builder
public record PublicDataProviderSummaryDto(
    @Schema(description = "Unique identifier of the provider")
    @NotNull
    UUID id,

    @Schema(description = "Display name of the provider")
    TranslationDto name
) {

}
