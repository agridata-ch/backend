package ch.agridata.notification.dto;

import ch.agridata.common.dto.SupportedLanguage;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import java.util.UUID;

/**
 * Represents a single notification recipient identified by a system user ID, an e-mail address, or both.
 * At least one of {@code userId} or {@code email} must be provided. When {@code email} is provided,
 * {@code language} is mandatory so the email can be sent in the recipient's preferred language.
 *
 * @CommentLastReviewed 2026-06-03
 */
public record RecipientRequestDto(
    UUID userId,
    @Email String email,
    SupportedLanguage language
) {

  @AssertTrue(message = "At least one of userId or email+language must be provided")
  public boolean isValid() {
    return userId != null || (email != null && !email.isBlank() && language != null);
  }
}
