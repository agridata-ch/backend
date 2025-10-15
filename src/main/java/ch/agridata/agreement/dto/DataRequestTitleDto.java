package ch.agridata.agreement.dto;

import ch.agridata.common.utils.ValidationSchemaGenerator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Represents the multilingual title of a data request. It ensures clarity and accessibility across supported languages.
 *
 * @CommentLastReviewed 2025-08-25
 */
@Builder
public record DataRequestTitleDto(
    @Schema(
        examples = {"Anfrage zu Bodentypen und Bodenqualitätsdaten"}
    )
    @Size(max = 255)
    @Size(min = 5, max = 255, groups = ValidationSchemaGenerator.Submit.class)
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    String de,

    @Schema(
        examples = {"Demande sur les types de sols et les données de qualité du sol"}
    )
    @Size(max = 255)
    @Size(min = 5, max = 255, groups = ValidationSchemaGenerator.Submit.class)
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    String fr,

    @Schema(
        examples = {"Richiesta sui tipi di suolo e sui dati sulla qualità del suolo"}
    )
    @Size(max = 255)
    @Size(min = 5, max = 255, groups = ValidationSchemaGenerator.Submit.class)
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    String it
) {
}

