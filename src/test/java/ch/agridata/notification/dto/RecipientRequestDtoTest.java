package ch.agridata.notification.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link RecipientRequestDto}.
 * Verifies the {@code isValid()} constraint that requires at least one of {@code userId} or {@code email}.
 *
 * @CommentLastReviewed 2026-05-05
 */
class RecipientRequestDtoTest {

  @Test
  void givenUserIdOnly_whenIsValid_thenTrue() {
    assertThat(new RecipientRequestDto(UUID.randomUUID(), null).isValid()).isTrue();
  }

  @Test
  void givenEmailOnly_whenIsValid_thenTrue() {
    assertThat(new RecipientRequestDto(null, "user@example.com").isValid()).isTrue();
  }

  @Test
  void givenBothUserIdAndEmail_whenIsValid_thenTrue() {
    assertThat(new RecipientRequestDto(UUID.randomUUID(), "user@example.com").isValid()).isTrue();
  }

  @Test
  void givenNullUserIdAndNullEmail_whenIsValid_thenFalse() {
    assertThat(new RecipientRequestDto(null, null).isValid()).isFalse();
  }

  @Test
  void givenNullUserIdAndBlankEmail_whenIsValid_thenFalse() {
    assertThat(new RecipientRequestDto(null, "   ").isValid()).isFalse();
  }

  @Test
  void givenNullUserIdAndEmptyEmail_whenIsValid_thenFalse() {
    assertThat(new RecipientRequestDto(null, "").isValid()).isFalse();
  }
}
