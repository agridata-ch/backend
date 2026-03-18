package ch.agridata.agreement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Represents a request for verifying a one-time password (OTP).
 *
 * @CommentLastReviewed: 2026-03-19
 */

public record VerifyOtpRequestDto(
    @Schema(
        description = "The 6-digit code to be verified.",
        examples = {"123456"},
        required = true
    )
    @NotBlank
    @Pattern(regexp = "^\\d{6}$", message = "OTP must be exactly 6 digits")
    String otpCode
) {
}
