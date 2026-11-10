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
 * Asserts that a {@link String} rich-text (HTML) value only contains the tags supported by the front-end WYSIWYG editor:
 * {@code p}, {@code br}, {@code strong}, {@code em}, {@code u}, {@code ul}, {@code ol} and {@code li}, plus the tags listed in
 * {@link #additionalTags()}. Tags must not carry attributes.
 * Any other markup (other tags, attributes, comments, CDATA sections, processing instructions, doctype declarations) is rejected.
 * In addition, every value that would fail PDF rendering is rejected: markup that is not well-formed (e.g. unclosed or wrongly
 * nested tags, or a raw {@code &} that is not an entity) and text that is not wrapped in a block element.
 *
 * @CommentLastReviewed 2026-11-09
 */
@Documented
@Constraint(validatedBy = RichTextValidator.class)
@Target({FIELD, METHOD, PARAMETER, ANNOTATION_TYPE, TYPE_USE})
@Retention(RUNTIME)
public @interface RichText {

  String message() default "must be well-formed, wrapped in block elements and only contain the supported rich-text tags "
      + "(p, br, strong, em, u, ul, ol, li and any field-specific additions) without attributes";

  /**
   * Tags allowed in addition to the ones supported by the WYSIWYG editor, e.g. headings for agb texts. Must be lowercase.
   */
  String[] additionalTags() default {};

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
