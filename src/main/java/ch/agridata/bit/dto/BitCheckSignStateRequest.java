package ch.agridata.bit.dto;

/**
 * Request DTO for the BIT evidence Signing API {@code /secure/v1/checkSignState} endpoint.
 * When {@code longPolling} is {@code true}, the server holds the request for up to 60 seconds
 * until the sign state changes from {@code SIGN_RUNNING} or the timeout is reached.
 *
 * @CommentLastReviewed 2026-04-09
 */
public record BitCheckSignStateRequest(
    String signProcessToken,
    boolean longPolling
) {
}
