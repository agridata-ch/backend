package ch.agridata.uidregister.service;

import ch.agridata.common.filters.RestClientLoggingFilter;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * MicroProfile REST client for the public UID-Register SOAP service ({@code IPublicServices}). It posts a pre-built SOAP 1.1 envelope and
 * returns the raw {@link Response} so that both successful payloads and SOAP fault envelopes (HTTP 500) can be parsed by
 * {@link SoapEnvelopeSupport}.
 *
 * @CommentLastReviewed 2026-06-16
 */
@RegisterRestClient(configKey = "uid-register")
@RegisterProvider(RestClientLoggingFilter.class)
@Path("/")
public interface UidRegisterSoapClient {

  String GET_BY_UID_SOAP_ACTION = "http://www.uid.admin.ch/xmlns/uid-wse/IPublicServices/GetByUID";

  @POST
  @Consumes(MediaType.TEXT_XML)
  @Produces(MediaType.TEXT_XML)
  @ClientHeaderParam(name = "SOAPAction", value = "\"" + GET_BY_UID_SOAP_ACTION + "\"")
  Response getByUid(String soapEnvelope);
}
