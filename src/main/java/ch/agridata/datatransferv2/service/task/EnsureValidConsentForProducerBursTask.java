package ch.agridata.datatransferv2.service.task;

import ch.agridata.agreement.api.ConsentRequestApi;
import ch.agridata.agreement.dto.ConsentRequestFundamentalViewDto;
import ch.agridata.common.exceptions.ConsentNotGrantedException;
import ch.agridata.datatransferv2.service.AgridataContext;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * Verifies that consent has been granted for all producer BURs in the request payload.
 * Checks against the valid data requests and additionally validates that
 * the union of all granted consent periods covers the entire requested date range without gaps.
 *
 * @CommentLastReviewed 2026-03-09
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
    var requestedDateRange = context.getRequestedDateRange();

    log.debug("Checking consent for producerBurs={}, dataRequestIds={}, requestedDateRange={}",
        producerBurs, validDataRequestIds, requestedDateRange);

    Map<String, List<ConsentRequestFundamentalViewDto>> grantedConsentsByBur = validDataRequestIds.stream()
        .flatMap(dataRequestId -> consentRequestApi.getGrantedConsentRequestsOfDataRequestAndProducersBurs(
            dataRequestId, producerBurs.stream().toList()).stream())
        .collect(Collectors.groupingBy(ConsentRequestFundamentalViewDto::dataProducerBur));

    Set<String> bursWithSufficientConsent = producerBurs.stream()
        .filter(bur -> isRangeFullyCovered(grantedConsentsByBur.getOrDefault(bur, List.of()), requestedDateRange))
        .collect(Collectors.toSet());

    Set<String> bursWithMissingConsent = new TreeSet<>(producerBurs);
    bursWithMissingConsent.removeAll(bursWithSufficientConsent);

    if (!bursWithMissingConsent.isEmpty()) {
      log.warn("Consent not granted for producerBurs={}", bursWithMissingConsent);
      throw new ConsentNotGrantedException(bursWithMissingConsent);
    }

    log.debug("Consent verified for all {} producer BUR(s)", producerBurs.size());
    return context;
  }

  private boolean isRangeFullyCovered(List<ConsentRequestFundamentalViewDto> consents, Range<@NotNull LocalDate> requestedRange) {
    RangeSet<@NotNull LocalDate> coveredRanges = TreeRangeSet.create();

    consents.stream()
        .filter(c -> c.grantedDataPeriodFrom() != null && c.grantedDataPeriodTo() != null)
        .forEach(c -> coveredRanges.add(Range.closedOpen(c.grantedDataPeriodFrom(), c.grantedDataPeriodTo().plusDays(1))));

    var normalizedRequest = Range.closedOpen(
        requestedRange.lowerEndpoint(),
        requestedRange.upperEndpoint().plusDays(1));

    return coveredRanges.encloses(normalizedRequest);
  }
}
