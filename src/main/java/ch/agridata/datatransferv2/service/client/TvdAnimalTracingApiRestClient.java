package ch.agridata.datatransferv2.service.client;

import ch.agridata.common.filters.RestClientLoggingFilter;
import io.quarkus.oidc.client.filter.OidcClientFilter;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Encoded;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * REST client for interacting with the TVD Animal Tracing API. It applies security, request/response filters,
 * and error mapping for reliable communication.
 *
 * @CommentLastReviewed 2026-02-04
 */

@RegisterRestClient(configKey = "tvd-animal-tracing-api")
@RegisterProvider(RestClientLoggingFilter.class)
@RegisterProvider(DataProviderExceptionMapper.class)
@OidcClientFilter("tvd-api")
@Path("/")
public interface TvdAnimalTracingApiRestClient extends DataProviderRestClient {

  @GET
  @Path("/{path}")
  @Override
  Response get(@Encoded @PathParam("path") String path,
               @BeanParam Headers headers);
}
