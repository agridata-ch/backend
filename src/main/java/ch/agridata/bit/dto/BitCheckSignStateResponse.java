package ch.agridata.bit.dto;

/**
 * Response DTO for the BIT evidence Signing API {@code /secure/v1/checkSignState} endpoint.
 *
 * @CommentLastReviewed 2026-04-09
 */
public record BitCheckSignStateResponse(
    BitSignReturnStatusCode status,
    String logId,
    BitSignState signState
) {
}
