package ch.agridata.product.dto;

import java.util.UUID;
import lombok.Builder;

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
    UUID id,
    String code
) {
}
