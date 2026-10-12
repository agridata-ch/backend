package ch.agridata.burregister.dto;

import lombok.Builder;

/**
 * Represents an organization's address in the BUR (Business and Enterprise Register). It captures street, zip code, city and country;
 * the zip code is the Swiss or, for foreign organisations, the foreign zip code.
 *
 * @CommentLastReviewed 2026-10-09
 */
@Builder
public record BurRegisterAddressDto(String street, String zip, String city, String country) {
}
