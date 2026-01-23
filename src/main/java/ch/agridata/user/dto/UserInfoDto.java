package ch.agridata.user.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Data transfer object (DTO) representing user profile information, including identity, contact details, and address.
 *
 * @CommentLastReviewed 2025-08-27
 */
@Schema(description = "Represents basic user information, such as name, contact details, and address.")
@Builder
public record UserInfoDto(

    @Schema(
        description = "AgateLoginId of the user.",
        examples = {"1234567"}
    )
    String agateLoginId,

    @Schema(
        description = "KtIdP of the user. Only set for data producers",
        examples = {"ZH123456"}
    )
    String ktIdP,

    @Schema(
        description = "UID of the user. Only set for data consumers.",
        examples = {"CHE123456789"}
    )
    String uid,

    @Schema(
        description = "User's first name.",
        examples = {"Anna"}
    )
    String givenName,

    @Schema(
        description = "User's last name.",
        examples = {"Muster"}
    )
    String familyName,

    @Schema(
        description = "Email address of the user",
        examples = {"anna.muster@example.com"}
    )
    String email,

    @Schema(
        description = "Phone number of the user",
        examples = {"+41791234567"}
    )
    String phoneNumber,

    @Schema(
        description = "Street name and house number of the user's residence.",
        examples = {"Bahnhofstrasse 10"}
    )
    String addressStreet,

    @Schema(
        description = "Locality (city or town) of the user's residence.",
        examples = {"Zürich"}
    )
    String addressLocality,

    @Schema(
        description = "Postal code of the user's residence.",
        examples = {"8001"}
    )
    String addressPostalCode,

    @Schema(
        description = "Country of residence",
        examples = {"CH"}
    )
    String addressCountry,

    @Schema(
        description = "Date and time when the user last logged into the system.",
        examples = {"2025-08-27T14:35:00"}
    )
    LocalDateTime lastLoginDate,

    @Schema(
        description = "Preferences of the user in the frontend"
    )
    UserPreferencesDto userPreferences,

    @Schema(
        description = "Roles of the user at the last login"
    )
    List<String> rolesAtLastLogin
) {
}
