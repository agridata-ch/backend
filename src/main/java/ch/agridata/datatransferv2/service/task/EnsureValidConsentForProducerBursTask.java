package ch.agridata.datatransferv2.service.task;

import ch.agridata.agreement.api.ConsentRequestApi;
import ch.agridata.agreement.dto.ConsentRequestFundamentalViewDto;
import ch.agridata.common.exceptions.ConsentNotGrantedException;
import ch.agridata.datatransferv2.service.AgridataContext;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Verifies that consent has been granted for all producer BURs in the request payload.
 * Checks against the valid data requests and additionally validates that
 * each consent's UID-BUR relation period covers the requested date.
 *
 * @CommentLastReviewed 2026-02-26
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class EnsureValidConsentForProducerBursTask implements UnaryOperator<AgridataContext> {

  private final ConsentRequestApi consentRequestApi;

  @Override
  public AgridataContext apply(final AgridataContext context) {
    var producerBurs = new HashSet<>(context.getProducerBurs());
    var validDataRequestIds = context.getValidDataRequestIds();
    var requestedDate = context.getRequestedDate();

    log.debug("Checking consent for producerBurs={}, dataRequestIds={}, requestedDate={}",
        producerBurs, validDataRequestIds, requestedDate);

    Set<String> producerBursWithGrantedConsent = validDataRequestIds.stream()
        .map(dataRequestId -> consentRequestApi.getGrantedConsentRequestIdsOfDataRequestAndProducersBurs(
            dataRequestId,
            producerBurs.stream().toList()))
        .flatMap(List::stream)
        .filter(consent -> isConsentValidAt(consent, requestedDate))
        .map(ConsentRequestFundamentalViewDto::dataProducerBur)
        .collect(Collectors.toSet());

    Set<String> missingConsentBurs = new TreeSet<>(producerBurs);
    missingConsentBurs.removeAll(producerBursWithGrantedConsent);

    if (!missingConsentBurs.isEmpty()) {
      log.warn("Consent not granted for producerBurs={}", missingConsentBurs);
      throw new ConsentNotGrantedException(missingConsentBurs);
    }

    log.debug("Consent verified for all {} producer BUR(s)", producerBurs.size());
    return context;
  }

  private boolean isConsentValidAt(ConsentRequestFundamentalViewDto consent, LocalDate date) {
    LocalDateTime dateTime = date.atStartOfDay();
    return consent.uidBurRelationSince() != null
        && !consent.uidBurRelationSince().isAfter(dateTime)
        && (consent.uidBurRelationUntil() == null || !consent.uidBurRelationUntil().isBefore(dateTime));
  }
}
