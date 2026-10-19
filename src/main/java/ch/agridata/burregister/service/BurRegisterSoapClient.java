package ch.agridata.burregister.service;

import ch.agridata.common.filters.RestClientLoggingFilter;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * MicroProfile REST client for the BurWeb query service ({@code QueryServiceV1X8}). It posts a pre-built SOAP 1.2 envelope with HTTP
 * Basic Auth and returns the raw {@link Response} so that both result and fault envelopes can be parsed by
 * {@link BurRegisterSoapSupport}.
 *
 * @CommentLastReviewed 2026-10-09
 */
@RegisterRestClient(configKey = "bur-register")
@RegisterProvider(RestClientLoggingFilter.class)
@Path("/")
public interface BurRegisterSoapClient {

  String GET_LOCAL_UNIT_BY_UID_ACTION = "http://burweb2.admin.ch/IQueryServiceV1X8/GetLocalUnitByUid";

  @POST
  @Consumes("application/soap+xml; charset=utf-8; action=\"" + GET_LOCAL_UNIT_BY_UID_ACTION + "\"")
  @Produces("application/soap+xml")
  @ClientHeaderParam(name = "Authorization", value = "{basicAuth}")
  Response getLocalUnitByUid(String soapEnvelope);

  /**
   * Builds the Basic-Auth header from the configured BUR credentials.
   */
  default String basicAuth() {
    var config = ConfigProvider.getConfig();
    var credentials = config.getValue("agridata.bur-register.username", String.class)
        + ":" + config.getValue("agridata.bur-register.password", String.class);
    return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
  }
}
