package ch.agridata.bit.dto;

/**
 * Response DTO for the BIT evidence Signing API {@code /secure/v1/dropSign} endpoint.
 *
 * @CommentLastReviewed 2026-04-09
 */
public record BitDropSignResponse(
    BitSignReturnStatusCode status,
    String logId
) {
}
