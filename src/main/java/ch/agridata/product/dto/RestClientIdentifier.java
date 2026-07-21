package ch.agridata.product.dto;

/**
 * Shared vocabulary identifying the external data-provider REST clients that serve as sources for data products.
 * Each constant corresponds to one configured client and carries its {@code configKey}, which links the identifier
 * to the client's {@code quarkus.rest-client.<configKey>.*} configuration (base URL, OIDC settings, and so on).
 * This enum lives in {@code common} because it is shared vocabulary co-owned across modules: {@code datatransferv2}
 * dispatches requests to the corresponding client, while {@code product} persists a reference to it and resolves the
 * client's URL for its API responses. When adding a client, keep the {@code configKey} in sync with both the
 * {@code @RegisterRestClient(configKey = ...)} declaration and the matching configuration block.
 *
 * @CommentLastReviewed 2026-07-04
 */

public enum RestClientIdentifier {
  AGIS_API(ConfigKeys.AGIS_API),
  TVD_ANIMAL_TRACING_API(ConfigKeys.TVD_ANIMAL_TRACING_API),
  TVD_ZO_API(ConfigKeys.TVD_ZO_API),
  ACONTROL_API(ConfigKeys.ACONTROL_API);

  /**
   * Compile-time constants for the {@code configKey} of each external data-provider REST client. These exist so the same
   * key can be referenced both by {@link RestClientIdentifier}'s constants and by the corresponding
   * {@code @RegisterRestClient(configKey = ...)} declarations, giving the annotation and the enum a single source of
   * truth that the compiler keeps in sync. Each value must match a {@code quarkus.rest-client.<configKey>.*}
   * configuration block; that binding is only verified at runtime, so keep the constant, the annotation, and the
   * configuration aligned when adding or renaming a client.
   *
   * @CommentLastReviewed 2026-07-04
   */

  public static final class ConfigKeys {
    public static final String AGIS_API = "agis-api";
    public static final String TVD_ANIMAL_TRACING_API = "tvd-animal-tracing-api";
    public static final String TVD_ZO_API = "tvd-zo-api";
    public static final String ACONTROL_API = "acontrol-api";

    private ConfigKeys() {
    }
  }

  private final String configKey;

  RestClientIdentifier(String configKey) {
    this.configKey = configKey;
  }

  public String configKey() {
    return configKey;
  }
}
