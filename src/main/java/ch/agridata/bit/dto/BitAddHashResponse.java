package ch.agridata.bit.dto;

/**
 * Response DTO for the BIT evidence Signing API {@code /secure/v1/addHash} endpoint.
 *
 * @CommentLastReviewed 2026-04-09
 */
public record BitAddHashResponse(
    BitSignReturnStatusCode status,
    String logId
) {
}
