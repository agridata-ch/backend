package ch.agridata.datatransferv2.service.task;

import ch.agridata.datatransferv2.service.AgridataContext;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.function.UnaryOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Extracts the producer BUR from the request parameters and sets it on the context.
 *
 * @CommentLastReviewed 2026-02-25
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class ResolveRequestedProducerBurTask implements UnaryOperator<AgridataContext> {

  @Override
  public AgridataContext apply(final AgridataContext context) {
    var bur = context.getRequestParameters().get("bur");
    context.setProducerBurs(List.of(bur));

    log.debug("Resolved producer BUR from request: {}", bur);
    return context;
  }
}
