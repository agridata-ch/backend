package ch.agridata.datatransferv2.service.flows;

import static ch.agridata.common.filters.PreSecurityMdcFilter.REQUEST_ID_MDC_FIELD;

import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.datatransferv2.service.AgridataContext;
import ch.agridata.datatransferv2.service.AgridataFlow;
import ch.agridata.datatransferv2.service.FlowEnum;
import ch.agridata.datatransferv2.service.Flowable;
import ch.agridata.datatransferv2.service.task.BuildProviderRequestTask;
import ch.agridata.datatransferv2.service.task.EnsureValidConsentForProducerBursTask;
import ch.agridata.datatransferv2.service.task.EnsureValidConsumerRequestTask;
import ch.agridata.datatransferv2.service.task.EnsureValidDataRequestTask;
import ch.agridata.datatransferv2.service.task.ResolveConsumerUidFromTokenTask;
import ch.agridata.datatransferv2.service.task.ResolveRequestedDateTask;
import ch.agridata.datatransferv2.service.task.ResolveRequestedProducerBurTask;
import ch.agridata.product.dto.DataProductProviderConfigurationDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.jboss.logging.MDC;

/**
 * Flow for BUR-based data transfers where the consumer is fully identified before calling the data provider.
 *
 * <p>All validation is performed upfront (pre-request). The consumer UID is extracted from the Agate token and must be
 * present, otherwise the request is rejected. The producer BUR is resolved from the query parameters, along with the
 * requested date. Before the request is forwarded to the data provider, the flow verifies that an active data request
 * exists for the given product and that the producer has granted consent to the consumer.</p>
 *
 * @CommentLastReviewed 2026-06-01
 */
@ApplicationScoped
@RequiredArgsConstructor
public class BurBasedPreValidationFlow implements Flowable {

  private final AgridataFlow agridataFlow;
  private final AgridataSecurityIdentity agridataSecurityIdentity;
  private final ResolveConsumerUidFromTokenTask resolveConsumerUidFromTokenTask;
  private final EnsureValidConsumerRequestTask ensureValidConsumerRequestTask;
  private final ResolveRequestedProducerBurTask resolveRequestedProducerBurTask;
  private final ResolveRequestedDateTask resolveRequestedDateTask;
  private final EnsureValidDataRequestTask ensureValidDataRequestTask;
  private final EnsureValidConsentForProducerBursTask ensureValidConsentForProducerBursTask;
  private final BuildProviderRequestTask buildProviderRequestTask;

  @Override
  public Response run(DataProductProviderConfigurationDto productProviderConfiguration,
                      Map<String, String> requestParameters) {

    return agridataFlow.run(
        AgridataContext.builder()
            .dataTransferRequestId(MDC.get(REQUEST_ID_MDC_FIELD).toString())
            .flowEnum(FlowEnum.BUR_BASED_PRE_VALIDATION)
            .productId(productProviderConfiguration.id())
            .productProviderConfiguration(productProviderConfiguration)
            .consumerAgateLoginId(agridataSecurityIdentity.getAgateLoginId())
            .requestParameters(requestParameters)
            .build(),
        List.of(
            resolveConsumerUidFromTokenTask,
            ensureValidConsumerRequestTask,
            resolveRequestedProducerBurTask,
            resolveRequestedDateTask,
            ensureValidDataRequestTask,
            ensureValidConsentForProducerBursTask,
            buildProviderRequestTask),
        List.of());
  }
}
