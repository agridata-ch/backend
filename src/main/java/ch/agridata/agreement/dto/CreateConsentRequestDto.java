package ch.agridata.agreement.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;

/**
 * Used to create consent requests
 *
 * @CommentLastReviewed 2025-10-23
 */
@Builder
public record CreateConsentRequestDto(@NotNull UUID dataRequestId, @NotNull String uid) {
}
