package ch.agridata.product.mapper;

import ch.agridata.product.dto.RestClientIdentifier;
import ch.agridata.product.persistence.RestClientEntity;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.config.Config;
import org.mapstruct.Named;

/**
 * Resolves the configured base URL for an external data-provider REST client, derived from the client's
 * {@link RestClientIdentifier} and the corresponding
 * {@code quarkus.rest-client.<configKey>.url} configuration value. It exists so that a {@code RestClientDto} is never
 * produced without its URL: it supports {@link RestClientMapper} during entity-to-DTO mapping and is not intended for
 * use outside the mapping layer. Resolution fails fast with an {@link IllegalStateException} when no URL is configured
 * for the client, surfacing missing or mistyped configuration at mapping time rather than allowing a null URL to reach
 * an API response.
 *
 * @CommentLastReviewed 2026-07-04
 */

@ApplicationScoped
@RequiredArgsConstructor
public class RestClientUrlResolver {
  private final Config config;

  @Named("resolveRestClientUrl")
  public String resolve(RestClientEntity entity) {
    RestClientIdentifier identifier = RestClientIdentifier.valueOf(entity.getCode());
    String code = entity.getCode();
    return config.getOptionalValue("quarkus.rest-client." + identifier.configKey() + ".url", String.class)
        .orElseThrow(() -> new IllegalStateException("Missing REST client URL config for code: '" + code + "'"));
  }
}
