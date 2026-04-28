package ch.agridata.notification.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import java.util.UUID;

/**
 * Represents a single notification recipient identified by a system user ID, an e-mail address, or both.
 * At least one of {@code userId} or {@code email} must be provided.
 *
 * @CommentLastReviewed 2026-04-22
 */
public record RecipientRequestDto(
    UUID userId,
    @Email String email
) {

  @AssertTrue(message = "At least one of userId or email must be provided")
  public boolean isValid() {
    return userId != null || (email != null && !email.isBlank());
  }
}
