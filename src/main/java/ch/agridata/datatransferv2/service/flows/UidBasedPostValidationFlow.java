package ch.agridata.datatransferv2.service.flows;

import static ch.agridata.common.filters.PreSecurityMdcFilter.REQUEST_ID_MDC_FIELD;

import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.datatransferv2.service.AgridataContext;
import ch.agridata.datatransferv2.service.AgridataFlow;
import ch.agridata.datatransferv2.service.FlowEnum;
import ch.agridata.datatransferv2.service.Flowable;
import ch.agridata.datatransferv2.service.task.BuildProviderRequestTask;
import ch.agridata.datatransferv2.service.task.EnsureValidConsentForProducerUidsTask;
import ch.agridata.datatransferv2.service.task.EnsureValidConsumerRequestTask;
import ch.agridata.datatransferv2.service.task.EnsureValidDataRequestTask;
import ch.agridata.datatransferv2.service.task.ResolveConsumerUidFromResponseHeaderTask;
import ch.agridata.datatransferv2.service.task.ResolveConsumerUidFromTokenTask;
import ch.agridata.datatransferv2.service.task.ResolveRequestedProducerUidTask;
import ch.agridata.product.dto.DataProductProviderConfigurationDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.jboss.logging.MDC;

/**
 * Flow for UID-based data transfers where the consumer UID is not necessarily known before calling the data provider.
 *
 * <p>The consumer UID is attempted to be resolved from the Agate token but is not required. The producer UID is resolved
 * from the query parameters. If the consumer UID was already present in the token, data request and consent validation
 * are performed immediately before forwarding the request to the data provider. Otherwise, the request is forwarded
 * first, and the consumer UID is resolved from the provider's {@code AGRIDATA-CONSUMER-UID} response header, after
 * which data request and consent validation are performed.</p>
 *
 * @CommentLastReviewed 2026-02-23
 */
@ApplicationScoped
@RequiredArgsConstructor
public class UidBasedPostValidationFlow implements Flowable {

  private final AgridataFlow agridataFlow;
  private final AgridataSecurityIdentity agridataSecurityIdentity;
  private final ResolveConsumerUidFromTokenTask resolveConsumerUidFromTokenTask;
  private final EnsureValidConsumerRequestTask ensureValidConsumerRequestTask;
  private final ResolveRequestedProducerUidTask resolveRequestedProducerUidTask;
  private final EnsureValidDataRequestTask ensureValidDataRequestTask;
  private final ResolveConsumerUidFromResponseHeaderTask resolveConsumerUidFromResponseHeaderTask;
  private final EnsureValidConsentForProducerUidsTask ensureValidConsentForProducerUidsTask;
  private final BuildProviderRequestTask buildProviderRequestTask;

  @Override
  public Response run(DataProductProviderConfigurationDto productProviderConfiguration,
                      Map<String, String> requestParameters) {

    var initContext = AgridataContext.builder()
        .dataTransferRequestId(MDC.get(REQUEST_ID_MDC_FIELD).toString())
        .flowEnum(FlowEnum.UID_BASED_POST_VALIDATION)
        .productId(productProviderConfiguration.id())
        .productProviderConfiguration(productProviderConfiguration)
        .consumerAgateLoginId(agridataSecurityIdentity.getAgateLoginId())
        .requestParameters(requestParameters)
        .build();

    if (agridataSecurityIdentity.getUid().isPresent()) {
      return agridataFlow.run(initContext,
          List.of(
              resolveConsumerUidFromTokenTask,
              ensureValidConsumerRequestTask,
              resolveRequestedProducerUidTask,
              ensureValidDataRequestTask,
              ensureValidConsentForProducerUidsTask,
              buildProviderRequestTask),
          List.of());
    }

    return agridataFlow.run(initContext,
        List.of(
            ensureValidConsumerRequestTask,
            resolveRequestedProducerUidTask,
            buildProviderRequestTask),
        List.of(
            resolveConsumerUidFromResponseHeaderTask,
            ensureValidDataRequestTask,
            ensureValidConsentForProducerUidsTask
        ));
  }
}
