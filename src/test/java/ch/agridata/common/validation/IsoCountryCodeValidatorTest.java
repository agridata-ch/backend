package ch.agridata.common.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for {@link IsoCountryCodeValidator}.
 *
 * @CommentLastReviewed 2026-09-16
 */
class IsoCountryCodeValidatorTest {

  private final IsoCountryCodeValidator validator = new IsoCountryCodeValidator();

  @ParameterizedTest
  @ValueSource(strings = {"CH", "DE", "FR", "AT", "ES"})
  void givenValidIsoCountryCode_whenValidated_thenValid(String value) {
    assertThat(validator.isValid(value, null)).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {"XX", "ZZ", "ch", "de", "Switzerland", "C", "CHE", ""})
  void givenInvalidIsoCountryCode_whenValidated_thenInvalid(String value) {
    assertThat(validator.isValid(value, null)).isFalse();
  }

  @Test
  void givenNull_whenValidated_thenValid() {
    // A null value is delegated to @NotNull; this constraint only judges the shape of a present value.
    assertThat(validator.isValid(null, null)).isTrue();
  }
}
