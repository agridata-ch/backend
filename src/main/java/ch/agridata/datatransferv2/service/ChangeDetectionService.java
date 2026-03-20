package ch.agridata.datatransferv2.service;

import static ch.agridata.datatransferv2.client.DataProviderRestClientProvider.RestClientIdentifier;

import ch.agridata.agreement.api.ConsentRequestApi;
import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.datatransferv2.client.DataProviderRestClient;
import ch.agridata.datatransferv2.client.DataProviderRestClientProvider;
import ch.agridata.datatransferv2.dto.ProducerIdentifier;
import ch.agridata.product.api.DataProductApi;
import ch.agridata.product.dto.DataProductProviderConfigurationDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.GenericType;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Resolves producer IDs for which either a new consent was granted or data has changed at the upstream provider since a given timestamp.
 * The result is the union of producer IDs with new consents and producer IDs with upstream data changes that also have a valid consent.
 *
 * @CommentLastReviewed 2026-03-04
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class ChangeDetectionService {

  private static final LocalDateTime EPOCH = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
  private static final String CHANGE_DETECTION_PATH_PLACEHOLDER = "{{LAST_CHANGED_SINCE_DATE_TIME}}";

  private final DataProductApi dataProductApi;
  private final ConsentRequestApi consentRequestApi;
  private final DataProviderRestClientProvider dataProviderRestClientProvider;
  private final AgridataSecurityIdentity securityIdentity;

  public List<ProducerIdentifier> getModifiedProducers(UUID productId, LocalDate since) {
    var config = dataProductApi.getProviderConfigurationById(productId);

    if (config.restClientChangeDetectionPathTemplate() == null) {
      throw new IllegalArgumentException("Change detection is not supported for product=" + productId);
    }

    return switch (FlowEnum.valueOf(config.flowCode())) {
      case UID_BASED_PRE_VALIDATION,
           UID_BASED_POST_VALIDATION -> getUidResult(config, since);
      case BUR_BASED_POST_VALIDATION,
           UNBOUND_POST_VALIDATION -> throw new IllegalArgumentException("Change detection is not supported for product=" + productId);
    };

  }

  private List<ProducerIdentifier> getUidResult(DataProductProviderConfigurationDto config, LocalDate since) {
    var productId = config.id();

    var allGrantedConsentRequestUids = new HashSet<>(
        consentRequestApi.getGrantedConsentRequestUidsForProductOfCurrentConsumerSince(productId, EPOCH));
    var newGrantedConsentRequestUids = new HashSet<>(
        consentRequestApi.getGrantedConsentRequestUidsForProductOfCurrentConsumerSince(productId, since.atStartOfDay()));

    log.debug("Change detection for productId={}: allGrantedConsentRequestUids={}, newGrantedConsentRequestUids={}", productId,
        allGrantedConsentRequestUids.size(), newGrantedConsentRequestUids.size());

    var changedUidsFromProviderWithGrantedConsent = fetchChangedUidsFromProvider(config, since).stream()
        .filter(allGrantedConsentRequestUids::contains)
        .collect(Collectors.toSet());

    log.debug("Change detection for productId={}: changedUidsFromProviderWithGrantedConsent={}", productId,
        changedUidsFromProviderWithGrantedConsent.size());

    var resultingUids = Stream.concat(newGrantedConsentRequestUids.stream(), changedUidsFromProviderWithGrantedConsent.stream())
        .collect(Collectors.toSet());

    return resultingUids.stream().sorted().map(uid -> new ProducerIdentifier(uid, null)).toList();
  }

  private List<String> fetchChangedUidsFromProvider(DataProductProviderConfigurationDto config, LocalDate since) {
    var resolvedPath = config.restClientChangeDetectionPathTemplate().replace(
        CHANGE_DETECTION_PATH_PLACEHOLDER,
        URLEncoder.encode(since.atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), StandardCharsets.UTF_8));
    var restClientIdentifier = RestClientIdentifier.valueOf(config.restClientIdentifierCode());
    var client = dataProviderRestClientProvider.get(restClientIdentifier);
    var headers = DataProviderRestClient.Headers.builder()
        .agridataConsumerUid(securityIdentity.getUid().orElse(null))
        .agridataConsumerAgateLoginId(securityIdentity.getAgateLoginId())
        .build();

    log.debug("Calling change detection endpoint: path={}, client={}", resolvedPath, restClientIdentifier);

    return client.get(resolvedPath, headers).readEntity(new GenericType<>() {
    });
  }
}
