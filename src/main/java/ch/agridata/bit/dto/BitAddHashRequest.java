package ch.agridata.bit.dto;

/**
 * Request DTO for the BIT evidence Signing API {@code /secure/v1/addHash} endpoint.
 * Adds a single Base64-encoded hash to an existing sign process.
 * All hashes within one sign process must use the same {@code signatureAlgorithm}.
 *
 * @CommentLastReviewed 2026-04-09
 */
public record BitAddHashRequest(
    String signProcessToken,
    String digest,
    String signatureAlgorithm,
    String pkcsVersion,
    String tag
) {
}
