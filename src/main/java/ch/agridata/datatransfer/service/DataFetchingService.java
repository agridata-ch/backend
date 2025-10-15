package ch.agridata.datatransfer.service;

import static ch.agridata.datatransfer.client.DataProviderRestClientFactory.RestClientIdentifier;

import ch.agridata.datatransfer.client.DataProviderRestClientFactory;
import ch.agridata.product.api.DataProductApi;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;

/**
 * Builds and sends requests to data provider APIs. It replaces template placeholders with request parameters, constructs JSON payloads, and
 * invokes the appropriate REST clients.
 *
 * @CommentLastReviewed 2025-08-25
 */
@ApplicationScoped
@RequiredArgsConstructor
public class DataFetchingService {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final DataProviderRestClientFactory dataProviderRestClientFactory;
  private final DataProductApi dataProductApi;

  public Object fetchData(UUID productId, Map<String, String> params) {

    var dataProduct = dataProductApi.getProviderConfigurationById(productId);

    var restClientIdentifierCode = RestClientIdentifier.valueOf(dataProduct.restClientIdentifierCode());
    var restClientMethod = dataProduct.restClientMethodCode();
    var restClientPath = dataProduct.restClientPath();
    var restClientRequestTemplate = dataProduct.restClientRequestTemplate();
    var request = buildRequest(restClientRequestTemplate, params);
    return invokeRestClient(restClientIdentifierCode, restClientMethod, restClientPath, request);
  }

  private JsonNode buildRequest(String restClientRequestTemplate, Map<String, String> params) {
    var request = replacePlaceholders(restClientRequestTemplate, params);

    try {
      return MAPPER.readTree(request);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Could not build request", e);
    }
  }

  private String replacePlaceholders(String template, Map<String, String> params) {
    Pattern p = Pattern.compile("\\{\\{\\s*([a-zA-Z0-9_\\-.]+)\\s*}}");
    Matcher m = p.matcher(template);
    StringBuilder sb = new StringBuilder();
    while (m.find()) {
      String key = m.group(1);
      String replacement = params.get(key);
      if (replacement == null) {
        throw new IllegalArgumentException("Parameter '" + key + "' not found");
      } else {
        m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
      }
    }
    m.appendTail(sb);
    return sb.toString();
  }

  private Object invokeRestClient(RestClientIdentifier restClientIdentifierCode,
                                  String restClientMethod,
                                  String restClientPath,
                                  JsonNode request) {
    return switch (restClientMethod) {
      case "POST" -> dataProviderRestClientFactory.get(restClientIdentifierCode).post(restClientPath, request);
      case "GET" -> dataProviderRestClientFactory.get(restClientIdentifierCode).get(restClientPath, request);
      case null, default -> throw new IllegalArgumentException("Unsupported rest client method: " + restClientMethod);
    };
  }

}
