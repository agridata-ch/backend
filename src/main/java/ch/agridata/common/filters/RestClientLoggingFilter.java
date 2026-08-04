package ch.agridata.common.filters;

import static io.quarkiverse.loggingjson.providers.KeyValueStructuredArgument.kv;

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
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Logs REST client requests and responses with configurable detail levels.
 * - INFO: logs URL, method, status, and duration
 * - DEBUG: additionally logs request body
 * - TRACE: additionally logs response body
 *
 * <p>Each detail is emitted as a dedicated JSON property via {@code kv(...)} structured arguments,
 * keeping the log message itself clean.</p>
 *
 * @CommentLastReviewed 2026-08-03
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
    String query = requestContext.getUri().getQuery();

    var logArguments = new ArrayList<>(List.of(
        method,
        uri,
        kv("operation", "rest.client.request"),
        kv("method", method),
        kv("uri", uri)));

    if (query != null && !query.isEmpty()) {
      logArguments.add(kv("query", query));
    }

    if (log.isDebugEnabled() && requestContext.hasEntity()) {
      logArguments.add(kv("body", getEntityAsString(requestContext)));
    }

    log.info("rest.client.request {} {}", logArguments.toArray());
  }

  @Override
  public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
    if (!log.isInfoEnabled()) {
      return;
    }

    String method = requestContext.getMethod();
    String uri = requestContext.getUri().toString();
    String query = requestContext.getUri().getQuery();
    int status = responseContext.getStatus();
    Long startTime = (Long) requestContext.getProperty(START_TIME_PROPERTY);
    long duration = startTime != null ? System.currentTimeMillis() - startTime : -1;

    var logArguments = new ArrayList<>(List.of(
        method,
        uri,
        status,
        duration,
        kv("operation", "rest.client.response"),
        kv("method", method),
        kv("uri", uri),
        kv("duration", duration),
        kv("status", status)));

    if (query != null && !query.isEmpty()) {
      logArguments.add(kv("query", query));
    }

    if (log.isTraceEnabled() && responseContext.hasEntity()) {
      logArguments.add(kv("body", getResponseBodyAsString(responseContext)));
    }

    log.info("rest.client.response {} {} -> {} ({} ms)", logArguments.toArray());
  }

  private String getEntityAsString(ClientRequestContext requestContext) {
    Object entity = requestContext.getEntity();
    if (entity == null) {
      return "";
    }

    // If entity is already a String, compact it if it's JSON
    if (entity instanceof String str) {
      return compactIfJson(str);
    }
    try {
      return OBJECT_MAPPER.writeValueAsString(entity);
    } catch (Exception _) {
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
      } catch (Exception _) {
        // fall through
      }
    }
    // Collapse whitespace for non-JSON strings
    return trimmed.replace('\r', ' ').replace('\n', ' ').replaceAll("\\s{2,}", " ");
  }

}
