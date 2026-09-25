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

  @Test
  void givenNbspEntity_toElement_parsesAsNonBreakingSpace() {
    var element = RichTextHtmlParser.toElement("de", "<p>a&nbsp;b</p>");

    assertThat(element.getElementsByTagName("p").getLength()).isEqualTo(1);
    assertThat(element.getTextContent()).isEqualTo("a b");
  }

  @Test
  void givenEditorContentWithNbspSpacers_toElement_doesNotThrow() {
    var html =
        "<p>First paragraph of text.</p><p>&nbsp;</p>"
            + "<ul><li><p><em>a list item in italics</em></p></li></ul>"
            + "<p><strong>&nbsp;</strong></p><p><strong>A bold heading</strong></p>";

    var element = RichTextHtmlParser.toElement("de", html);

    assertThat(element.getTagName()).isEqualTo("de");
    assertThat(element.getElementsByTagName("p").getLength()).isEqualTo(5);
    assertThat(element.getElementsByTagName("li").getLength()).isEqualTo(1);
  }
}
