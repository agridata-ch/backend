package ch.agridata.user.dto;

import java.util.List;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Data Transfer Object representing user preferences.
 *
 * @CommentLastReviewed 2025-11-19
 */
@Builder
public record UserPreferencesDto(
    @Schema(
        description = "If the main menu is opened or closed.",
        examples = {"true"}
    )
    Boolean mainMenuOpened,
    @Schema(
        description = "the active uid of the producer",
        examples = {"CHE123456789"}
    )
    String activeUid,
    @Schema(
        description = "consent request ids for which we dont want to show migration notices anymore",
        examples = {"a5b1d2e3-4f6a-2b7c-6d0e-9f1a3b5c7d9e"}
    )
    List<String> dismissedMigratedIds
) {
}
