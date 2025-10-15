package ch.agridata.datatransfer.client;

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

  private final AgisRegisterApiRestClient agisRegisterApiRestClient;
  private final AgisStructureApiRestClient agisStructureApiRestClient;
  private final AgisEcoEthoApiRestClient agisEcoEthoApiRestClient;

  @Inject
  public DataProviderRestClientFactory(@RestClient AgisRegisterApiRestClient agisRegisterApiRestClient,
                                       @RestClient AgisStructureApiRestClient agisStructureApiRestClient,
                                       @RestClient AgisEcoEthoApiRestClient agisEcoEthoApiRestClient) {
    this.agisRegisterApiRestClient = agisRegisterApiRestClient;
    this.agisStructureApiRestClient = agisStructureApiRestClient;
    this.agisEcoEthoApiRestClient = agisEcoEthoApiRestClient;
  }

  public DataProviderRestClient get(RestClientIdentifier restClientIdentifier) {
    return switch (restClientIdentifier) {
      case AGIS_REGISTER_V1 -> agisRegisterApiRestClient;
      case AGIS_STRUCTURE_V1 -> agisStructureApiRestClient;
      case AGIS_ECO_ETHO_V1 -> agisEcoEthoApiRestClient;
    };
  }

  /**
   * Enumerates supported external rest clients that serve as sources for data products.
   *
   * @CommentLastReviewed 2025-08-28
   */
  public enum RestClientIdentifier {
    AGIS_REGISTER_V1,
    AGIS_STRUCTURE_V1,
    AGIS_ECO_ETHO_V1
  }

}
