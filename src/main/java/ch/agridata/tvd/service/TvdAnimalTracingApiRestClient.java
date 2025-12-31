package ch.agridata.tvd.service;

import ch.agridata.common.filters.RestClientLoggingFilter;
import ch.agridata.tvd.dto.TvdEquidOwnerUidDto;
import io.quarkus.oidc.client.filter.OidcClientFilter;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * Declares the REST client interface for the TVD Animal Tracing API. It defines the endpoint for retrieving equid owner legal unit data by
 * AGATE login ID, including REST client logging, centralized error mapping, and OIDC-based authentication.
 *
 * @CommentLastReviewed 2025-12-29
 */

@RegisterRestClient(configKey = "tvd-animal-tracing-api")
@RegisterProvider(RestClientLoggingFilter.class)
@RegisterProvider(TvdClientExceptionMapper.class)
@OidcClientFilter("tvd-api")
@Path("/")
public interface TvdAnimalTracingApiRestClient {

  @GET
  @Path("customer/legalunits/equidowner/{agateLoginId}")
  @Produces(MediaType.APPLICATION_JSON)
  TvdEquidOwnerUidDto fetchEquidOwnerLegalUnits(@PathParam("agateLoginId") String agateLoginId);

}
