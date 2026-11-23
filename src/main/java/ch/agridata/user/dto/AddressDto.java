package ch.agridata.user.dto;

import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Represents a postal address of a company or a farm as delivered by the register. All fields are optional.
 *
 * @CommentLastReviewed 2026-11-18
 */
@Schema(description = "Data transfer object representing a postal address")
@Builder
public record AddressDto(
    String addressLine,
    String street,
    String houseNumber,
    String postOfficeBox,
    String zipCode,
    String town
) {
}
