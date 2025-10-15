package ch.agridata.common.jsonfieldrewrite;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientResponseContext;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Provides utilities for extracting and serializing JSON payloads in client contexts. It supports both request and response processing.
 *
 * @CommentLastReviewed 2025-08-25
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonFieldRewriteUtils {

  public static String extractPathOrElseThrow(ClientRequestContext context) {
    return Optional.of(context)
        .map(ClientRequestContext::getUri)
        .map(URI::getPath)
        .orElseThrow(() -> new IllegalArgumentException("No path provided"));
  }

  public static String getJsonString(ClientResponseContext responseContext) throws IOException {
    byte[] originalBytes = responseContext.getEntityStream().readAllBytes();
    return new String(originalBytes, StandardCharsets.UTF_8);
  }

  public static String getJsonString(ClientRequestContext requestContext) {
    Object entity = requestContext.getEntity();
    try {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.writeValueAsString(entity);
    } catch (JsonProcessingException e) {
      throw new WebApplicationException("Failed to serialize request entity", e, 500);
    }
  }
}
