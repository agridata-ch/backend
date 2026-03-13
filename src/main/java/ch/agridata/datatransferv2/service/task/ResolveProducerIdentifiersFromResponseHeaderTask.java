package ch.agridata.datatransferv2.service.task;

import ch.agridata.common.exceptions.ExternalWebServiceException;
import ch.agridata.datatransferv2.service.AgridataContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Resolves producer UIDs and BURs from the data provider response headers.
 * Parses the JSON arrays in {@code AGRIDATA-RESPONSE-PRODUCER-UIDS} and
 * {@code AGRIDATA-RESPONSE-PRODUCER-BURS} and sets them on the context for downstream validation.
 * At least one of the two headers must be non-empty.
 *
 * @CommentLastReviewed 2026-02-27
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class ResolveProducerIdentifiersFromResponseHeaderTask implements UnaryOperator<AgridataContext> {

  private static final String AGRIDATA_RESPONSE_PRODUCER_UIDS_HEADER = "AGRIDATA-RESPONSE-PRODUCER-UIDS";
  private static final String AGRIDATA_RESPONSE_PRODUCER_BURS_HEADER = "AGRIDATA-RESPONSE-PRODUCER-BURS";

  private final ObjectMapper objectMapper;

  @Override
  public AgridataContext apply(final AgridataContext context) {

    String uidsHeaderValue = Optional.ofNullable(context.getResponseHeaders().get(AGRIDATA_RESPONSE_PRODUCER_UIDS_HEADER)).orElse("[]");
    String bursHeaderValue = Optional.ofNullable(context.getResponseHeaders().get(AGRIDATA_RESPONSE_PRODUCER_BURS_HEADER)).orElse("[]");

    List<String> producerUids = parseHeader(uidsHeaderValue, "producer UIDs");
    context.setProducerUids(producerUids);
    log.debug("Resolved ProducerUids={}", producerUids);

    List<String> producerBurs = parseHeader(bursHeaderValue, "producer BURs");
    context.setProducerBurs(producerBurs);
    log.debug("Resolved ProducerBurs={}", producerBurs);

    if (producerUids.isEmpty() && producerBurs.isEmpty()) {
      throw new ExternalWebServiceException("Neither " + AGRIDATA_RESPONSE_PRODUCER_UIDS_HEADER
          + " nor " + AGRIDATA_RESPONSE_PRODUCER_BURS_HEADER + " header values are present in provider response");
    }

    return context;
  }

  private List<String> parseHeader(final String value, final String description) {
    try {
      return objectMapper.readValue(value, new TypeReference<>() {
      });
    } catch (JsonProcessingException e) {
      throw new ExternalWebServiceException("Failed to parse " + description + " from response header", e);
    }
  }
}
