package ch.agridata.bit.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request DTO for the BIT evidence Signing API {@code /secure/v1/initSign} endpoint.
 * Initialises a new sign/seal process on the server and returns a {@code signProcessToken}.
 *
 * @CommentLastReviewed 2026-04-09
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BitInitSignRequest(
    String keyBearer,
    String profile,
    String lang,
    String callbackUrl,
    String authType,
    @JsonProperty("adminGlobalID") String adminGlobalId
) {
}
