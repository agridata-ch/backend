package ch.agridata.agreement.dto;

import ch.agridata.common.utils.ValidationSchemaGenerator;
import ch.agridata.common.validation.RichText;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Defines the purpose of a data request in multiple languages. It validates and documents intent across internationalized contexts.
 * The purpose is rich text and may only contain the tags supported by the WYSIWYG editor.
 *
 * @CommentLastReviewed 2026-11-09
 */
@Builder
public record DataRequestPurposeDto(
    @Schema(
        examples = {"Bewertung der Bodenqualität"}
    )
    @RichText
    @Size(max = 5000)
    @Size(min = 10, max = 5000, groups = ValidationSchemaGenerator.Submit.class)
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    String de,

    @Schema(
        examples = {"Évaluation de la qualité du sol"}
    )
    @RichText
    @Size(max = 5000)
    @Size(min = 10, max = 5000, groups = ValidationSchemaGenerator.Submit.class)
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    String fr,

    @Schema(
        examples = {"Valutazione della qualità del suolo"}
    )
    @RichText
    @Size(max = 5000)
    @Size(min = 10, max = 5000, groups = ValidationSchemaGenerator.Submit.class)
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    String it
) {
}

