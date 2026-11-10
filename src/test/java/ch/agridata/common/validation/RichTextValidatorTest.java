package ch.agridata.common.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for {@link RichTextValidator}.
 *
 * @CommentLastReviewed 2026-11-09
 */
class RichTextValidatorTest {

  private final RichTextValidator validator = new RichTextValidator();

  @ParameterizedTest
  @ValueSource(strings = {
      "",
      "<p></p>",
      "<p>a&nbsp;b &lt;script&gt; 1 &gt; 0</p>",
      "<p><strong>bold</strong> <em>italic</em> <u>underline</u><br>line</p>",
      "<ul><li><p>one</p></li></ul><ol><li><p>two</p></li></ol>",
      "<p>a<br/>b<br />c</p>",
      "<p >spaced</p >"
  })
  void givenSupportedTags_whenValidated_thenValid(String value) {
    assertThat(validator.isValid(value, null)).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "<script>alert(1)</script>",
      "<p onclick=\"alert(1)\">text</p>",
      "<p class=\"x\">text</p>",
      "<h1>title</h1>",
      "<a href=\"https://example.com\">link</a>",
      "<img src=x onerror=alert(1)>",
      "<P>upper case</P>",
      "<p>text<!-- comment --></p>",
      "<p><![CDATA[<script>alert(1)</script>]]></p>",
      "<p><?pi data?></p>",
      "<x:p>namespaced</x:p>",
      "<scr<p>ipt>alert(1)</script>",
      "<p>unterminated <</p>",
      "<p>unclosed",
      "</p>",
      "<p><strong>wrongly nested</p></strong>",
      "<br >",
      "<p>Müller & Co</p>",
      "<p>Gr&uuml;ezi</p>",
      "plain text",
      "text <p>partly wrapped</p>"
  })
  void givenUnsupportedMarkup_whenValidated_thenInvalid(String value) {
    assertThat(validator.isValid(value, null)).isFalse();
  }

  @Test
  void givenAdditionalTags_whenValidated_thenOnlyConfiguredTagsAreAddedToTheEditorTags() throws NoSuchFieldException {
    var headingValidator = new RichTextValidator();
    headingValidator.initialize(WithHeadings.class.getDeclaredField("text").getAnnotation(RichText.class));

    assertThat(headingValidator.isValid("<h1>a</h1><h2>b</h2><h3>c</h3><p><strong>d</strong></p>", null)).isTrue();
    assertThat(headingValidator.isValid("<h4>d</h4>", null)).isFalse();
    assertThat(headingValidator.isValid("<h1 class=\"x\">a</h1>", null)).isFalse();
    assertThat(validator.isValid("<h1>a</h1>", null)).isFalse();
  }

  @Test
  void givenNull_whenValidated_thenValid() {
    // A null value is delegated to @NotNull; this constraint only judges the markup of a present value.
    assertThat(validator.isValid(null, null)).isTrue();
  }

  private static final class WithHeadings {
    @RichText(additionalTags = {"h1", "h2", "h3"})
    String text;
  }
}
