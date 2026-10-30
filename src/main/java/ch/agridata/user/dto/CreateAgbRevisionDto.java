package ch.agridata.user.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Data transfer object for publishing a new agb revision. It is used as the request body
 * of the agb revision creation endpoint.
 *
 * @CommentLastReviewed 2026-10-27
 */
@Schema(description = "Data transfer object for publishing a new agb revision")
public record CreateAgbRevisionDto(
    @Schema(
        description = "Version of the agb revision as dot separated numbers. Must be unique and newer than the current version",
        examples = {"1.5"},
        required = true
    )
    @NotBlank
    @Size(max = 20)
    @Pattern(regexp = "^\\d{1,9}(\\.\\d{1,9}){1,9}$", message = "Version must consist of at least 2 dot separated numbers, e.g. 1.5")
    String version,

    @Schema(
        description = "Date and time from which users must have accepted the agb revision. "
            + "Must not be before today; a time earlier today is replaced with now. If null, no acceptance is required.",
        examples = {"2026-12-01T00:00:00"}
    )
    LocalDateTime enforceConsentFrom,

    @Schema(description = "Text of the agb revision in all languages", required = true)
    @NotNull
    @Valid
    AgbTextDto agbText
) {

}
