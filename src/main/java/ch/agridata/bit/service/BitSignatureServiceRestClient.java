package ch.agridata.bit.service;

import ch.agridata.bit.dto.BitAddHashRequest;
import ch.agridata.bit.dto.BitAddHashResponse;
import ch.agridata.bit.dto.BitCheckSignStateRequest;
import ch.agridata.bit.dto.BitCheckSignStateResponse;
import ch.agridata.bit.dto.BitDropSignRequest;
import ch.agridata.bit.dto.BitDropSignResponse;
import ch.agridata.bit.dto.BitGetSignedHashesRequest;
import ch.agridata.bit.dto.BitGetSignedHashesResponse;
import ch.agridata.bit.dto.BitInitSignRequest;
import ch.agridata.bit.dto.BitInitSignResponse;
import ch.agridata.bit.dto.BitStartSignRequest;
import ch.agridata.bit.dto.BitStartSignResponse;
import ch.agridata.common.filters.RestClientLoggingFilter;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * MicroProfile REST client interface for the BIT evidence Signing API ({@code /secure/v1}).
 * Authentication is performed via mutual TLS (mTLS)
 * The REST client timeout must be configured to at least 120 seconds to support long-polling
 * on {@code checkSignState} (server holds the connection for up to 60 seconds per poll).
 *
 * @CommentLastReviewed 2026-04-09
 */
@RegisterRestClient(configKey = "bit-signature-api")
@RegisterProvider(RestClientLoggingFilter.class)
@RegisterProvider(BitClientExceptionMapper.class)
@Path("/secure/v1")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface BitSignatureServiceRestClient {

  @POST
  @Path("/initSign")
  BitInitSignResponse initSign(BitInitSignRequest request);

  @POST
  @Path("/addHash")
  BitAddHashResponse addHash(BitAddHashRequest request);

  @POST
  @Path("/startSign")
  BitStartSignResponse startSign(BitStartSignRequest request);

  @POST
  @Path("/checkSignState")
  BitCheckSignStateResponse checkSignState(BitCheckSignStateRequest request);

  @POST
  @Path("/getSignedHashes")
  BitGetSignedHashesResponse getSignedHashes(BitGetSignedHashesRequest request);

  @POST
  @Path("/dropSign")
  BitDropSignResponse dropSign(BitDropSignRequest request);
}
