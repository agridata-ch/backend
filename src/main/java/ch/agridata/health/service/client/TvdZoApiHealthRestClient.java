package ch.agridata.health.service.client;

import ch.agridata.common.filters.RestClientLoggingFilter;
import ch.agridata.health.service.HealthExceptionMapper;
import ch.agridata.product.dto.RestClientIdentifier;
import io.quarkus.oidc.client.filter.OidcClientFilter;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * REST client for the TVD ZO API's {@code /api-zo/health} endpoint, using OIDC authentication.
 *
 * @CommentLastReviewed 2026-10-21
 */
@RegisterRestClient(configKey = RestClientIdentifier.ConfigKeys.TVD_ZO_API)
@RegisterProvider(RestClientLoggingFilter.class)
@RegisterProvider(HealthExceptionMapper.class)
@OidcClientFilter("tvd-api")
@Path("/")
public interface TvdZoApiHealthRestClient {

  @GET
  @Path("/api-zo/health")
  Response healthCheck();

}
