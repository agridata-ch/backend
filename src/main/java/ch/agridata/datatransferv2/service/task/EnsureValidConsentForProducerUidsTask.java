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
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Verifies that consent has been granted by all producer UIDs in the request payload.
 * Checks against the valid data requests found by EnsureValidDataRequestTask.
 *
 * @CommentLastReviewed 2026-02-26
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

    Set<String> producerUidsWithGrantedConsent = validDataRequestIds.stream()
        .map(dataRequestId -> consentRequestApi.getGrantedConsentRequestsOfDataRequestAndProducersUids(
            dataRequestId,
            producerUids.stream().toList()))
        .flatMap(List::stream)
        .map(ConsentRequestFundamentalViewDto::dataProducerUid)
        .collect(Collectors.toSet());

    Set<String> missingConsentUids = new TreeSet<>(producerUids);
    missingConsentUids.removeAll(producerUidsWithGrantedConsent);

    if (!missingConsentUids.isEmpty()) {
      log.warn("Consent not granted for producerUids={}", missingConsentUids);
      throw new ConsentNotGrantedException(missingConsentUids);
    }

    log.debug("Consent verified for all {} producer UID(s)", producerUids.size());
    return context;
  }
}
