package ch.agridata.agreement.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;
import lombok.Builder;

/**
 * Data Transfer Object representing a Contract Revision for PDF rendering.
 * This record is annotated for JAXB marshalling to serve as the XML source
 * for Apache FOP and XSL-FO templates. It contains flattened address data
 * for both the Consumer and Provider, along with signature details.
 *
 * @CommentLastReviewed: 2026-04-17
 */

@XmlRootElement(name = "ContractRevision")
@XmlAccessorType(XmlAccessType.FIELD)
@Builder
public record ContractRevisionPdfDto(

    // request information
    ContractRevisionPdfTranslationDto requestTitle,
    ContractRevisionPdfTranslationDto requestDescription,
    ContractRevisionPdfTranslationDto requestPurpose,

    String targetGroup,

    List<ContractRevisionPdfTranslationDto> products,

    // consumer
    String consumerName,
    String consumerStreet,
    String consumerZipCity,
    String consumerPhoneNumber,
    String consumerEmailAddress,
    String consumerUid,

    // Pre-formatted Address in the style "name, street, zip city"
    String consumerAddressInline,

    String providerName,
    String providerStreet,
    String providerZipCity,

    // Pre-formatted Address in the style "name, street, zip city"
    String providerAddressInline,

    ContractRevisionPdfTranslationDto providerSystemName,

    String consumerSignatureName1,
    String consumerSignatureDate1,
    String consumerSignatureName2,
    String consumerSignatureDate2,
    String providerSignatureName1,
    String providerSignatureDate1,
    String providerSignatureName2,
    String providerSignatureDate2,

    String consumerSignatureType,
    String providerSignatureType
) {
  // Constructor for JAXB
  public ContractRevisionPdfDto() {
    this(
        null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null,
        null, null, null
    );
  }
}
