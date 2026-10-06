package ch.agridata.product.service.client;

import ch.agridata.product.api.DataProviderRestClient;
import ch.agridata.product.api.DataProviderRestClientProviderApi;
import ch.agridata.product.dto.RestClientIdentifier;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Provides instances of configured data provider REST clients. It centralizes client selection through identifiers and limits the
 * number of concurrent requests per data provider.
 *
 * @CommentLastReviewed 2026-10-02
 */
@ApplicationScoped
public class DataProviderRestClientProvider implements DataProviderRestClientProviderApi {

  private final AgisApiRestClient agisApiRestClient;
  private final TvdAnimalTracingApiRestClient tvdAnimalTracingApiRestClient;
  private final TvdZoApiRestClient tvdZoApiRestClient;
  private final AcontrolApiRestClient acontrolApiRestClient;
  private final Map<RestClientIdentifier, Semaphore> permitsPerProvider;

  @Inject
  public DataProviderRestClientProvider(@RestClient AgisApiRestClient agisApiRestClient,
                                        @RestClient TvdAnimalTracingApiRestClient tvdAnimalTracingApiRestClient,
                                        @RestClient TvdZoApiRestClient tvdZoApiRestClient,
                                        @RestClient AcontrolApiRestClient acontrolApiRestClient,
                                        @ConfigProperty(name = "agridata.product.data-provider.max-concurrent-requests")
                                        int maxConcurrentRequests) {
    this.agisApiRestClient = agisApiRestClient;
    this.tvdAnimalTracingApiRestClient = tvdAnimalTracingApiRestClient;
    this.tvdZoApiRestClient = tvdZoApiRestClient;
    this.acontrolApiRestClient = acontrolApiRestClient;
    this.permitsPerProvider = Arrays.stream(RestClientIdentifier.values())
        .collect(Collectors.toMap(Function.identity(), identifier -> new Semaphore(maxConcurrentRequests)));
  }

  public DataProviderRestClient get(RestClientIdentifier restClientIdentifier) {
    return new BulkheadDataProviderRestClient(restClientIdentifier, clientFor(restClientIdentifier),
        permitsPerProvider.get(restClientIdentifier));
  }

  private DataProviderRestClient clientFor(RestClientIdentifier restClientIdentifier) {
    return switch (restClientIdentifier) {
      case AGIS_API -> agisApiRestClient;
      case TVD_ANIMAL_TRACING_API -> tvdAnimalTracingApiRestClient;
      case TVD_ZO_API -> tvdZoApiRestClient;
      case ACONTROL_API -> acontrolApiRestClient;
    };
  }

}
