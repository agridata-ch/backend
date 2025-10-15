package ch.agridata.agreement.dto;

import ch.agridata.common.utils.ValidationSchemaGenerator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Defines the purpose of a data request in multiple languages. It validates and documents intent across internationalized contexts.
 *
 * @CommentLastReviewed 2025-08-25
 */
@Builder
public record DataRequestPurposeDto(
    @Schema(
        examples = {"Bewertung der Bodenqualität"}
    )
    @Size(max = 1000)
    @Size(min = 10, max = 1000, groups = ValidationSchemaGenerator.Submit.class)
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    String de,

    @Schema(
        examples = {"Évaluation de la qualité du sol"}
    )
    @Size(max = 1000)
    @Size(min = 10, max = 1000, groups = ValidationSchemaGenerator.Submit.class)
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    String fr,

    @Schema(
        examples = {"Valutazione della qualità del suolo"}
    )
    @Size(max = 1000)
    @Size(min = 10, max = 1000, groups = ValidationSchemaGenerator.Submit.class)
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    String it
) {
}

