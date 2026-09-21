package ch.agridata.common.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Asserts that a {@link String} is a valid ISO 3166-1 alpha-2 country code (e.g. {@code CH}, {@code DE}). Any code recognized by
 * {@link java.util.Locale#getISOCountries()} is accepted; this only rejects malformed or non-existent codes, it does not restrict which
 * countries may be used.
 *
 * @CommentLastReviewed 2026-09-16
 */
@Documented
@Constraint(validatedBy = IsoCountryCodeValidator.class)
@Target({FIELD, METHOD, PARAMETER, ANNOTATION_TYPE, TYPE_USE})
@Retention(RUNTIME)
public @interface IsoCountryCode {

  String message() default "must be a valid ISO 3166-1 alpha-2 country code";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
