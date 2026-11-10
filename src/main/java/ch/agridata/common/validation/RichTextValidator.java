package ch.agridata.common.validation;

import ch.agridata.common.exceptions.RichTextParseException;
import ch.agridata.common.utils.RichTextHtmlParser;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Validator for {@link RichText}: the value is parsed with {@link RichTextHtmlParser#toElement(String, String)}, the same parse used for
 * PDF rendering, so a value fails validation whenever it would fail PDF rendering. The parsed tree must then consist of text nodes and
 * supported elements (including {@link RichText#additionalTags()}) without attributes only. Any other node (comments, CDATA sections,
 * processing instructions) is rejected, as browsers interpret these differently from an XML parser.
 *
 * @CommentLastReviewed 2026-11-09
 */
public class RichTextValidator implements ConstraintValidator<RichText, String> {

  private static final Set<String> EDITOR_TAGS = Set.of("p", "br", "strong", "em", "u", "ul", "ol", "li");

  private Set<String> supportedTags = EDITOR_TAGS;

  @Override
  public void initialize(RichText annotation) {
    var tags = new HashSet<>(EDITOR_TAGS);
    tags.addAll(List.of(annotation.additionalTags()));
    supportedTags = Set.copyOf(tags);
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }
    try {
      return containsOnlySupportedNodes(RichTextHtmlParser.toElement("root", value));
    } catch (RichTextParseException e) {
      return false;
    }
  }

  private boolean containsOnlySupportedNodes(Node parent) {
    NodeList children = parent.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      boolean supported = switch (child.getNodeType()) {
        case Node.TEXT_NODE -> true;
        case Node.ELEMENT_NODE -> supportedTags.contains(child.getNodeName())
            && !child.hasAttributes()
            && containsOnlySupportedNodes(child);
        default -> false;
      };
      if (!supported) {
        return false;
      }
    }
    return true;
  }
}
