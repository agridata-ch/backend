package ch.agridata.agreement.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Represents an OTP challenge.
 *
 * @CommentLastReviewed: 2026-03-19
 */

public record OtpChallengeDto(
    @Schema(
        description = "Unique identifier of the challenge",
        examples = {"F259F6C6-8C31-4809-AF3E-1C11379E9D32"}
    )
    UUID challengeId,
    @Schema(
        description = "Expiration time of the challenge",
        examples = {"2026-03-19T12:00:00Z"}
    )
    LocalDateTime expiresAt,
    @Schema(
        description = "Masked phone number",
        examples = {"***********8"}
    )
    String maskedPhoneNumber,
    @Schema(
        description = "Number of remaining attempts",
        examples = {"30"}
    )
    long retryAfterSeconds
) {
}
