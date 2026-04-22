package ch.agridata.bit.dto;

/**
 * Request DTO for the BIT evidence Signing API {@code /secure/v1/dropSign} endpoint.
 * Cleans up the sign process on the server side. Should be called after a successful
 * or failed signing attempt. The server automatically drops processes after 4 hours.
 *
 * @CommentLastReviewed 2026-04-09
 */
public record BitDropSignRequest(
    String signProcessToken
) {
}
