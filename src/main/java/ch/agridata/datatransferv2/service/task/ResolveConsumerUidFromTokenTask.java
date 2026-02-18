package ch.agridata.datatransferv2.service.task;

import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.datatransferv2.service.AgridataContext;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.function.UnaryOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Resolves the consumer UID from the authenticated user's security token.
 * This task extracts the UID claim and sets it on the context for downstream validation.
 *
 * @CommentLastReviewed 2026-02-04
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class ResolveConsumerUidFromTokenTask implements UnaryOperator<AgridataContext> {

  private final AgridataSecurityIdentity agridataSecurityIdentity;

  @Override
  public AgridataContext apply(final AgridataContext context) {
    String consumerUid = agridataSecurityIdentity.getUidOrElseThrow();
    context.setConsumerUid(consumerUid);
    context.setConsumerAgateLoginId(agridataSecurityIdentity.getAgateLoginId());

    log.debug("Resolved consumer UID={}, agateLoginId={}", consumerUid,
        context.getConsumerAgateLoginId());
    return context;
  }
}
