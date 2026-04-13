package ch.agridata.bit.dto;

import java.util.List;

/**
 * Represents a single signed hash entry in the {@code getSignedHashes} response.
 * Each entry corresponds to one {@code addHash} call and carries the signature blob,
 * a timestamp token, and the associated CRL and OCSP data.
 *
 * @CommentLastReviewed 2026-04-09
 */
public record BitSignatureEntry(
    String signature,
    String pkcsVersion,
    String tag,
    String timestamp,
    List<String> timestampCrl,
    List<String> timestampOcsp
) {
}
