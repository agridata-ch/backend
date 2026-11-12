package ch.agridata.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

/**
 * Validator for {@link RichText}: checks each {@link String} component of a record with jsoup against an allowlist matching the
 * front-end editor schema, optionally extended by the headings {@code h1}&ndash;{@code h6}. A violation is reported on the offending
 * component (e.g. {@code purpose.fr}) so clients can tell which language failed. {@code null} values are valid; use {@code @NotNull} to
 * require them.
 *
 * @CommentLastReviewed 2026-11-12
 */
public class RichTextValidator implements ConstraintValidator<RichText, Record> {

  private static final String[] EDITOR_TAGS = {"p", "br", "strong", "em", "u", "ul", "ol", "li"};
  private static final String[] HEADING_TAGS = {"h1", "h2", "h3", "h4", "h5", "h6"};

  private Safelist safelist = safelist(false);

  @Override
  public void initialize(RichText constraint) {
    safelist = safelist(constraint.allowHeadings());
  }

  @Override
  public boolean isValid(Record value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }

    var valid = true;
    for (RecordComponent component : value.getClass().getRecordComponents()) {
      if (component.getType() == String.class && !isValidHtml((String) read(component, value), safelist)) {
        valid = false;
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
            .addPropertyNode(component.getName())
            .addConstraintViolation();
      }
    }
    return valid;
  }

  static Safelist safelist(boolean allowHeadings) {
    var safelist = Safelist.none().addTags(EDITOR_TAGS).addAttributes("ol", "start");
    return allowHeadings ? safelist.addTags(HEADING_TAGS) : safelist;
  }

  static boolean isValidHtml(String html, Safelist safelist) {
    return html == null || Jsoup.isValid(html, safelist);
  }

  private static Object read(RecordComponent component, Record value) {
    try {
      return component.getAccessor().invoke(value);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException("Cannot read record component " + component.getName(), e);
    }
  }
}
