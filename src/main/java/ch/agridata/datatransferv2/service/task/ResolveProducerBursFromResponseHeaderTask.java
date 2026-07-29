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
 * Resolves producer BURs from the data provider response headers.
 * Parses the comma-separated values in {@code AGRIDATA-RESPONSE-PRODUCER-BURS} and sets them on the
 * context for downstream validation. Each individual entry must consist of alphanumeric characters only.
 * The header must be present.
 *
 * @CommentLastReviewed 2026-07-29
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class ResolveProducerBursFromResponseHeaderTask implements UnaryOperator<AgridataContext> {

  private static final String AGRIDATA_RESPONSE_PRODUCER_BURS_HEADER = "AGRIDATA-RESPONSE-PRODUCER-BURS";

  @Override
  public AgridataContext apply(final AgridataContext context) {

    String headerValue = ResponseHeaderParser.findHeaderValue(context.getResponseHeaders(), AGRIDATA_RESPONSE_PRODUCER_BURS_HEADER)
        .orElseThrow(() -> new ExternalWebServiceException(
            AGRIDATA_RESPONSE_PRODUCER_BURS_HEADER + " header is not present in provider response"));

    List<String> producerBurs = ResponseHeaderParser.parseAlphanumericCsv(headerValue, AGRIDATA_RESPONSE_PRODUCER_BURS_HEADER);
    context.setProducerBurs(producerBurs);
    log.debug("Resolved ProducerBurs={}", producerBurs);

    return context;
  }
}
