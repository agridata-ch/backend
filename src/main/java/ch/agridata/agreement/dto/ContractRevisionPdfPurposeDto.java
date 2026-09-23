package ch.agridata.agreement.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyElement;
import java.util.List;
import org.w3c.dom.Element;

/**
 * Carries the localized purpose of a data request as parsed rich-text (HTML) nodes for PDF rendering.
 * Each language is held as a DOM element ({@code <de>}, {@code <fr>}, {@code <it>}) whose children are
 * the supported rich-text elements (p, strong, em, u, ul, ol, li). Marshalled verbatim into the XSL-FO
 * source tree so the stylesheet can transform the markup into formatted output instead of printing it
 * as escaped source text.
 *
 * @CommentLastReviewed 2026-09-22
 */
@XmlAccessorType(XmlAccessType.FIELD)
public final class ContractRevisionPdfPurposeDto {

  @XmlAnyElement
  private List<Element> languages;

  public ContractRevisionPdfPurposeDto() {
  }

  public ContractRevisionPdfPurposeDto(List<Element> languages) {
    this.languages = languages;
  }

  public List<Element> getLanguages() {
    return languages;
  }
}
