package ch.agridata.datatransferv2.service.task;

import ch.agridata.agreement.api.ConsentRequestApi;
import ch.agridata.agreement.dto.ConsentRequestFundamentalViewDto;
import ch.agridata.common.exceptions.ConsentNotGrantedException;
import ch.agridata.datatransferv2.service.AgridataContext;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Verifies that consent has been granted by all producer UIDs in the request payload.
 * Checks against the valid data requests found by EnsureValidDataRequestTask.
 * For consent-free products the check is skipped; instead the asynchronous
 * creation of a LEGALLY_PERMITTED consent request is enqueued for each producer UID.
 *
 * @CommentLastReviewed 2026-08-31
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class EnsureValidConsentForProducerUidsTask implements UnaryOperator<AgridataContext> {

  private final ConsentRequestApi consentRequestApi;

  @Override
  public AgridataContext apply(final AgridataContext context) {
    var producerUids = new HashSet<>(context.getProducerUids());
    var validDataRequestIds = context.getValidDataRequestIds();

    log.debug("Checking consent for producerUids={}, dataRequestIds={}",
        producerUids, validDataRequestIds);

    Set<String> missingConsentUids = findUidsWithMissingConsent(producerUids, validDataRequestIds);

    if (!missingConsentUids.isEmpty()) {
      return handleMissingConsent(context, producerUids, missingConsentUids);
    }

    log.debug("Consent verified for all {} producer UID(s)", producerUids.size());
    return context;
  }

  private Set<String> findUidsWithMissingConsent(Set<String> producerUids, List<UUID> validDataRequestIds) {
    Set<String> producerUidsWithGrantedConsent = consentRequestApi
        .getGrantedConsentRequestsOfDataRequestsAndProducersUids(validDataRequestIds, producerUids.stream().toList())
        .stream()
        .map(ConsentRequestFundamentalViewDto::dataProducerUid)
        .collect(Collectors.toSet());

    Set<String> missingConsentUids = new TreeSet<>(producerUids);
    missingConsentUids.removeAll(producerUidsWithGrantedConsent);
    return missingConsentUids;
  }

  private AgridataContext handleMissingConsent(AgridataContext context, Set<String> producerUids,
                                               Set<String> missingConsentUids) {
    if (!context.getProductProviderConfiguration().consentRequired()) {
      var dataRequestId = context.getValidDataRequestIds().getFirst();
      log.debug("Product is consent-free, enqueueing legally permitted consent requests for dataRequestId={}, producerUids={}",
          dataRequestId, producerUids);
      missingConsentUids.forEach(uid -> consentRequestApi.enqueueLegallyPermittedUidBasedConsentRequest(dataRequestId, uid));
      return context;
    }

    log.warn("Consent not granted for producerUids={}", missingConsentUids);
    throw new ConsentNotGrantedException(missingConsentUids);
  }
}
