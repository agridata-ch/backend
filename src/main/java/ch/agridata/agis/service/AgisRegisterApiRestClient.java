package ch.agridata.agis.service;

import ch.agridata.agis.dto.AgisPersonFarmResponseType;
import ch.agridata.agis.dto.AgisRegisterDataRequest;
import ch.agridata.common.jsonfieldrewrite.JsonFieldRewriteInboundFilter;
import ch.agridata.common.jsonfieldrewrite.JsonFieldRewriteOutboundFilter;
import io.quarkus.oidc.client.filter.OidcClientFilter;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * Declares the REST client interface for the AGIS Register API. It defines the endpoint for submitting register data requests, with filters
 * for JSON field rewriting, error handling, and OIDC authentication.
 *
 * @CommentLastReviewed 2025-08-25
 */

@RegisterRestClient(configKey = "agis-register-api")
@RegisterProvider(JsonFieldRewriteInboundFilter.class)
@RegisterProvider(JsonFieldRewriteOutboundFilter.class)
@RegisterProvider(AgisClientExceptionMapper.class)
@OidcClientFilter("agis-api")
@Path("/")
public interface AgisRegisterApiRestClient {

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("register")
  AgisPersonFarmResponseType register(AgisRegisterDataRequest request);

}
