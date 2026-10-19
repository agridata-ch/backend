package ch.agridata.burregister.dto;

import lombok.Builder;

/**
 * Models an organization entry from the BUR (Business and Enterprise Register). It contains the name, legal name, UID and address.
 *
 * @CommentLastReviewed 2026-10-09
 */
@Builder
public record BurRegisterOrganisationDto(String name, String legalName, String uid, BurRegisterAddressDto address) {
}
