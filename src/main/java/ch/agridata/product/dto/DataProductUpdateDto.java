package ch.agridata.product.dto;

import ch.agridata.common.utils.ValidationSchemaGenerator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Represents the DataProductUpdateDto, a data transfer object used to carry information
 * related to the creation and update of a data product. The fields in this record define various
 * properties and configurations needed for identifying and interacting with a specific
 * data product.
 *
 * @CommentLastReviewed 2026-06-11
 */

@Schema(description = "Data transfer object representing a data product")
@Builder
public record DataProductUpdateDto(
    @Schema(
        description = "Name of the data product"
    )
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    @Valid
    DataProductNameDto name,

    @Schema(
        description = "Description of the data product"
    )
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
    @Valid
    DataProductDescriptionDto description,

    @Schema(
        description = "UUID of DataSourceSystem",
        examples = "5335d715-e95c-4777-a424-ab73f2ff5618"
    )
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
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
    @NotNull(groups = ValidationSchemaGenerator.Submit.class)
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
    String restClientChangeDetectionPathTemplate
) {

}
