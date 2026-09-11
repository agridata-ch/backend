package ch.agridata.product.dto;

import ch.agridata.common.dto.LinkDto;
import ch.agridata.common.utils.ValidationSchemaGenerator;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.openapitools.jackson.nullable.JsonNullable;

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
 * <p>Every field is wrapped in {@link JsonNullable} so a PATCH can distinguish an <em>omitted</em> field (absent from the JSON body,
 * {@link JsonNullable#isPresent()} is {@code false} &ndash; leave the current value untouched) from an <em>explicit {@code null}</em>
 * (present with a {@code null} value &ndash; clear the field). {@code @JsonInclude(NON_ABSENT)} keeps undefined values out of the
 * serialized body. Bean Validation constraints are applied to the wrapped value through a registered {@code JsonNullable} value
 * extractor, so an omitted field is skipped by every constraint (including {@code @NotNull}/{@code @Null}) as before.
 *
 * @CommentLastReviewed 2026-09-07
 */
@Schema(description = "Data transfer object representing a data product")
@Builder
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public record DataProductUpdateDto(
    @Schema(
        description = "Name of the data product"
    )
    // Editable while active by admins, but locked for providers (only @Null for PatchAsProvider).
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    @Null(groups = ValidationSchemaGenerator.PatchAsProvider.class)
    JsonNullable<@Valid DataProductNameDto> name,

    @Schema(
        description = "Description of the data product"
    )
    // Editable while active by admins, but locked for providers (only @Null for PatchAsProvider).
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    @Null(groups = ValidationSchemaGenerator.PatchAsProvider.class)
    JsonNullable<@Valid DataProductDescriptionDto> description,

    @Schema(
        description = "UUID of DataSourceSystem",
        examples = "5335d715-e95c-4777-a424-ab73f2ff5618"
    )
    // Bound at draft time; immutable once active for every role (@Null for both patch groups).
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    @Null(groups = ValidationSchemaGenerator.PatchAsProvider.class)
    @Null(groups = ValidationSchemaGenerator.PatchAsAdmin.class)
    JsonNullable<UUID> dataSourceSystemId,

    @Schema(
        description = "UUID of rest client code",
        examples = "b1398c9d-c28d-4e7e-b5f0-f5d615a6471c"
    )
    JsonNullable<UUID> restClientId,

    @Schema(
        description = "Template for the path from which the data product will be fetched",
        examples = "v1/animal/{{uid}}"
    )
    @Size(max = 1000)
    JsonNullable<String> restClientPathTemplate,

    @Schema(
        // A scalar JsonNullable renders as `type: object`, which makes the OpenAPI generator parse this
        // '{'-leading example into an object and emit an object example for a String field. Pinning the type
        // to STRING keeps the example a string. Only needed for JSON-shaped examples; see JsonNullableOpenApiFilter.
        type = SchemaType.STRING,
        description = "Template of the request body for fetching the data product",
        examples = "{\"search\":{\"uid\":\"{{uid}}\"}}"
    )
    @Size(max = 1000)
    JsonNullable<String> restClientRequestTemplate,

    @Schema(
        description = "The http-method used to fetch the data product",
        examples = "GET"
    )
    JsonNullable<RestClientMethodCodeEnum> restClientMethodCode,

    @Schema(
        description = "The flow code used for authorizing data retrieval",
        examples = "UID_BASED_PRE_VALIDATION"
    )
    JsonNullable<FlowCodeEnum> flowCode,

    @Schema(
        description = "Template for the path for retrieving updates to the data product",
        examples = "v1/animal-updates/{{uid}}?since={{LAST_CHANGED_SINCE}}"
    )
    @Size(max = 1000)
    JsonNullable<String> restClientChangeDetectionPathTemplate,

    @Schema(
        description = "List of relevant product links"
    )
    @Size(max = 5)
    JsonNullable<List<@Valid LinkDto>> links,

    @Schema(
        description = "Extended product details"
    )
    JsonNullable<@Valid DataProductExtendedDescriptionDto> extendedDescription,

    @Schema(
        description = "If a consent is required for this data product",
        examples = "true"
    )
    // Bound at draft time; immutable once active for every role (@Null for both patch groups).
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    @Null(groups = ValidationSchemaGenerator.PatchAsProvider.class)
    @Null(groups = ValidationSchemaGenerator.PatchAsAdmin.class)
    JsonNullable<Boolean> consentRequired,

    @Schema(
        description = "If a payment is required for this data product",
        examples = "true"
    )
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    JsonNullable<Boolean> paymentRequired,

    @Schema(
        description = "Pricing basis of the data product"
    )
    JsonNullable<@Valid DataProductDescriptionDto> pricingBasis

) {

  @AssertTrue(groups = ValidationSchemaGenerator.Submit.class, message = "pricingBasis must not be null when paymentRequired is true")
  public boolean isPricingBasisPresentWhenPaymentRequired() {
    return !Boolean.TRUE.equals(unwrap(paymentRequired)) || unwrap(pricingBasis) != null;
  }

  @AssertTrue(groups = ValidationSchemaGenerator.Submit.class, message = "pricingBasis must be null when paymentRequired is not true")
  public boolean isPricingBasisAbsentWhenPaymentNotRequired() {
    return Boolean.TRUE.equals(unwrap(paymentRequired)) || unwrap(pricingBasis) == null;
  }

  private static <T> T unwrap(JsonNullable<T> nullable) {
    return nullable == null ? null : nullable.orElse(null);
  }
}
