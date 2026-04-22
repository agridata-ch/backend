package ch.agridata.datatransfer.service.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Provides instances of configured data provider REST clients. It centralizes client selection through identifiers.
 *
 * @CommentLastReviewed 2025-08-28
 */
@ApplicationScoped
public class DataProviderRestClientFactory {

  private final AgisApiRestClient agisApiRestClient;

  @Inject
  public DataProviderRestClientFactory(@RestClient AgisApiRestClient agisApiRestClient) {
    this.agisApiRestClient = agisApiRestClient;
  }

  public DataProviderRestClient get(RestClientIdentifier restClientIdentifier) {
    return switch (restClientIdentifier) {
      case AGIS_API -> agisApiRestClient;
    };
  }

  /**
   * Enumerates supported external rest clients that serve as sources for data products.
   *
   * @CommentLastReviewed 2025-08-28
   */
  public enum RestClientIdentifier {
    AGIS_API
  }

}
