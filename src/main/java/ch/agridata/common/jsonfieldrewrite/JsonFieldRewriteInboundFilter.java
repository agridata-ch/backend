package ch.agridata.common.jsonfieldrewrite;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Intercepts inbound responses to apply rewrite rules. It ensures data is consistently processed before use.
 *
 * @CommentLastReviewed 2025-08-25
 */
@ApplicationScoped
public class JsonFieldRewriteInboundFilter implements ClientResponseFilter {

  JsonFieldRewriteService jsonFieldRewriteService;
  JsonFieldRewriteConfigExtractor jsonFieldRewriteConfigExtractor;

  @ConfigProperty(name = "agridata.json-field-rewrite.enabled", defaultValue = "true")
  boolean jsonFieldRewriteEnabled;
  @ConfigProperty(name = "agridata.json-field-rewrite.config", defaultValue = "[]")
  String jsonFieldRewriteConfig;

  public JsonFieldRewriteInboundFilter(JsonFieldRewriteService jsonFieldRewriteService,
                                       JsonFieldRewriteConfigExtractor jsonFieldRewriteConfigExtractor) {
    this.jsonFieldRewriteService = jsonFieldRewriteService;
    this.jsonFieldRewriteConfigExtractor = jsonFieldRewriteConfigExtractor;
  }

  @Override
  public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
    if (!jsonFieldRewriteEnabled) {
      return;
    }

    if (responseContext.getStatus() < 200 || responseContext.getStatus() >= 300) {
      return;
    }

    var path = JsonFieldRewriteUtils.extractPathOrElseThrow(requestContext);
    var method = requestContext.getMethod();
    var rewriteConfig = jsonFieldRewriteConfigExtractor.extractConfig(jsonFieldRewriteConfig, path, method, "INBOUND");

    String originalJson = JsonFieldRewriteUtils.getJsonString(responseContext);

    try {
      String rewrittenJson = jsonFieldRewriteService.rewriteJson(originalJson, rewriteConfig);
      responseContext.setEntityStream(new ByteArrayInputStream(rewrittenJson.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      throw new WebApplicationException("JSON field rewriting (inbound) failed", e, 500);
    }
  }

}
