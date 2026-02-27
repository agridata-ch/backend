package ch.agridata.datatransferv2.service.task;

import ch.agridata.datatransferv2.service.AgridataContext;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.function.UnaryOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Extracts the producer UID from the request parameters and sets it on the context.
 *
 * @CommentLastReviewed 2026-02-04
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class ResolveRequestedProducerUidTask implements UnaryOperator<AgridataContext> {

  @Override
  public AgridataContext apply(final AgridataContext context) {
    var uid = context.getRequestParameters().get("uid");
    context.setProducerUids(List.of(uid));

    log.debug("Resolved producer UID from request: {}", uid);
    return context;
  }
}
