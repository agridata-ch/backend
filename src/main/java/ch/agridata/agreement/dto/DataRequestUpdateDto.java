package ch.agridata.agreement.dto;

import ch.agridata.common.utils.ValidationSchemaGenerator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * It encapsulates required fields for updating a data request. It enforces constraints such as string lengths, non-null requirements, and
 * email formatting rules.
 *
 * @CommentLastReviewed 2025-08-25
 */

@Schema(description = "Data transfer object representing a data request")
@Builder
public record DataRequestUpdateDto(

    @Schema(
        description = "Title of the data request"
    )
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    @Valid
    DataRequestTitleDto title,

    @Schema(
        description = "Description of the data request "
    )
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    @Valid
    DataRequestDescriptionDto description,

    @Schema(
        description = "Purpose of the data request"
    )
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    @Valid
    DataRequestPurposeDto purpose,

    @Schema(
        description = "Shorter name of the data consumer defined by the data consumer used when displaying the request to the producer",
        examples = {"Bio Suisse"}
    )
    @Size(max = 255)
    @Size(min = 3, max = 255, groups = ValidationSchemaGenerator.Submit.class)
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    String dataConsumerDisplayName,

    @Schema(
        description = "City of the data consumer",
        examples = {"Basel"}
    )
    @Size(max = 120)
    @Size(min = 2, max = 120, groups = ValidationSchemaGenerator.Submit.class)
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    String dataConsumerCity,

    @Schema(
        description = "Zip code of the data consumer",
        examples = {"4052"}
    )
    @Size(max = 10)
    @Size(min = 4, max = 10, groups = ValidationSchemaGenerator.Submit.class)
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    String dataConsumerZip,

    @Schema(
        description = "Street of the data consumer",
        examples = {"Peter Merian-Str. 34"}
    )
    @Size(max = 120)
    @Size(min = 2, max = 120, groups = ValidationSchemaGenerator.Submit.class)
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    String dataConsumerStreet,

    @Schema(
        description = "2 letter country code of the data consumer",
        examples = {"CH"}
    )
    @Size(max = 2)
    @Size(min = 2, max = 2, groups = ValidationSchemaGenerator.Submit.class)
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    String dataConsumerCountry,

    @Schema(
        description = "Contact phone number for the data request",
        examples = {"+41 79 123 45 67"}
    )
    @Size(max = 50)
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    String contactPhoneNumber,

    @Schema(
        description = "Contact email address for the data request",
        examples = {"example@labelorganisation.ch"}
    )
    @Size(max = 255)
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    @Pattern(regexp = "^.*@.*$", message = "Must contain @", groups = ValidationSchemaGenerator.Submit.class)
    String contactEmailAddress,

    @Schema(
        description = "Target group of the data request",
        examples = {"von allen Bio Suisse Mitgliedern"}
    )
    @Size(max = 150)
    @Size(min = 5, max = 150, groups = ValidationSchemaGenerator.Submit.class)
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    String targetGroup,

    @Schema(
        description = "List of data products requested",
        examples = "[\"c661ea48-106d-4d7a-a5d1-a9a6db48dd8c\"]"
    )
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    @NotEmpty(groups = ValidationSchemaGenerator.Submit.class)
    List<UUID> products
) {


}
