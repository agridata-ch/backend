package ch.agridata.user.dto;

import ch.agridata.common.utils.ValidationSchemaGenerator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * This Dto represents the agb text in 3 languages.
 *
 * @CommentLastReviewed 2026-07-16
 */
@Builder
public record AgbTextDto(
    @Schema(
        examples = {"<h1>AGB</h2>"}
    )
    @NotBlank
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    @Size(max = 50000)
    String de,
    @Schema(
        examples = {"<h1>CG</h2>"}
    )
    @NotBlank
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    @Size(max = 50000)
    String fr,
    @Schema(
        examples = {"<h1>CG</h2>"}
    )
    @NotBlank
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    @Size(max = 50000)
    String it
) {
}
