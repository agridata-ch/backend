package ch.agridata.user.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Data transfer object representing the AGB acceptance state of a single user. It is used as the response to the AGB acceptance overview
 * endpoint for admins and support.
 *
 * @CommentLastReviewed 2026-10-20
 */

@Schema(description = "AGB acceptance state of a user")
public record AgbAcceptanceOverviewDto(
    @Schema(
        description = "Unique identifier assigned by Agridata to a user (UUID format).",
        examples = {"3fa85f64-5717-4562-b3fc-2c963f66afa6"}
    )
    UUID userId,

    @Schema(
        description = "User's first name.",
        examples = {"Anna"}
    )
    String givenName,

    @Schema(
        description = "User's last name.",
        examples = {"Muster"}
    )
    String familyName,

    @Schema(
        description = "Email address of the user",
        examples = {"anna.muster@example.com"}
    )
    String email,

    @Schema(
        description = "UID of the company the user belongs to.",
        examples = {"CHE123456789"}
    )
    String uid,

    @Schema(
        description = "Date and time when the user last accepted the AGB.",
        examples = {"2026-07-20T14:35:00"}
    )
    LocalDateTime lastAcceptedAgbDate,

    @Schema(
        description = "Version of the AGB revision the user last accepted.",
        examples = {"1.4"}
    )
    String lastAcceptedAgbVersion,

    @Schema(description = "AGB acceptance status: ACCEPTED if no acceptance is pending, TRANSITION_PERIOD while "
        + "the acceptance deadline lies in the future, PENDING once it has passed.")
    AgbAcceptanceStatusEnum status
) {

}
