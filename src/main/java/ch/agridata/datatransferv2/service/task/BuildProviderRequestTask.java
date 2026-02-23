package ch.agridata.datatransferv2.service.task;

import static ch.agridata.datatransferv2.client.DataProviderRestClientProvider.RestClientIdentifier;

import ch.agridata.datatransferv2.client.DataProviderRestClient;
import ch.agridata.datatransferv2.client.DataProviderRestClientProvider;
import ch.agridata.datatransferv2.service.AgridataContext;
import ch.agridata.product.api.DataProductApi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Builds and prepares requests to data provider APIs. Replaces template placeholders
 * with request parameters and constructs the appropriate REST client invocation.
 *
 * @CommentLastReviewed 2026-02-04
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class BuildProviderRequestTask implements UnaryOperator<AgridataContext> {

  private final DataProviderRestClientProvider dataProviderRestClientProvider;
  private final DataProductApi dataProductApi;

  @Override
  public AgridataContext apply(final AgridataContext context) {
    log.debug("Building provider request for productId={}", context.getProductId());

    var request = buildProviderRequest(context);
    context.setProviderRequest(request);

    return context;
  }

  private Supplier<Response> buildProviderRequest(AgridataContext context) {
    var productId = context.getProductId();
    var dataProduct = dataProductApi.getProviderConfigurationById(productId);
    var requestParameters = context.getRequestParameters();
    var requestMethod = dataProduct.restClientMethodCode();

    Set<String> usedKeys = new HashSet<>();
    var requestPath = replacePlaceholders(dataProduct.restClientPath(), requestParameters, usedKeys);
    var requestBody = replacePlaceholders(dataProduct.restClientRequestTemplate(), requestParameters, usedKeys);
    var finalPath = appendUnusedAsQueryParams(requestPath, requestParameters, usedKeys);

    var restClientIdentifierCode = RestClientIdentifier.valueOf(dataProduct.restClientIdentifierCode());
    var client = dataProviderRestClientProvider.get(restClientIdentifierCode);
    var headers = DataProviderRestClient.Headers.builder()
        .agridataConsumerAgateLoginId(context.getConsumerAgateLoginId())
        .agridataConsumerUid(context.getConsumerUid())
        .build();

    log.debug("Provider request configured: method={}, path={}, clientId={}",
        requestMethod, finalPath, restClientIdentifierCode);

    return switch (requestMethod) {
      case "POST" -> () -> client.post(finalPath, headers, requestBody);
      case "GET" -> () -> client.get(finalPath, headers);
      default -> {
        log.warn("Unsupported REST client method: {}", requestMethod);
        throw new IllegalArgumentException("Unsupported rest client method: " + requestMethod);
      }
    };
  }

  private String replacePlaceholders(String template, Map<String, String> params, Set<String> usedKeys) {
    if (template == null) {
      return null;
    }

    Pattern p = Pattern.compile("\\{\\{\\s*([a-zA-Z0-9_\\-.]+)\\s*}}");
    Matcher m = p.matcher(template);
    StringBuilder sb = new StringBuilder();
    while (m.find()) {
      String key = m.group(1);
      String replacement = params.get(key);
      if (replacement == null) {
        log.warn("Template placeholder '{}' not found in request parameters", key);
        throw new IllegalArgumentException("Parameter '" + key + "' not found in request");
      }
      usedKeys.add(key);
      m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
    }
    m.appendTail(sb);
    return sb.toString();
  }

  private String appendUnusedAsQueryParams(String path, Map<String, String> params, Set<String> usedKeys) {
    UriBuilder uriBuilder = UriBuilder.fromUri(path);
    params.forEach((key, value) -> {
      if (!usedKeys.contains(key)) {
        uriBuilder.queryParam(key, value);
      }
    });
    return uriBuilder.build().toString();
  }
}
