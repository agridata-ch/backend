package ch.agridata.bit.dto;

/**
 * Response DTO for the BIT evidence Signing API {@code /secure/v1/startSign} endpoint.
 * When {@code mode} is {@code INTERACTIVE}, the {@code interactionUrl} may be present.
 * For Mobile ID authentication, the URL does not need to be opened in a browser — the user receives
 * a push notification on their device instead.
 *
 * @CommentLastReviewed 2026-04-09
 */
public record BitStartSignResponse(
    BitSignReturnStatusCode status,
    String logId,
    BitSignMode mode,
    String interactionUrl
) {
}
