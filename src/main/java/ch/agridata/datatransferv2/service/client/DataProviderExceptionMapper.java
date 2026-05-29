package ch.agridata.datatransferv2.service.client;

import ch.agridata.common.exceptions.DataProviderException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

/**
 * Maps provider error responses into system exceptions. It captures status codes and response bodies to aid debugging.
 *
 * @CommentLastReviewed 2026-02-04
 */
@Slf4j
public class DataProviderExceptionMapper implements ResponseExceptionMapper<RuntimeException> {

  @Override
  public RuntimeException toThrowable(Response response) {
    String body = "<no response body>";
    try {
      body = response.readEntity(String.class);
    } catch (Exception e) {
      log.warn("Failed to read error response body", e);
    }

    return new DataProviderException(response.getStatus(), body);
  }
}
