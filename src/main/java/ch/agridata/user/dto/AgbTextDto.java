package ch.agridata.user.dto;

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
    @Size(max = 50000)
    String de,
    @Schema(
        examples = {"<h1>CG</h2>"}
    )
    @Size(max = 50000)
    String fr,
    @Schema(
        examples = {"<h1>CG</h2>"}
    )
    @Size(max = 50000)
    String it
) {
}
