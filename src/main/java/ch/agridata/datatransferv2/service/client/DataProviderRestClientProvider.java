package ch.agridata.datatransferv2.service.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Provides instances of configured data provider REST clients. It centralizes client selection through identifiers.
 *
 * @CommentLastReviewed 2026-02-04
 */
@ApplicationScoped
public class DataProviderRestClientProvider {

  private final AgisApiRestClient agisApiRestClient;
  private final TvdAnimalTracingApiRestClient tvdAnimalTracingApiRestClient;
  private final TvdZoApiRestClient tvdZoApiRestClient;

  @Inject
  public DataProviderRestClientProvider(@RestClient AgisApiRestClient agisApiRestClient,
                                        @RestClient TvdAnimalTracingApiRestClient tvdAnimalTracingApiRestClient,
                                        @RestClient TvdZoApiRestClient tvdZoApiRestClient) {
    this.agisApiRestClient = agisApiRestClient;
    this.tvdAnimalTracingApiRestClient = tvdAnimalTracingApiRestClient;
    this.tvdZoApiRestClient = tvdZoApiRestClient;
  }

  public DataProviderRestClient get(RestClientIdentifier restClientIdentifier) {
    return switch (restClientIdentifier) {
      case AGIS_API -> agisApiRestClient;
      case TVD_ANIMAL_TRACING_API -> tvdAnimalTracingApiRestClient;
      case TVD_ZO_API -> tvdZoApiRestClient;
    };
  }

  /**
   * Enumerates supported external rest clients that serve as sources for data products.
   *
   * @CommentLastReviewed 2026-02-04
   */
  public enum RestClientIdentifier {
    AGIS_API,
    TVD_ANIMAL_TRACING_API,
    TVD_ZO_API
  }

}
