package ch.agridata.agreement.dto;

import java.time.LocalDate;

/**
 * A version of the contract template. {@code validTo} is the {@code validFrom} of the next version and null for the current version.
 *
 * @CommentLastReviewed 2026-11-18
 */
public record ContractTemplateVersionDto(String version, LocalDate validFrom, LocalDate validTo) {
}
