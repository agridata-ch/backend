package ch.agridata.agreement.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.Builder;

/**
 * Carries localized translations (German, French, Italian) for a single attribute
 * used in PDF generation.
 *
 * @CommentLastReviewed 2026-05-04
 */

@XmlAccessorType(XmlAccessType.FIELD)
@Builder
public record ContractRevisionPdfTranslationDto(String de, String fr, String it) {
  public ContractRevisionPdfTranslationDto() {
    this(null, null, null);
  }
}