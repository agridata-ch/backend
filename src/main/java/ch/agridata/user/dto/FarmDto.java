package ch.agridata.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Represents a farm (BUR) shown on the farm overview, with its cantonal farm id, farm type and location address.
 *
 * @CommentLastReviewed 2026-11-18
 */
@Schema(description = "Data transfer object representing a farm on the farm overview")
@Builder
public record FarmDto(
    @NotNull
    String bur,
    String ktIdB,
    FarmTypeEnum farmTypeCode,
    AddressDto address
) {
}
