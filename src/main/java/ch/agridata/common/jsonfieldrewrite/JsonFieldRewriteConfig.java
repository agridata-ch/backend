package ch.agridata.common.jsonfieldrewrite;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * Defines the configuration model for JSON field rewriting. It specifies whitelisted properties, rule mappings, and fallback strategies.
 *
 * @CommentLastReviewed 2025-08-25
 */
public record JsonFieldRewriteConfig(
    List<String> whiteListProperties,
    Map<String, UnaryOperator<JsonNode>> rewriteRules,
    UnaryOperator<JsonNode> fallbackRewriteRule) {
}
