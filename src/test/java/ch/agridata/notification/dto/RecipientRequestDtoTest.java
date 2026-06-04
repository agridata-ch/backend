package ch.agridata.notification.dto;

import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.common.dto.SupportedLanguage;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link RecipientRequestDto}.
 * Verifies the {@code isValid()} constraint that requires at least one of {@code userId} or {@code email},
 * and the {@code isValid()} constraint that requires {@code language} when
 * {@code email} is set.
 *
 * @CommentLastReviewed 2026-06-03
 */
class RecipientRequestDtoTest {

  @Test
  void givenUserIdOnly_whenIsValid_thenTrue() {
    assertThat(new RecipientRequestDto(UUID.randomUUID(), null, null).isValid()).isTrue();
  }

  @Test
  void givenEmailOnly_whenIsValid_thenTrue() {
    assertThat(new RecipientRequestDto(null, "user@example.com", SupportedLanguage.DE).isValid()).isTrue();
  }

  @Test
  void givenBothUserIdAndEmail_whenIsValid_thenTrue() {
    assertThat(new RecipientRequestDto(UUID.randomUUID(), "user@example.com", SupportedLanguage.DE).isValid()).isTrue();
  }

  @Test
  void givenNullUserIdAndNullEmail_whenIsValid_thenFalse() {
    assertThat(new RecipientRequestDto(null, null, null).isValid()).isFalse();
  }

  @Test
  void givenNullUserIdAndBlankEmail_whenIsValid_thenFalse() {
    assertThat(new RecipientRequestDto(null, "   ", null).isValid()).isFalse();
  }

  @Test
  void givenNullUserIdAndEmptyEmail_whenIsValid_thenFalse() {
    assertThat(new RecipientRequestDto(null, "", null).isValid()).isFalse();
  }

  @Test
  void givenEmailWithLanguage_whenisValid_thenTrue() {
    assertThat(new RecipientRequestDto(null, "user@example.com", SupportedLanguage.FR).isValid()).isTrue();
  }

  @Test
  void givenEmailWithoutLanguage_whenisValid_thenFalse() {
    assertThat(new RecipientRequestDto(null, "user@example.com", null).isValid()).isFalse();
  }

  @Test
  void givenNoEmailWithoutLanguage_whenisValid_thenTrue() {
    assertThat(new RecipientRequestDto(UUID.randomUUID(), null, null).isValid()).isTrue();
  }
}
