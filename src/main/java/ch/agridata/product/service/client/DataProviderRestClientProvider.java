package ch.agridata.product.service.client;

import ch.agridata.product.api.DataProviderRestClient;
import ch.agridata.product.api.DataProviderRestClientProviderApi;
import ch.agridata.product.dto.RestClientIdentifier;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Provides instances of configured data provider REST clients. It centralizes client selection through identifiers.
 *
 * @CommentLastReviewed 2026-05-26
 */
@ApplicationScoped
public class DataProviderRestClientProvider implements DataProviderRestClientProviderApi {

  private final AgisApiRestClient agisApiRestClient;
  private final TvdAnimalTracingApiRestClient tvdAnimalTracingApiRestClient;
  private final TvdZoApiRestClient tvdZoApiRestClient;
  private final AcontrolApiRestClient acontrolApiRestClient;

  @Inject
  public DataProviderRestClientProvider(@RestClient AgisApiRestClient agisApiRestClient,
                                        @RestClient TvdAnimalTracingApiRestClient tvdAnimalTracingApiRestClient,
                                        @RestClient TvdZoApiRestClient tvdZoApiRestClient,
                                        @RestClient AcontrolApiRestClient acontrolApiRestClient) {
    this.agisApiRestClient = agisApiRestClient;
    this.tvdAnimalTracingApiRestClient = tvdAnimalTracingApiRestClient;
    this.tvdZoApiRestClient = tvdZoApiRestClient;
    this.acontrolApiRestClient = acontrolApiRestClient;
  }

  public DataProviderRestClient get(RestClientIdentifier restClientIdentifier) {
    return switch (restClientIdentifier) {
      case AGIS_API -> agisApiRestClient;
      case TVD_ANIMAL_TRACING_API -> tvdAnimalTracingApiRestClient;
      case TVD_ZO_API -> tvdZoApiRestClient;
      case ACONTROL_API -> acontrolApiRestClient;
    };
  }

}
