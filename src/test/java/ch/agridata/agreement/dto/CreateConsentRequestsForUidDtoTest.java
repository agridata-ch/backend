package ch.agridata.agreement.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for {@link CreateConsentRequestsForUidDto}. Verifies that every BUR consists of 8 to 9 alphanumeric characters.
 *
 * @CommentLastReviewed 2026-10-16
 */
class CreateConsentRequestsForUidDtoTest {

  private static final String VALID_UID = "CHE101000001";

  private static ValidatorFactory validatorFactory;
  private static Validator validator;

  @BeforeAll
  static void setUpValidator() {
    validatorFactory = Validation.buildDefaultValidatorFactory();
    validator = validatorFactory.getValidator();
  }

  @AfterAll
  static void tearDownValidator() {
    validatorFactory.close();
  }

  @ParameterizedTest
  @ValueSource(strings = {"A12345678", "B12345678", "12345678", "a1234567b", "A1234567"})
  void givenAlphanumericBurOfEightToNineCharacters_whenValidate_thenNoViolations(String bur) {
    var dto = new CreateConsentRequestsForUidDto(VALID_UID, List.of(bur));

    assertThat(validator.validate(dto)).isEmpty();
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "A", "A123456", "A123456789", "A-1234567", "A 1234567", "A1234567.", "Ä12345678"})
  void givenMalformedBur_whenValidate_thenBursViolation(String bur) {
    var dto = new CreateConsentRequestsForUidDto(VALID_UID, List.of(bur));

    assertThat(validator.validate(dto)).singleElement()
        .satisfies(v -> assertThat(v.getPropertyPath().toString()).startsWith("burs"));
  }
}
