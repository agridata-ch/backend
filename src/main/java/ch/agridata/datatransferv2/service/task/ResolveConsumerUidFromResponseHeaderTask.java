package ch.agridata.datatransferv2.service.task;

import ch.agridata.common.exceptions.ExternalWebServiceException;
import ch.agridata.datatransferv2.service.AgridataContext;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.function.UnaryOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Resolves the consumer UID from the data provider response header.
 * This task extracts the UID and sets it on the context for downstream validation.
 *
 * @CommentLastReviewed 2026-02-23
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class ResolveConsumerUidFromResponseHeaderTask implements UnaryOperator<AgridataContext> {

  private static final String AGRIDATA_CONSUMER_UID_HEADER = "AGRIDATA-CONSUMER-UID";

  @Override
  public AgridataContext apply(final AgridataContext context) {

    String consumerUid = Optional.ofNullable(context.getResponseHeaders().get(AGRIDATA_CONSUMER_UID_HEADER))
        .orElseThrow(() -> new ExternalWebServiceException(AGRIDATA_CONSUMER_UID_HEADER + " header is absent in provider response"));
    context.setConsumerUid(consumerUid);
    log.debug("Resolved ConsumerUid={}", consumerUid);

    return context;
  }
}
