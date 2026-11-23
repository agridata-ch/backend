package ch.agridata.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Represents the company (UID) shown on the farm overview, with its name and postal address.
 *
 * @CommentLastReviewed 2026-11-18
 */
@Schema(description = "Data transfer object representing a company on the farm overview")
@Builder
public record CompanyDto(
    @NotNull
    String uid,
    String name,
    AddressDto address
) {
}
