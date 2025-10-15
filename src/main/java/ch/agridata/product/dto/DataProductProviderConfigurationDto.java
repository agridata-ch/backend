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
        description = "Path for the request",
        examples = {"structure"}
    )
    String restClientPath,

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
    String restClientRequestTemplate
)

    implements Serializable {
}
