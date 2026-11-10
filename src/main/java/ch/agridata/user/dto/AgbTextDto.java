package ch.agridata.user.dto;

import ch.agridata.common.validation.RichText;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * This Dto represents the agb text in 3 languages. The agb text is rich text and may only contain the tags supported by the WYSIWYG
 * editor plus the headings h1 to h3.
 *
 * @CommentLastReviewed 2026-11-09
 */
@Builder
public record AgbTextDto(
    @Schema(
        examples = {"<h1>AGB</h1>"}
    )
    @RichText(additionalTags = {"h1", "h2", "h3"})
    @Size(max = 50000)
    String de,
    @Schema(
        examples = {"<h1>CG</h1>"}
    )
    @RichText(additionalTags = {"h1", "h2", "h3"})
    @Size(max = 50000)
    String fr,
    @Schema(
        examples = {"<h1>CG</h1>"}
    )
    @RichText(additionalTags = {"h1", "h2", "h3"})
    @Size(max = 50000)
    String it
) {
}
