package ch.agridata.product.dto;

import ch.agridata.common.dto.LinkDto;
import ch.agridata.common.utils.ValidationSchemaGenerator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Single request body shared by every data-product mutation: create (POST), full replace of a draft (PUT) and partial update of an
 * active product (PATCH). The rules for <em>which</em> fields may be set are not hard-coded per endpoint but expressed through Bean
 * Validation groups, so the same record can serve all three verbs:
 *
 * <ul>
 *   <li><b>Default group (no {@code groups} attribute)</b> &ndash; format checks such as {@code @Size} and nested {@code @Valid} that
 *       always apply, on every create, update and patch.</li>
 *   <li><b>{@link ValidationSchemaGenerator.Submit}</b> &ndash; completeness ({@code @NotNull}): the fields that must be present before a
 *       product can go live. This is <em>not</em> checked while the product is a draft, but only at the {@code DRAFT -> ACTIVE}
 *       transition (activation). A draft may therefore be saved incomplete via POST/PUT and completed later.</li>
 *   <li><b>{@link ValidationSchemaGenerator.PatchAsProvider} / {@link ValidationSchemaGenerator.PatchAsAdmin}</b> &ndash; immutability
 *       rules for PATCH, which is only allowed on an <em>active</em> product. A {@code @Null} in one of these groups marks a field that
 *       the given role may no longer change once the product is active (admins may change more than providers). A field without such a
 *       {@code @Null} stays editable while active.</li>
 * </ul>
 *
 * <p>PUT is restricted to draft products and PATCH to active products (enforced in the service layer); the validation group applied to
 * a given request follows directly from that split.
 *
 * @CommentLastReviewed 2026-08-18
 */
@Schema(description = "Data transfer object representing a data product")
@Builder
public record DataProductUpdateDto(
    @Schema(
        description = "Name of the data product"
    )
    // Editable while active by admins, but locked for providers (only @Null for PatchAsProvider).
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    @Null(groups = ValidationSchemaGenerator.PatchAsProvider.class)
    @Valid
    DataProductNameDto name,

    @Schema(
        description = "Description of the data product"
    )
    // Editable while active by admins, but locked for providers (only @Null for PatchAsProvider).
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    @Null(groups = ValidationSchemaGenerator.PatchAsProvider.class)
    @Valid
    DataProductDescriptionDto description,

    @Schema(
        description = "UUID of DataSourceSystem",
        examples = "5335d715-e95c-4777-a424-ab73f2ff5618"
    )
    // Bound at draft time; immutable once active for every role (@Null for both patch groups).
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    @Null(groups = ValidationSchemaGenerator.PatchAsProvider.class)
    @Null(groups = ValidationSchemaGenerator.PatchAsAdmin.class)
    UUID dataSourceSystemId,

    @Schema(
        description = "UUID of rest client code",
        examples = "b1398c9d-c28d-4e7e-b5f0-f5d615a6471c"
    )
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    UUID restClientId,

    @Schema(
        description = "Template for the path from which the data product will be fetched",
        examples = "v1/animal/{{uid}}"
    )
    @Size(max = 1000)
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    String restClientPathTemplate,

    @Schema(
        description = "Template for the path from which the data product will be fetched",
        examples = "{\"search\":{\"uid\":\"{{uid}}\"}}"
    )
    @Size(max = 1000)
    String restClientRequestTemplate,

    @Schema(
        description = "The http-method used to fetch the data product",
        examples = "GET"
    )
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    RestClientMethodCodeEnum restClientMethodCode,

    @Schema(
        description = "The flow code used for authorizing data retrieval",
        examples = "UID_BASED_PRE_VALIDATION"
    )
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    FlowCodeEnum flowCode,

    @Schema(
        description = "Template for the path for retrieving updates to the data product",
        examples = "v1/animal-updates/{{uid}}?since={{LAST_CHANGED_SINCE}}"
    )
    @Size(max = 1000)
    String restClientChangeDetectionPathTemplate,

    @Schema(
        description = "List of relevant product links"
    )
    @Size(max = 5)
    List<@Valid LinkDto> links,

    @Schema(
        description = "Extended product details"
    )
    @Valid
    DataProductExtendedDescriptionDto extendedDescription,

    @Schema(
        description = "If a consent is required for this data product",
        examples = "true"
    )
    // Bound at draft time; immutable once active for every role (@Null for both patch groups).
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    @Null(groups = ValidationSchemaGenerator.PatchAsProvider.class)
    @Null(groups = ValidationSchemaGenerator.PatchAsAdmin.class)
    Boolean consentRequired
) {

}
