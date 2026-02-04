package ch.agridata.datatransferv2.client;

import ch.agridata.common.filters.RestClientLoggingFilter;
import io.quarkus.oidc.client.filter.OidcClientFilter;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Encoded;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * REST client for interacting with the AGIS API. It applies security, request/response filters, and error mapping for reliable
 * communication.
 *
 * @CommentLastReviewed 2026-02-04
 */

@RegisterRestClient(configKey = "agis-api")
@RegisterProvider(RestClientLoggingFilter.class)
@RegisterProvider(DataProviderExceptionMapper.class)
@OidcClientFilter("agis-api")
@Path("/")
public interface AgisApiRestClient extends DataProviderRestClient {

  @POST
  @Path("/{path}")
  @Override
  Response post(@Encoded @PathParam("path") String path,
                @BeanParam DataProviderRestClient.Headers headers,
                Object body);

  @GET
  @Path("/{path}")
  @Override
  Response get(@Encoded @PathParam("path") String path,
               @BeanParam DataProviderRestClient.Headers headers);
}
