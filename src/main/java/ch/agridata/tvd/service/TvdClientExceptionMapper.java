package ch.agridata.tvd.service;

import ch.agridata.common.exceptions.ExternalWebServiceException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

/**
 * Maps non-successful TVD REST client responses to a runtime exception. Attempts to read the response body for
 * diagnostic details and wraps the HTTP status and payload in an {@link ExternalWebServiceException}.
 *
 * @CommentLastReviewed 2025-12-29
 */
@Slf4j
public class TvdClientExceptionMapper implements ResponseExceptionMapper<RuntimeException> {

  @Override
  public RuntimeException toThrowable(Response response) {
    String body = "<no response body>";
    try {
      body = response.readEntity(String.class);
    } catch (Exception e) {
      log.warn("Failed to read error response body", e);
    }

    return new ExternalWebServiceException("TVD service error: HTTP " + response.getStatus() + " – " + body);
  }
}
