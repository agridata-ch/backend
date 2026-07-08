package ch.agridata.product.dto;

import java.util.UUID;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Identifies a REST client used to call a data product endpoint.
 *
 * @param id   unique identifier of this REST client entry
 * @param code the configuration key matching the {@code configKey} attribute
 *             of the {@link org.eclipse.microprofile.rest.client.inject.RegisterRestClient}
 *             annotation on the corresponding client interface
 * @CommentLastReviewed 2026-06-11
 */

@Builder
public record RestClientDto(
    @Schema(
        description = "Unique identifier of the rest client",
        examples = {"3fa85f64-5717-4562-b3fc-2c963f66afb7"}
    )
    UUID id,

    @Schema(description = "Stable technical code of the rest client", examples = {"AGIS_API"})
    String code,
    @Schema(description = "Human-readable name of the REST client, intended for display in user interfaces", examples = {"Agis"})
    String displayName,
    @Schema(description = "Base URL of the external API accessed by this REST client", examples = {"http://example.com/agis"})
    String url
) {
}
