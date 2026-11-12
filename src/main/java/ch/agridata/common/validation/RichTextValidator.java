package ch.agridata.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.TextNode;
import org.jsoup.safety.Safelist;

/**
 * Validator for {@link RichText}: checks each {@link String} component of a record with jsoup against an allowlist matching the
 * front-end editor schema, optionally extended by the headings {@code h1}&ndash;{@code h6}, and rejects text directly at the top level
 * (e.g. {@code "text"} or {@code "text<p>x</p>"}), since the editor always wraps its content in block elements. A violation is reported
 * on the offending component (e.g. {@code purpose.fr}) so clients can tell which language failed. {@code null} values are valid; use
 * {@code @NotNull} to require them.
 *
 * @CommentLastReviewed 2026-11-16
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
    return html == null || (Jsoup.isValid(html, safelist) && hasNoTopLevelText(html));
  }

  // The editor wraps all content in block elements, so text at the top level means plain text instead of editor HTML.
  private static boolean hasNoTopLevelText(String html) {
    return Jsoup.parseBodyFragment(html).body().textNodes().stream().allMatch(TextNode::isBlank);
  }

  private static Object read(RecordComponent component, Record value) {
    try {
      return component.getAccessor().invoke(value);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException("Cannot read record component " + component.getName(), e);
    }
  }
}
