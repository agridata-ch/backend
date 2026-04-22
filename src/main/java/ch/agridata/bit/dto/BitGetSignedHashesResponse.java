package ch.agridata.bit.dto;

import java.util.List;

/**
 * Response DTO for the BIT evidence Signing API {@code /secure/v1/getSignedHashes} endpoint.
 * Contains one {@link BitSignatureEntry} per hash added via {@code addHash}.
 *
 * @CommentLastReviewed 2026-04-09
 */
public record BitGetSignedHashesResponse(
    BitSignReturnStatusCode status,
    String logId,
    String signerCert,
    List<String> signerCertOcsp,
    List<String> signerCertCrl,
    List<BitSignatureEntry> signatures
) {
}
