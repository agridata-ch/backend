package ch.agridata.product.dto;

import ch.agridata.common.utils.ValidationSchemaGenerator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Represents the multilangual name of a DataProduct.
 *
 * @CommentLastReviewed 2026-06-11
 */

@Builder
public record DataProductNameDto(
    @Size(max = 1000)
    @Size(min = 5, max = 255, groups = ValidationSchemaGenerator.Submit.class)
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    @Schema(
        description = "German name of the data product",
        examples = {
            "Tierschutz"
        }
    )
    String de,

    @Size(max = 1000)
    @Size(min = 5, max = 255, groups = ValidationSchemaGenerator.Submit.class)
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    @Schema(
        description = "French name of the data product",
        examples = {
            "Protection des animaux"
        }
    )
    String fr,

    @Size(max = 1000)
    @Size(min = 5, max = 255, groups = ValidationSchemaGenerator.Submit.class)
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    @Schema(
        description = "Italian name of the data product",
        examples = {
            "Protezione degli animali"
        }
    )
    String it

) {

}
