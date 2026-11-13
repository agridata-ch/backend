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
 * Asserts that every {@link String} component of the annotated record (e.g. the {@code de}/{@code fr}/{@code it} values of a translation
 * DTO) is rich-text HTML restricted to the subset produced by the front-end WYSIWYG editor: {@code p, br, strong, em, u, ul, ol, li}
 * without attributes, except {@code start} on {@code ol} (emitted by the editor for lists not starting at 1). With {@link #allowHeadings()}
 * the headings {@code h1}&ndash;{@code h6} are accepted as well (used for AGB texts).
 * Values containing other markup are rejected rather than sanitized, so tampered payloads fail visibly.
 *
 * <p>Placed on the field rather than inside the DTO because some translation DTOs are shared between rich-text and plain-text fields.
 *
 * @CommentLastReviewed 2026-11-12
 */
@Documented
@Constraint(validatedBy = RichTextValidator.class)
@Target({FIELD, METHOD, PARAMETER, ANNOTATION_TYPE, TYPE_USE})
@Retention(RUNTIME)
public @interface RichText {

  String message() default "must only contain the allowed rich-text elements without attributes";

  /**
   * Whether the headings {@code h1}&ndash;{@code h6} are allowed in addition to the editor subset.
   */
  boolean allowHeadings() default false;

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
