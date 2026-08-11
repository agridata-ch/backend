package ch.agridata.health.service;

import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

/**
 * Maps a non-2xx health-endpoint response into a {@link HealthException} carrying the HTTP status.
 *
 * @CommentLastReviewed 2026-08-11
 */
@Slf4j
public class HealthExceptionMapper implements ResponseExceptionMapper<RuntimeException> {

  @Override
  public RuntimeException toThrowable(Response response) {
    return new HealthException(response.getStatus());
  }

}
