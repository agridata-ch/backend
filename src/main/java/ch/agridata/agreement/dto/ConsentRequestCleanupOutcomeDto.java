package ch.agridata.agreement.dto;

import java.time.LocalDate;
import lombok.Builder;

/**
 * Captures the result of a single consent request cleanup pass. It reports the evaluated time
 * window together with the number of consent requests that were terminated.
 *
 * @CommentLastReviewed 2026-09-07
 */

@Builder
public record ConsentRequestCleanupOutcomeDto(
    LocalDate fromInclusive,
    LocalDate toInclusive,
    long terminatedConsentRequestCount
) {
}
