package ch.agridata.product.dto;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.UUID;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Encapsulates provider-specific configuration for retrieving a product. It includes client identifiers, HTTP method, path, and request
 * templates for integration with provider systems.
 *
 * @CommentLastReviewed 2025-08-25
 */
@Builder
public record DataProductProviderConfigurationDto(
    @Schema(
        description = "Unique identifier of the product",
        examples = {"3fa85f64-5717-4562-b3fc-2c963f66afb7"}
    )
    @NotNull
    UUID id,

    @Schema(
        description = "Identifier of the rest client",
        examples = {"AGIS_STRUCTURE"}
    )
    String restClientIdentifierCode,

    @Schema(
        description = "Http method for the request",
        examples = {"POST"}
    )
    String restClientMethodCode,

    @Schema(
        description = "Path template for the request",
        examples = {"/farm-data/uid/{{uid}}"}
    )
    String restClientPathTemplate,

    @Schema(
        description = "Body template for the request",
        examples = {"""
            {
              "surveyYear": "{{year}}",
              "ids": {
                "ber": "{{bur}}"
              },
              "dataTypes": {
                "structureType": ["animalData"]
              }
            }"""}
    )
    String restClientRequestTemplate,

    @Schema(
        description = "Flow code for the data transfer",
        examples = {"UID_BASED_PRE_VALIDATION"}
    )
    String flowCode,

    @Schema(
        description = "Path template for detecting changes",
        examples = {"/farm-data/changes?since={{LAST_CHANGED_SINCE_DATE_TIME}}"}
    )
    String restClientChangeDetectionPathTemplate)

    implements Serializable {
}
