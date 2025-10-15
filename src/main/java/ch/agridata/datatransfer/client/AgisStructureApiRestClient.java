package ch.agridata.datatransfer.client;

import ch.agridata.common.filters.RestClientLoggingFilter;
import ch.agridata.common.jsonfieldrewrite.JsonFieldRewriteInboundFilter;
import ch.agridata.common.jsonfieldrewrite.JsonFieldRewriteOutboundFilter;
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
 * REST client for interacting with the AGIS Structure API. It applies security, request/response filters, and error mapping for reliable
 * communication.
 *
 * @CommentLastReviewed 2025-10-15
 */

@RegisterRestClient(configKey = "agis-structure-api")
@RegisterProvider(RestClientLoggingFilter.class)
@RegisterProvider(JsonFieldRewriteInboundFilter.class)
@RegisterProvider(JsonFieldRewriteOutboundFilter.class)
@RegisterProvider(DataProviderExceptionMapper.class)
@OidcClientFilter("agis-api")
@Path("/")
public interface AgisStructureApiRestClient extends DataProviderRestClient {

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("{path}")
  @Override
  Object post(@PathParam("path") String path,
              Object request);
}
