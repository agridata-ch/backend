package ch.agridata.datatransferv2.service.task;

import ch.agridata.datatransferv2.service.AgridataContext;
import ch.agridata.product.dto.RestClientIdentifier;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Integration-only override that replaces a data consumer's identity toward the TVD clients with a shared dummy UID.
 *
 * <p>Background: the TVD validates every incoming request against the data consumers registered on their side, keyed by the consumer UID.
 * A request whose UID is unknown to the TVD is rejected. On the test environment the TVD does not want to register every real data consumer
 * individually, so they maintain a single shared dummy LegalUnit instead. This override lets real data consumers test on the integration
 * environment against that dummy: it swaps the outgoing consumer identity (the {@code AGRIDATA-CONSUMER-UID} header and the
 * {@code recipientUid} request parameter) for the configured dummy UID, so the TVD recognises the request while the consumer still
 * authenticates and is resolved with its own real UID everywhere else in agridata.ch.
 *
 * <p>The override is a strict no-op unless the active profile is one of {@link #ALLOWED_PROFILES} and it is explicitly enabled via
 * configuration. The profile check is a hard safety net so the override can never activate outside the integration environment, even if the
 * feature flag were misconfigured. Consumer UIDs listed in {@code excluded-consumer-uids} keep their real UID, for consumers that the TVD
 * has registered individually after all.
 *
 * @CommentLastReviewed 2026-07-30
 */
@Slf4j
@ApplicationScoped
public class OverrideTvdConsumerUidTask {

  private static final List<String> ALLOWED_PROFILES = List.of("local", "test", "develop", "testing", "integration");
  private static final Set<RestClientIdentifier> TVD_CLIENTS =
      Set.of(RestClientIdentifier.TVD_ANIMAL_TRACING_API, RestClientIdentifier.TVD_ZO_API);
  private static final String RECIPIENT_UID_PARAM = "recipientUid";

  @ConfigProperty(name = "quarkus.profile")
  String activeProfile;

  @ConfigProperty(name = "agridata.tvd.consumer-uid-override.enabled", defaultValue = "false")
  boolean enabled;

  @ConfigProperty(name = "agridata.tvd.consumer-uid-override.dummy-uid")
  Optional<String> dummyUid;

  @ConfigProperty(name = "agridata.tvd.consumer-uid-override.excluded-consumer-uids")
  Optional<List<String>> excludedConsumerUids;

  /**
   * Overrides, in place, the data consumer's identity carried on the context (the consumer UID and the {@code recipientUid} request
   * parameter) with the configured dummy UID, so the outgoing TVD request identifies the shared dummy instead of the real consumer.
   */
  public void apply(AgridataContext context) {
    var consumerUid = context.getConsumerUid();
    if (!isActive()) {
      log.debug("Skipping TVD consumer UID override: override is not active for the current profile/configuration.");
      return;
    }
    if (!targetsTvd(context)) {
      log.debug(
          "Skipping TVD consumer UID override: request targets rest client '{}', which is not a TVD client.",
          context.getProductProviderConfiguration().restClientIdentifierCode());
      return;
    }
    if (isBlank(consumerUid)) {
      log.debug("Skipping TVD consumer UID override: no consumer UID present on the context.");
      return;
    }
    if (isExcluded(consumerUid)) {
      log.debug("Skipping TVD consumer UID override: consumer UID '{}' is on the excluded list.", consumerUid);
      return;
    }

    var dummy = dummyUid.orElseThrow();
    context.setConsumerUid(dummy);
    log.info("TVD consumer UID override active: replaced consumer UID '{}' with dummy UID '{}'.", consumerUid, dummy);

    Map<String, String> requestParameters = context.getRequestParameters();
    if (requestParameters != null) {
      requestParameters.computeIfPresent(
          RECIPIENT_UID_PARAM,
          (key, previousValue) -> {
            log.info(
                "TVD consumer UID override active: replaced request parameter '{}' '{}' with dummy UID '{}'.",
                RECIPIENT_UID_PARAM,
                previousValue,
                dummy);
            return dummy;
          });
    }
  }

  private boolean isActive() {
    return ALLOWED_PROFILES.contains(activeProfile) && enabled && dummyUid.isPresent();
  }

  private boolean targetsTvd(AgridataContext context) {
    var identifier = RestClientIdentifier.valueOf(context.getProductProviderConfiguration().restClientIdentifierCode());
    return TVD_CLIENTS.contains(identifier);
  }

  private boolean isExcluded(String consumerUid) {
    return excludedConsumerUids.map(uids -> uids.contains(consumerUid)).orElse(false);
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
