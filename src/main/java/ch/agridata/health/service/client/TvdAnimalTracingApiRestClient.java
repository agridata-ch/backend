package ch.agridata.health.service.client;

import ch.agridata.common.filters.RestClientLoggingFilter;
import ch.agridata.health.service.HealthExceptionMapper;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * REST client for TVD's {@code /health-check} endpoint; the caller supplies a fixed {@code Authorization} token
 * (no OIDC), reusing the TVD base URL.
 *
 * @CommentLastReviewed 2026-08-11
 */
@RegisterRestClient(configKey = "tvd-animal-tracing-api")
@RegisterProvider(RestClientLoggingFilter.class)
@RegisterProvider(HealthExceptionMapper.class)
@Path("/")
public interface TvdAnimalTracingApiRestClient {

  @GET
  @Path("/health-check")
  Response healthCheck(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization);

}
