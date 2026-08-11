package ch.agridata.health.service.client;

import ch.agridata.common.filters.RestClientLoggingFilter;
import ch.agridata.health.service.HealthExceptionMapper;
import io.quarkus.oidc.client.filter.OidcClientFilter;
import jakarta.ws.rs.Encoded;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * REST client for AGIS' per-subsystem {@code /health} endpoints, reusing the AGIS base URL and OIDC authentication.
 *
 * @CommentLastReviewed 2026-08-11
 */
@RegisterRestClient(configKey = "agis-api")
@RegisterProvider(RestClientLoggingFilter.class)
@RegisterProvider(HealthExceptionMapper.class)
@OidcClientFilter("agis-api")
@Path("/")
public interface AgisApiRestClient {

  @GET
  @Path("/{path}")
  Response get(@Encoded @PathParam("path") String path);

}
