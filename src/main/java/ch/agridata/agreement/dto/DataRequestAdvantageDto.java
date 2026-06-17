package ch.agridata.agreement.dto;

import ch.agridata.common.utils.ValidationSchemaGenerator;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * DataRequestAdvantageDto is a data transfer object representing an advantage request
 * with multilingual support for German, French, and Italian text fragments.
 * Each text fragment must adhere to validation rules for minimum and maximum length.
 *
 * @CommentLastReviewed 2026-06-17
 */

@Builder
public record DataRequestAdvantageDto(
    @Schema(
        examples = {"Vermeiden von Mehreingaben"}
    )
    @Size(max = 255)
    @Size(min = 5, max = 255, groups = ValidationSchemaGenerator.Submit.class)
    String de,

    @Schema(
        examples = {"Éviter les saisies multiples"}
    )
    @Size(max = 255)
    @Size(min = 5, max = 255, groups = ValidationSchemaGenerator.Submit.class)
    String fr,

    @Schema(
        examples = {"Evitare inserimenti multipli"}
    )
    @Size(max = 255)
    @Size(min = 5, max = 255, groups = ValidationSchemaGenerator.Submit.class)
    String it
) {
}
