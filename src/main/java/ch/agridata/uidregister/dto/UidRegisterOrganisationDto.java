package ch.agridata.uidregister.dto;

import lombok.Builder;

/**
 * Models an organization entry from the UID register. It contains the name, legal name, UID, and address, providing a complete
 * representation of a registered entity.
 *
 * @CommentLastReviewed 2025-08-25
 */

@Builder
public record UidRegisterOrganisationDto(String name, String legalName, String uid, UidRegisterAddressDto address) {

}
