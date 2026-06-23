package ch.agridata.datatransferv2.service.task;

import ch.agridata.common.exceptions.ExternalWebServiceException;
import ch.agridata.datatransferv2.service.AgridataContext;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Resolves producer UIDs and BURs from the data provider response headers.
 * Parses the comma-separated values in {@code AGRIDATA-RESPONSE-PRODUCER-UIDS} and
 * {@code AGRIDATA-RESPONSE-PRODUCER-BURS} and sets them on the context for downstream validation.
 * Each individual entry must consist of alphanumeric characters only.
 * At least one of the two headers must be non-empty.
 *
 * @CommentLastReviewed 2026-06-04
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class ResolveProducerIdentifiersFromResponseHeaderTask implements UnaryOperator<AgridataContext> {

  private static final String AGRIDATA_RESPONSE_PRODUCER_UIDS_HEADER = "AGRIDATA-RESPONSE-PRODUCER-UIDS";
  private static final String AGRIDATA_RESPONSE_PRODUCER_BURS_HEADER = "AGRIDATA-RESPONSE-PRODUCER-BURS";
  private static final Pattern ALPHANUMERIC = Pattern.compile("[a-zA-Z0-9]+");

  @Override
  public AgridataContext apply(final AgridataContext context) {

    String uidsHeaderValue = findHeaderValue(context.getResponseHeaders(), AGRIDATA_RESPONSE_PRODUCER_UIDS_HEADER).orElse("");
    String bursHeaderValue = findHeaderValue(context.getResponseHeaders(), AGRIDATA_RESPONSE_PRODUCER_BURS_HEADER).orElse("");

    List<String> producerUids = parseHeader(uidsHeaderValue, AGRIDATA_RESPONSE_PRODUCER_UIDS_HEADER);
    context.setProducerUids(producerUids);
    log.debug("Resolved ProducerUids={}", producerUids);

    List<String> producerBurs = parseHeader(bursHeaderValue, AGRIDATA_RESPONSE_PRODUCER_BURS_HEADER);
    context.setProducerBurs(producerBurs);
    log.debug("Resolved ProducerBurs={}", producerBurs);

    if (producerUids.isEmpty() && producerBurs.isEmpty()) {
      throw new ExternalWebServiceException("Neither " + AGRIDATA_RESPONSE_PRODUCER_UIDS_HEADER
          + " nor " + AGRIDATA_RESPONSE_PRODUCER_BURS_HEADER + " header values are present in provider response");
    }

    return context;
  }

  private static List<String> parseHeader(final String value, final String headerName) {
    List<String> entries = Arrays.stream(value.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .toList();
    for (String entry : entries) {
      if (!ALPHANUMERIC.matcher(entry).matches()) {
        throw new ExternalWebServiceException(headerName + " header contains invalid value '" + entry
            + "': only alphanumeric characters are allowed (entries separated by commas)");
      }
    }
    return entries;
  }

  private static Optional<String> findHeaderValue(final Map<String, String> headers, final String headerName) {
    return headers.entrySet().stream()
        .filter(e -> headerName.equalsIgnoreCase(e.getKey()))
        .map(Map.Entry::getValue)
        .findFirst();
  }
}
