package ch.agridata.common.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.agridata.common.exceptions.RichTextParseException;
import org.junit.jupiter.api.Test;

class RichTextHtmlParserTest {

  @Test
  void givenMalformedXhtml_toElement_throwsRichTextParseException() {
    assertThatThrownBy(() -> RichTextHtmlParser.toElement("de", "<p>unclosed"))
        .isInstanceOf(RichTextParseException.class)
        .hasMessageContaining("is not well-formed XHTML")
        .hasCauseInstanceOf(Exception.class);
  }

  @Test
  void givenBareText_toElement_throwsRichTextParseException() {
    assertThatThrownBy(() -> RichTextHtmlParser.toElement("fr", "just plain text"))
        .isInstanceOf(RichTextParseException.class)
        .hasMessageContaining("is not HTML markup")
        .hasNoCause();
  }

  @Test
  void givenWellFormedMarkup_toElement_returnsWrappingElement() {
    var element = RichTextHtmlParser.toElement("it", "<p>ciao</p>");
    assertThat(element.getTagName()).isEqualTo("it");
    assertThat(element.getElementsByTagName("p").getLength()).isEqualTo(1);
  }
}