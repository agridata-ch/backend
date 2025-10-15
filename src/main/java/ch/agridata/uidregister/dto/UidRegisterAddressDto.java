package ch.agridata.uidregister.dto;

import lombok.Builder;


/**
 * Represents an organization’s address in the UID register. It captures fields such as street, zip code, city, and country in a
 * transportable format.
 *
 * @CommentLastReviewed 2025-08-25
 */

@Builder
public record UidRegisterAddressDto(String street, String zip, String city, String country) {
}
