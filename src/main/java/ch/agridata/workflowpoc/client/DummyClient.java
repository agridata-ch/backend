package ch.agridata.workflowpoc.client;

import io.quarkus.oidc.client.filter.OidcClientFilter;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * Dummy for workflow poc
 *
 * @CommentLastReviewed 2026-01-07
 */
@RegisterRestClient(configKey = "agis-structure-api")
@OidcClientFilter("agis-api")
@Path("/")
public interface DummyClient extends DataProviderRestClient {

  @POST
  @Path("{path}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.WILDCARD)
  Response post(@PathParam("path") String path, Object request);
}
