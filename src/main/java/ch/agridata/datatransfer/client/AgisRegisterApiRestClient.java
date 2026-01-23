package ch.agridata.datatransfer.client;

import ch.agridata.common.filters.RestClientLoggingFilter;
import io.quarkus.oidc.client.filter.OidcClientFilter;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * REST client for interacting with the AGIS Register API. It applies security, request/response filters, and error mapping for reliable
 * communication.
 *
 * @CommentLastReviewed 2025-10-15
 */

@RegisterRestClient(configKey = "agis-register-api")
@RegisterProvider(RestClientLoggingFilter.class)
@RegisterProvider(DataProviderExceptionMapper.class)
@OidcClientFilter("agis-api")
@Path("/")
public interface AgisRegisterApiRestClient extends DataProviderRestClient {

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("{path}")
  @Override
  Object post(@PathParam("path") String path,
              Object request);
}
