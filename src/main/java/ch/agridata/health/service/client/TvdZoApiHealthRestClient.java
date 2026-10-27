package ch.agridata.health.service.client;

import ch.agridata.common.filters.RestClientLoggingFilter;
import ch.agridata.health.service.HealthExceptionMapper;
import ch.agridata.product.dto.RestClientIdentifier;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * REST client for the TVD ZO API's {@code /api-zo/health} endpoint; the caller supplies a fixed {@code Authorization}
 * token.
 *
 * @CommentLastReviewed 2026-10-27
 */
@RegisterRestClient(configKey = RestClientIdentifier.ConfigKeys.TVD_ZO_API)
@RegisterProvider(RestClientLoggingFilter.class)
@RegisterProvider(HealthExceptionMapper.class)
@Path("/")
public interface TvdZoApiHealthRestClient {

  @GET
  @Path("/api-zo/health")
  Response healthCheck(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization);

}
