package ch.agridata.agreement.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Used to create the consent requests of a single data producer UID (and optionally its BURs) for a data request.
 *
 * @CommentLastReviewed 2026-09-24
 */

@Schema(description = "Data transfer object used to create consent requests for a data producer UID and optionally its BURs")
@Builder
public record CreateConsentRequestsForUidDto(

    @Schema(
        description = "UID of the data producer",
        examples = {"CHE101000001"}
    )
    @NotNull
    @Pattern(
        regexp = "^(?:CHE|ZZZ)\\d{9}$",
        message = "Invalid UID format. Expected format is 'CHE' or 'ZZZ' followed by 9 digits."
    )
    String uid,

    @Schema(
        description = "BURs of the data producer. May be empty. When provided, every BUR must belong to the UID in AGIS.",
        examples = {"[\"A12345678\", \"A12345679\"]"}
    )
    List<String> burs
) {
}