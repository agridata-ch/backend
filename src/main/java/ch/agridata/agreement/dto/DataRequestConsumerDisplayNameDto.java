package ch.agridata.agreement.dto;

import ch.agridata.common.utils.ValidationSchemaGenerator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Represents the multilingual display name of the data consumer.
 *
 * @CommentLastReviewed 2026-09-18
 */
@Builder
public record DataRequestConsumerDisplayNameDto(
    @Schema(
        examples = {"Bio Suisse"}
    )
    @Size(max = 255)
    @Size(min = 3, max = 255, groups = ValidationSchemaGenerator.Submit.class)
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    String de,

    @Schema(
        examples = {"Bio Suisse"}
    )
    @Size(max = 255)
    @Size(min = 3, max = 255, groups = ValidationSchemaGenerator.Submit.class)
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    String fr,

    @Schema(
        examples = {"Bio Suisse"}
    )
    @Size(max = 255)
    @Size(min = 3, max = 255, groups = ValidationSchemaGenerator.Submit.class)
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    String it
) {
}
