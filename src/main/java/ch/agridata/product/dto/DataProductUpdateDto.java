package ch.agridata.product.dto;

import ch.agridata.common.dto.LinkDto;
import ch.agridata.common.utils.ValidationSchemaGenerator;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.openapitools.jackson.nullable.JsonNullable;

/**
 * Single request body shared by every data-product mutation: create (POST), full replace of a draft (PUT) and partial update (PATCH).
 * The annotations here only describe the <em>shape</em> of valid data, expressed through Bean Validation groups:
 *
 * <ul>
 *   <li><b>Default group (no {@code groups} attribute)</b> &ndash; format checks such as {@code @Size} and nested {@code @Valid} that
 *       always apply, on every create, update and patch. The ungrouped {@code @NotNull} on {@code consentRequired} and
 *       {@code paymentRequired} belongs here too: both map to a {@code boolean} primitive on the entity, so a present {@code null}
 *       would unbox to a {@code NullPointerException} in the mapper. Because the value extractor skips an omitted field, the
 *       constraint reads as "if you send it, it must not be null" and still lets a draft leave the field out entirely.</li>
 *   <li><b>{@link ValidationSchemaGenerator.Submit}</b> &ndash; completeness ({@code @NotNull}): the fields that must be present before a
 *       product can go live. This is <em>not</em> checked while the product is a draft, but at the {@code DRAFT -> ACTIVE} transition
 *       (activation) and again on every PATCH of an active product, where it is applied to the <em>resulting</em> state so an active
 *       product cannot be patched back into an incomplete one. A draft may therefore be saved incomplete and completed later.</li>
 * </ul>
 *
 * <p><em>Who</em> may change which field in which state is not part of this DTO; see
 * {@code ch.agridata.product.service.DataProductFieldPermission}.
 *
 * <p>Every field is wrapped in {@link JsonNullable} so a PATCH can distinguish an <em>omitted</em> field (absent from the JSON body,
 * {@link JsonNullable#isPresent()} is {@code false} &ndash; leave the current value untouched) from an <em>explicit {@code null}</em>
 * (present with a {@code null} value &ndash; clear the field). {@code @JsonInclude(NON_ABSENT)} keeps undefined values out of the
 * serialized body. Bean Validation constraints are applied to the wrapped value through a registered {@code JsonNullable} value
 * extractor, so an omitted field is skipped by every constraint (including {@code @NotNull}).
 *
 * @CommentLastReviewed 2026-10-28
 */
@Schema(description = "Data transfer object representing a data product")
@Builder
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public record DataProductUpdateDto(
    @Schema(
        description = "Name of the data product"
    )
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    JsonNullable<@Valid DataProductNameDto> name,

    @Schema(
        description = "Description of the data product"
    )
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    JsonNullable<@Valid DataProductDescriptionDto> description,

    @Schema(
        description = "UUID of DataSourceSystem",
        examples = "5335d715-e95c-4777-a424-ab73f2ff5618"
    )
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
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
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    @NotNull
    JsonNullable<Boolean> consentRequired,

    @Schema(
        description = "If a payment is required for this data product",
        examples = "true"
    )
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    @NotNull
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
