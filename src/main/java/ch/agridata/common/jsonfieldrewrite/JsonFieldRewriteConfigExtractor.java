package ch.agridata.common.jsonfieldrewrite;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import lombok.Getter;

/**
 * Extracts configuration for JSON field rewrite operations. It determines applicable rules for processing requests and responses.
 *
 * @CommentLastReviewed 2025-08-25
 */
@Getter
@ApplicationScoped
public class JsonFieldRewriteConfigExtractor {

  private static final String CONFIG_KEY_PATH = "path";
  private static final String CONFIG_KEY_METHOD = "method";
  private static final String CONFIG_KEY_DIRECTION = "direction";
  private static final String CONFIG_KEY_WHITELIST_PROPERTIES = "whitelistProperties";
  private static final String CONFIG_KEY_FALLBACK_REWRITE_RULE = "fallbackRewriteRule";
  private static final String CONFIG_KEY_REWRITE_RULES = "rewriteRules";
  private static final String CONFIG_KEY_RULE_PATTERN = "pattern";
  private static final String CONFIG_KEY_RULE_REPLACEMENT = "replaceWith";

  public JsonFieldRewriteConfig extractConfig(String configJson, String path, String method, String direction)
      throws JsonProcessingException {
    JsonNode matchedConfig = findMatchingConfig(configJson, path, method, direction);
    List<String> whitelist = extractWhitelist(matchedConfig);
    Map<String, UnaryOperator<JsonNode>> rewriteRules = extractRewriteRules(matchedConfig);
    UnaryOperator<JsonNode> fallbackRewriteRule = extractFallbackRewriteRule(matchedConfig);

    return new JsonFieldRewriteConfig(whitelist, rewriteRules, fallbackRewriteRule);
  }

  private JsonNode findMatchingConfig(String configJson, String path, String method, String direction) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    JsonNode configArray = mapper.readTree(configJson);

    for (JsonNode config : configArray) {
      if (matchesConfig(config, path, method, direction)) {
        return config;
      }
    }

    throw new IllegalArgumentException(
        "No json rewrite config found for path: " + path + ", method: " + method + ", direction: " + direction);
  }

  private boolean matchesConfig(JsonNode config, String path, String method, String direction) {
    return path.equalsIgnoreCase(config.get(CONFIG_KEY_PATH).asText())
        && method.equalsIgnoreCase(config.get(CONFIG_KEY_METHOD).asText())
        && direction.equalsIgnoreCase(config.get(CONFIG_KEY_DIRECTION).asText());
  }

  private List<String> extractWhitelist(JsonNode config) {
    List<String> whitelist = new ArrayList<>();
    for (JsonNode node : config.withArray(CONFIG_KEY_WHITELIST_PROPERTIES)) {
      whitelist.add(node.asText());
    }
    return whitelist;
  }

  private Map<String, UnaryOperator<JsonNode>> extractRewriteRules(JsonNode config) {
    Map<String, UnaryOperator<JsonNode>> rewriteRules = new HashMap<>();
    JsonNode rulesNode = config.get(CONFIG_KEY_REWRITE_RULES);
    if (rulesNode == null || !rulesNode.isObject()) {
      return rewriteRules;
    }

    for (var entry : rulesNode.properties()) {
      String fieldName = entry.getKey();
      JsonNode rules = entry.getValue();

      if (rules.isArray()) {
        UnaryOperator<JsonNode> combinedRewriteRule = createCombinedRewriteRule(rules);
        rewriteRules.put(fieldName, combinedRewriteRule);
      }
    }

    return rewriteRules;
  }

  private UnaryOperator<JsonNode> extractFallbackRewriteRule(JsonNode config) {
    JsonNode fallbackNode = config.get(CONFIG_KEY_FALLBACK_REWRITE_RULE);

    if (fallbackNode == null || fallbackNode.isNull()) {
      return UnaryOperator.identity();
    }

    JsonNode replacementNode = fallbackNode.get(CONFIG_KEY_RULE_REPLACEMENT);

    if (replacementNode == null) {
      return UnaryOperator.identity();
    }

    if (replacementNode.isNull()) {
      return json -> null;
    }

    if (replacementNode.isTextual()) {
      String replacement = replacementNode.asText();
      return node -> new TextNode(replacement);
    }

    return UnaryOperator.identity();
  }

  private UnaryOperator<JsonNode> createCombinedRewriteRule(JsonNode rules) {
    List<UnaryOperator<JsonNode>> rewriteRules = new ArrayList<>();

    for (JsonNode rule : rules) {
      String pattern = rule.get(CONFIG_KEY_RULE_PATTERN).asText();
      String replacement = rule.get(CONFIG_KEY_RULE_REPLACEMENT).asText();
      Pattern regex = Pattern.compile(pattern);

      rewriteRules.add(node -> {
        if (node == null || !node.isTextual()) {
          return node;
        }
        String input = node.asText();
        String output = regex.matcher(input).replaceAll(replacement);
        return new TextNode(output);
      });
    }

    return node -> {
      for (UnaryOperator<JsonNode> rule : rewriteRules) {
        JsonNode result = rule.apply(node);
        if (!result.equals(node)) {
          return result;
        }
      }
      return node;
    };
  }

}
