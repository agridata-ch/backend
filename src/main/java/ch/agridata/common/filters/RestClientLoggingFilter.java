package ch.agridata.common.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;

/**
 * Logs REST client requests and responses with configurable detail levels.
 * - INFO: logs URL, method, status, and duration
 * - DEBUG: additionally logs request body
 * - TRACE: additionally logs response body
 *
 * @CommentLastReviewed 2025-10-15
 */
@ApplicationScoped
@Slf4j
public class RestClientLoggingFilter implements ClientRequestFilter, ClientResponseFilter {

  private static final String START_TIME_PROPERTY = "restClientLoggingFilter.startTime";
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Override
  public void filter(ClientRequestContext requestContext) throws IOException {
    requestContext.setProperty(START_TIME_PROPERTY, System.currentTimeMillis());

    if (!log.isInfoEnabled()) {
      return;
    }

    String method = requestContext.getMethod();
    String uri = requestContext.getUri().toString();

    var logBuilder = log.atInfo()
        .addKeyValue("operation", "rest.client.request")
        .addKeyValue("method", method)
        .addKeyValue("uri", uri);

    if (log.isDebugEnabled() && requestContext.hasEntity()) {
      String requestBody = getEntityAsString(requestContext);
      logBuilder = logBuilder.addKeyValue("body", requestBody);
    }

    logBuilder.log("message=REST Client Request: {} {}", method, uri);
  }

  @Override
  public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
    if (!log.isInfoEnabled()) {
      return;
    }

    String method = requestContext.getMethod();
    String uri = requestContext.getUri().toString();
    int status = responseContext.getStatus();
    Long startTime = (Long) requestContext.getProperty(START_TIME_PROPERTY);
    long duration = startTime != null ? System.currentTimeMillis() - startTime : -1;

    StringBuilder logMessage = new StringBuilder();
    logMessage.append("REST Client Response: ").append(method).append(" ").append(uri).append(" -> Status: ").append(status)
        .append(", Duration: ").append(duration).append(" ms");

    var logBuilder = log.atInfo()
        .addKeyValue("operation", "rest.client.response")
        .addKeyValue("method", method)
        .addKeyValue("uri", uri)
        .addKeyValue("Duration", duration)
        .addKeyValue("status", status);

    if (log.isTraceEnabled() && responseContext.hasEntity()) {
      String responseBody = getResponseBodyAsString(responseContext);
      logBuilder = logBuilder.addKeyValue("body", responseBody);
    }
    logBuilder.log("message={}", logMessage);

  }

  private String getEntityAsString(ClientRequestContext requestContext) {
    Object entity = requestContext.getEntity();
    if (entity == null) {
      return "";
    }

    // If entity is already a String, compact it if it's JSON
    if (entity instanceof String) {
      return compactIfJson((String) entity);
    }
    try {
      return OBJECT_MAPPER.writeValueAsString(entity);
    } catch (Exception e) {
      return entity.toString();
    }
  }

  private String getResponseBodyAsString(ClientResponseContext responseContext) throws IOException {
    if (!responseContext.hasEntity()) {
      return "";
    }
    InputStream entityStream = responseContext.getEntityStream();
    if (entityStream == null) {
      return "";
    }
    byte[] bodyBytes = entityStream.readAllBytes();
    String body = new String(bodyBytes, StandardCharsets.UTF_8);
    responseContext.setEntityStream(new ByteArrayInputStream(bodyBytes));
    return compactIfJson(body);
  }

  private String compactIfJson(String body) {
    if (body == null || body.isEmpty()) {
      return "";
    }
    String trimmed = body.trim();
    if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
      try {
        return OBJECT_MAPPER.writeValueAsString(OBJECT_MAPPER.readTree(trimmed));
      } catch (Exception e) {
        // fall through
      }
    }
    // Collapse whitespace for non-JSON strings
    return trimmed.replace('\r', ' ').replace('\n', ' ').replaceAll("\\s{2,}", " ");
  }

}
