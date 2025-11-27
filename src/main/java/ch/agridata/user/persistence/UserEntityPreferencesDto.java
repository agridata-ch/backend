package ch.agridata.user.persistence;

import java.util.List;

/**
 * Data Transfer Object representing user preferences in persistence layer.
 *
 * @CommentLastReviewed 2025-11-19
 */
public record UserEntityPreferencesDto(Boolean mainMenuOpened, String activeUid, List<String> dismissedMigratedIds) {
}
