package ch.agridata.common.jsonfieldrewrite;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.MediaType;
import java.io.IOException;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Intercepts outbound client requests to apply rewrite rules. It modifies JSON payloads before transmission.
 *
 * @CommentLastReviewed 2025-08-25
 */
@ApplicationScoped
public class JsonFieldRewriteOutboundFilter implements ClientRequestFilter {

  JsonFieldRewriteService jsonFieldRewriteService;
  JsonFieldRewriteConfigExtractor jsonFieldRewriteConfigExtractor;

  @ConfigProperty(name = "agridata.json-field-rewrite.enabled", defaultValue = "true")
  boolean jsonFieldRewriteEnabled;
  @ConfigProperty(name = "agridata.json-field-rewrite.config", defaultValue = "[]")
  String jsonFieldRewriteConfig;

  public JsonFieldRewriteOutboundFilter(JsonFieldRewriteService jsonFieldRewriteService,
                                        JsonFieldRewriteConfigExtractor jsonFieldRewriteConfigExtractor) {
    this.jsonFieldRewriteService = jsonFieldRewriteService;
    this.jsonFieldRewriteConfigExtractor = jsonFieldRewriteConfigExtractor;
  }

  @Override
  public void filter(ClientRequestContext requestContext) throws IOException {
    if (!jsonFieldRewriteEnabled) {
      return;
    }

    var path = JsonFieldRewriteUtils.extractPathOrElseThrow(requestContext);
    var method = requestContext.getMethod();
    var rewriteConfig = jsonFieldRewriteConfigExtractor.extractConfig(jsonFieldRewriteConfig, path, method, "OUTBOUND");

    String originalJson = JsonFieldRewriteUtils.getJsonString(requestContext);

    try {
      String rewrittenJson = jsonFieldRewriteService.rewriteJson(originalJson, rewriteConfig);
      requestContext.setEntity(rewrittenJson, null, MediaType.APPLICATION_JSON_TYPE);
    } catch (Exception e) {
      throw new WebApplicationException("JSON field rewriting (outbound) failed", e, 500);
    }
  }

}
