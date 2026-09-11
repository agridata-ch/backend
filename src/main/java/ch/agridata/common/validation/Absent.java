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
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.openapitools.jackson.nullable.JsonNullable;

/**
 * Asserts that a {@link JsonNullable} field is <em>absent</em> from the request body, i.e. that the client did not send the property at
 * all. Use it for fields a given role may no longer touch, where {@code @Null} is not enough: because
 * {@link JsonNullableValueExtractor} is {@code @UnwrapByDefault}, an explicit {@code null} in the JSON is extracted as a {@code null}
 * value and therefore <em>satisfies</em> {@code @Null}, which would let a client clear an immutable field. Declare it as
 * {@code @Absent(payload = Unwrapping.Skip.class)} so it validates the wrapper itself and can distinguish an omitted field from a
 * present {@code null}; without that payload the extractor unwraps the value and the constraint can never see the difference.
 *
 * <p>A {@code null} reference counts as absent, so DTOs built directly (e.g. in unit tests) where an unset field is a plain {@code null}
 * rather than {@link JsonNullable#undefined()} pass as well.
 *
 * @CommentLastReviewed 2026-09-11
 */
@Documented
@Constraint(validatedBy = AbsentValidator.class)
@Target({FIELD, METHOD, PARAMETER, ANNOTATION_TYPE, TYPE_USE})
@Retention(RUNTIME)
@Repeatable(Absent.List.class)
public @interface Absent {

  String message() default "must not be set";

  Class<?>[] groups() default {};

  // Must default to the empty array (HV000075), so every usage has to declare payload = Unwrapping.Skip.class itself to switch off the
  // implicit unwrapping done by JsonNullableValueExtractor.
  Class<? extends Payload>[] payload() default {};

  /**
   * Container that lets {@link Absent} be declared several times on the same element, once per validation group.
   *
   * @CommentLastReviewed 2026-09-11
   */
  @Documented
  @Target({FIELD, METHOD, PARAMETER, ANNOTATION_TYPE, TYPE_USE})
  @Retention(RUNTIME)
  @interface List {
    Absent[] value();
  }
}
