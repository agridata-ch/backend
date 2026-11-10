package ch.agridata.product.dto;

import ch.agridata.common.utils.ValidationSchemaGenerator;
import ch.agridata.common.validation.RichText;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Represents the multilingual pricing basis of a DataProduct. The pricing basis is rich text and may only contain the tags supported
 * by the WYSIWYG editor.
 *
 * @CommentLastReviewed 2026-11-09
 */

@Builder
public record DataProductPricingBasisDto(
    @RichText
    @Size(max = 1000)
    @Size(min = 10, max = 1000, groups = ValidationSchemaGenerator.Submit.class)
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    @Schema(
        description = "German pricing basis of the data product",
        examples = {"<p>Gebühren pro Abruf</p>"}
    )
    String de,

    @RichText
    @Size(max = 1000)
    @Size(min = 10, max = 1000, groups = ValidationSchemaGenerator.Submit.class)
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    @Schema(
        description = "French pricing basis of the data product",
        examples = {"<p>Redevances par appel</p>"}
    )
    String fr,

    @RichText
    @Size(max = 1000)
    @Size(min = 10, max = 1000, groups = ValidationSchemaGenerator.Submit.class)
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    @Schema(
        description = "Italian pricing basis of the data product",
        examples = {"<p>Tasse per chiamata</p>"}
    )
    String it
) {
}
