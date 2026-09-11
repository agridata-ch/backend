package ch.agridata.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.openapitools.jackson.nullable.JsonNullable;

/**
 * Validator for {@link Absent}: a field is absent when the JSON did not contain the property
 * ({@link JsonNullable#isPresent()} is {@code false}) or when the wrapper itself is {@code null}.
 *
 * @CommentLastReviewed 2026-09-11
 */
public class AbsentValidator implements ConstraintValidator<Absent, JsonNullable<?>> {

  @Override
  public boolean isValid(JsonNullable<?> value, ConstraintValidatorContext context) {
    return value == null || !value.isPresent();
  }
}
