package ch.agridata.bit.dto;

/**
 * Response DTO for the BIT evidence Signing API {@code /secure/v1/initSign} endpoint.
 *
 * @CommentLastReviewed 2026-04-09
 */
public record BitInitSignResponse(
    BitSignReturnStatusCode status,
    String logId,
    String signProcessToken
) {
}
