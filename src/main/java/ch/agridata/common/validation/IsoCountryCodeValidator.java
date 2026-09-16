package ch.agridata.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Locale;
import java.util.Set;

/**
 * Validator for {@link IsoCountryCode}: a value is valid when it matches one of the codes returned by
 * {@link Locale#getISOCountries()}, case-sensitively.
 *
 * @CommentLastReviewed 2026-09-16
 */
public class IsoCountryCodeValidator implements ConstraintValidator<IsoCountryCode, String> {

  private static final Set<String> ISO_COUNTRIES = Set.of(Locale.getISOCountries());

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return value == null || ISO_COUNTRIES.contains(value);
  }
}
