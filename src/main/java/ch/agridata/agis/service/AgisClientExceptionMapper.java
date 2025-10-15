package ch.agridata.agis.service;

import ch.agridata.common.exceptions.ExternalWebServiceException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

/**
 * Maps error responses from the AGIS API into application-specific exceptions. It extracts error details from HTTP responses and wraps them
 * in a consistent exception type for higher-level handling.
 *
 * @CommentLastReviewed 2025-08-25
 */
@Slf4j
public class AgisClientExceptionMapper implements ResponseExceptionMapper<RuntimeException> {

  @Override
  public RuntimeException toThrowable(Response response) {
    String body = "<no response body>";
    try {
      body = response.readEntity(String.class);
    } catch (Exception e) {
      log.warn("Failed to read error response body", e);
    }

    return new ExternalWebServiceException("AGIS service error: HTTP " + response.getStatus() + " – " + body);
  }
}
