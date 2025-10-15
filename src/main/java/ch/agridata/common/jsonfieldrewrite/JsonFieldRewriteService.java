package ch.agridata.common.jsonfieldrewrite;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import lombok.extern.slf4j.Slf4j;

/**
 * Applies configurable rewrite rules to JSON content. It processes objects and arrays recursively and respects whitelists for unaltered
 * fields.
 *
 * @CommentLastReviewed 2025-08-25
 */
@ApplicationScoped
@Slf4j
public class JsonFieldRewriteService {

  public String rewriteJson(String json, JsonFieldRewriteConfig config) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    JsonNode rewrittenJson = mapper.readTree(json);

    rewriteRecursively(
        rewrittenJson,
        config.whiteListProperties(),
        config.rewriteRules(),
        config.fallbackRewriteRule()
    );

    log.debug("Original JSON:\n{}\nRewritten JSON:\n{}",
        mapper.writeValueAsString(mapper.readTree(json)),
        mapper.writeValueAsString(rewrittenJson));

    return mapper.writeValueAsString(rewrittenJson);
  }

  private void rewriteRecursively(JsonNode node,
                                  List<String> whitelistProperties,
                                  Map<String, UnaryOperator<JsonNode>> rewriteRules,
                                  UnaryOperator<JsonNode> defaultRewriteRule) {

    if (node.isObject()) {
      ObjectNode obj = (ObjectNode) node;

      obj.properties().forEach(entry -> {
        String key = entry.getKey();
        JsonNode value = entry.getValue();

        if (whitelistProperties.contains(key)) {
          return;
        }

        if (value.isObject() || value.isArray()) {
          rewriteRecursively(value, whitelistProperties, rewriteRules, defaultRewriteRule);
        } else {
          UnaryOperator<JsonNode> rewriteRule = rewriteRules.getOrDefault(key, defaultRewriteRule);
          obj.set(key, rewriteRule.apply(value));
        }
      });
    } else if (node.isArray()) {
      for (JsonNode item : node) {
        rewriteRecursively(item, whitelistProperties, rewriteRules, defaultRewriteRule);
      }
    }
  }

}
