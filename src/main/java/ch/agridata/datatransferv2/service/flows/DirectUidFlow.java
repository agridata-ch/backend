package ch.agridata.datatransferv2.service.flows;

import static ch.agridata.common.filters.PreSecurityMdcFilter.REQUEST_ID_MDC_FIELD;

import ch.agridata.datatransferv2.service.AgridataContext;
import ch.agridata.datatransferv2.service.AgridataFlow;
import ch.agridata.datatransferv2.service.FlowEnum;
import ch.agridata.datatransferv2.service.Flowable;
import ch.agridata.datatransferv2.service.task.BuildProviderRequestTask;
import ch.agridata.datatransferv2.service.task.EnsureValidConsentForProducerUidsTask;
import ch.agridata.datatransferv2.service.task.EnsureValidConsumerRequestTask;
import ch.agridata.datatransferv2.service.task.EnsureValidDataRequestTask;
import ch.agridata.datatransferv2.service.task.ResolveConsumerUidFromTokenTask;
import ch.agridata.datatransferv2.service.task.ResolveRequestedProducerUidTask;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jboss.logging.MDC;

/**
 * Flow implementation for direct UID-based data transfers. Validates consumer request, consent, and builds provider request.
 *
 * @CommentLastReviewed 2026-02-04
 */
@ApplicationScoped
@RequiredArgsConstructor
public class DirectUidFlow implements Flowable {

  private final AgridataFlow agridataFlow;
  private final ResolveConsumerUidFromTokenTask resolveConsumerUidFromTokenTask;
  private final EnsureValidConsumerRequestTask ensureValidConsumerRequestTask;
  private final ResolveRequestedProducerUidTask resolveRequestedProducerUidTask;
  private final EnsureValidDataRequestTask ensureValidDataRequestTask;
  private final EnsureValidConsentForProducerUidsTask ensureValidConsentForProducerUidsTask;
  private final BuildProviderRequestTask buildProviderRequestTask;

  @Override
  public Response run(UUID productId,
                      Map<String, String> requestParameters) {
    return agridataFlow.run(
        AgridataContext.builder()
            .dataTransferRequestId(MDC.get(REQUEST_ID_MDC_FIELD).toString())
            .flowEnum(FlowEnum.UID_DIRECT)
            .productId(productId)
            .requestParameters(requestParameters)
            .build(),
        List.of(
            resolveConsumerUidFromTokenTask,
            ensureValidConsumerRequestTask,
            resolveRequestedProducerUidTask,
            ensureValidDataRequestTask,
            ensureValidConsentForProducerUidsTask,
            buildProviderRequestTask),
        List.of());
  }

}
