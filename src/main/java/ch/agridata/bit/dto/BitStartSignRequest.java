package ch.agridata.bit.dto;

/**
 * Request DTO for the BIT evidence Signing API {@code /secure/v1/startSign} endpoint.
 *
 * @CommentLastReviewed 2026-04-09
 */
public record BitStartSignRequest(
    String signProcessToken
) {
}
