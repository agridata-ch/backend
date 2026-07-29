package ch.agridata.datatransferv2.service.task;

import ch.agridata.common.exceptions.ExternalWebServiceException;
import ch.agridata.datatransferv2.service.AgridataContext;
import ch.agridata.datatransferv2.service.utils.ResponseHeaderParser;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.function.UnaryOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Resolves producer UIDs from the data provider response headers.
 * Parses the comma-separated values in {@code AGRIDATA-RESPONSE-PRODUCER-UIDS} and sets them on the
 * context for downstream validation. Each individual entry must consist of alphanumeric characters only.
 * The header must be present.
 *
 * @CommentLastReviewed 2026-07-29
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class ResolveProducerUidsFromResponseHeaderTask implements UnaryOperator<AgridataContext> {

  private static final String AGRIDATA_RESPONSE_PRODUCER_UIDS_HEADER = "AGRIDATA-RESPONSE-PRODUCER-UIDS";

  @Override
  public AgridataContext apply(final AgridataContext context) {

    String headerValue = ResponseHeaderParser.findHeaderValue(context.getResponseHeaders(), AGRIDATA_RESPONSE_PRODUCER_UIDS_HEADER)
        .orElseThrow(() -> new ExternalWebServiceException(
            AGRIDATA_RESPONSE_PRODUCER_UIDS_HEADER + " header is not present in provider response"));

    List<String> producerUids = ResponseHeaderParser.parseAlphanumericCsv(headerValue, AGRIDATA_RESPONSE_PRODUCER_UIDS_HEADER);
    context.setProducerUids(producerUids);
    log.debug("Resolved ProducerUids={}", producerUids);

    return context;
  }
}
