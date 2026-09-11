package ch.agridata.common.validation;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.valueextraction.Unwrapping;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;

/**
 * Unit tests for {@link Absent}. They pin the distinction the annotation exists for: an omitted field passes, a field present with an
 * explicit {@code null} does not. The second case is what {@code @Null} could not express, because the registered
 * {@code JsonNullable} value extractor unwraps by default and hands {@code @Null} the wrapped {@code null}.
 *
 * @CommentLastReviewed 2026-09-11
 */
class AbsentValidatorTest {

  private static Validator validator;

  @BeforeAll
  static void setUp() {
    // The extractor is discovered through CDI at runtime, so it has to be registered by hand here; without it these tests would pass
    // trivially, proving nothing about the Unwrapping.Skip payload.
    validator = Validation.byDefaultProvider().configure()
        .addValueExtractor(new JsonNullableValueExtractor())
        .buildValidatorFactory()
        .getValidator();
  }

  @Test
  void givenUndefinedValue_whenValidated_thenNoViolation() {
    assertThat(validator.validate(new Holder(JsonNullable.undefined(), JsonNullable.undefined(), JsonNullable.undefined()))).isEmpty();
  }

  @Test
  void givenNullReference_whenValidated_thenNoViolation() {
    // DTOs built directly in tests leave an unset field as a plain null rather than JsonNullable.undefined().
    assertThat(validator.validate(new Holder(null, null, JsonNullable.undefined()))).isEmpty();
  }

  @Test
  void givenExplicitNull_whenValidated_thenViolation() {
    var violations = validator.validate(new Holder(JsonNullable.of(null), JsonNullable.undefined(), JsonNullable.undefined()));

    assertThat(violations).singleElement()
        .satisfies(violation -> assertThat(violation.getPropertyPath()).hasToString("guarded"));
  }

  @Test
  void givenPresentValue_whenValidated_thenViolation() {
    var violations = validator.validate(new Holder(JsonNullable.of("a value"), JsonNullable.undefined(), JsonNullable.undefined()));

    assertThat(violations).singleElement()
        .satisfies(violation -> assertThat(violation.getPropertyPath()).hasToString("guarded"));
  }

  @Test
  void givenPresentValueOnUnguardedField_whenValidated_thenNoViolation() {
    assertThat(validator.validate(new Holder(JsonNullable.undefined(), JsonNullable.of("a value"), JsonNullable.undefined()))).isEmpty();
  }

  @Test
  void givenOmittedField_whenValidatedAgainstNotNull_thenNoViolation() {
    // Control case for the unwrapping the value extractor performs: @NotNull never sees an omitted field...
    assertThat(validator.validate(new Holder(JsonNullable.undefined(), JsonNullable.undefined(), JsonNullable.undefined()))).isEmpty();
  }

  @Test
  void givenExplicitNullField_whenValidatedAgainstNotNull_thenViolation() {
    // ...but it does see a present null, which is what protects the primitive-backed booleans on DataProductUpdateDto.
    var violations = validator.validate(new Holder(JsonNullable.undefined(), JsonNullable.undefined(), JsonNullable.of(null)));

    assertThat(violations).singleElement()
        .satisfies(violation -> assertThat(violation.getPropertyPath()).hasToString("required"));
  }

  /**
   * Minimal record exercising a guarded, an unguarded and a {@code @NotNull} field side by side.
   *
   * @CommentLastReviewed 2026-09-11
   */
  private record Holder(
      @Absent(payload = Unwrapping.Skip.class)
      JsonNullable<@Valid String> guarded,

      JsonNullable<String> unguarded,

      @NotNull
      JsonNullable<String> required
  ) {
  }
}
