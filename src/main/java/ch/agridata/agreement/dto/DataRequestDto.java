package ch.agridata.agreement.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Represents the complete definition of a data request. It includes identifiers, metadata, consumer details, requested products, and
 * contact information.
 *
 * @CommentLastReviewed 2025-08-25
 */

@Schema(description = "Data transfer object representing a data request")
@Builder
public record DataRequestDto(

    @Schema(
        description = "Unique identifier of the data request",
        examples = {"3fa85f64-5717-4562-b3fc-2c963f66afb7"}
    )
    UUID id,

    @Schema(
        description = "Human friendly id of the data request",
        examples = {"GL56"}
    )
    String humanFriendlyId,

    @Schema(
        description = "Date and time when the data request was submitted",
        examples = {"2025-06-16T11:04:51.823889"},
        type = SchemaType.STRING,
        format = "date"
    )
    LocalDateTime submissionDate,

    @Schema(
        description = "Title of the data request"
    )
    DataRequestTitleDto title,

    @Schema(
        description = "Description of the data request ")
    DataRequestDescriptionDto description,

    @Schema(
        description = "Purpose of the data request"
    )
    DataRequestPurposeDto purpose,

    @Schema(
        description = "List of data products requested"
    )
    List<UUID> products,

    @Schema(
        description = "State of the data request",
        implementation = DataRequestStateEnum.class
    )
    @NotNull
    DataRequestStateEnum stateCode,

    @Schema(
        description = "LegalName of the data consumer taken from the uid register",
        examples = {"Bio Suisse, Vereinigung Schweizer Biolandbau-Organisationen"}
    )
    @Size(max = 255)
    String dataConsumerLegalName,

    @Schema(
        description = "Shorter name of the data consumer defined by the data consumer used when displaying the request to the producer",
        examples = {"Bio Suisse"}
    )
    @Size(max = 255)
    String dataConsumerDisplayName,

    @Schema(
        description = "Uid of the data consumer",
        examples = {"CHE101708094"}
    )
    @Max(16)
    String dataConsumerUid,

    @Schema(
        description = "City of the data consumer",
        examples = {"Basel"}
    )
    @Size(max = 120)
    String dataConsumerCity,

    @Schema(
        description = "Zip code of the data consumer",
        examples = {"4052"}
    )
    @Size(max = 10)
    String dataConsumerZip,

    @Schema(
        description = "Street of the data consumer",
        examples = {"Peter Merian-Str. 34"}
    )
    @Size(max = 255)
    String dataConsumerStreet,

    @Schema(
        description = "2 letter country code of the data consumer",
        examples = {"CH"}
    )
    @Size(max = 2)
    String dataConsumerCountry,

    @Schema(
        description = "Contact phone number for the data request",
        examples = {"+41 79 123 45 67"}
    )
    @Size(max = 50)
    String contactPhoneNumber,

    @Schema(
        description = "Contact email address for the data request",
        examples = {"example@labelorganisation.ch"}
    )
    @Size(max = 255)
    String contactEmailAddress,

    @Schema(
        description = "Base64-encoded logo of the data consumer",
        examples = {"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA..."}
    )
    String dataConsumerLogoBase64,

    @Size(max = 150)
    String targetGroup,

    @Schema(
        description = "Regex of valid redirect_uri",
        examples = {"^https:\\/\\/www\\.dummy-label-organisation\\/.*$"}
    )
    String validRedirectUriRegex

) {

}
