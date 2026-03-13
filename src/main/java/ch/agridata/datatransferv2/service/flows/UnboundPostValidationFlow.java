package ch.agridata.datatransferv2.service.flows;

import static ch.agridata.common.filters.PreSecurityMdcFilter.REQUEST_ID_MDC_FIELD;

import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.datatransferv2.service.AgridataContext;
import ch.agridata.datatransferv2.service.AgridataFlow;
import ch.agridata.datatransferv2.service.FlowEnum;
import ch.agridata.datatransferv2.service.Flowable;
import ch.agridata.datatransferv2.service.task.BuildProviderRequestTask;
import ch.agridata.datatransferv2.service.task.EnsureValidConsentForProducerBursTask;
import ch.agridata.datatransferv2.service.task.EnsureValidConsentForProducerUidsTask;
import ch.agridata.datatransferv2.service.task.EnsureValidConsumerRequestTask;
import ch.agridata.datatransferv2.service.task.EnsureValidDataRequestTask;
import ch.agridata.datatransferv2.service.task.ResolveConsumerUidFromResponseHeaderTask;
import ch.agridata.datatransferv2.service.task.ResolveConsumerUidFromTokenTask;
import ch.agridata.datatransferv2.service.task.ResolveProducerIdentifiersFromResponseHeaderTask;
import ch.agridata.datatransferv2.service.task.ResolveRequestedDateTask;
import ch.agridata.product.dto.DataProductProviderConfigurationDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.jboss.logging.MDC;

/**
 * Flow for unbound data transfers where neither the producer UID nor BUR is known upfront.
 *
 * <p>The consumer UID is attempted to be resolved from the Agate token but is not required. No producer identifier is
 * resolved from the query parameters before the request. If the consumer UID was already present in the token, data
 * request validation is performed before forwarding the request to the data provider. In both cases, the producer UIDs
 * and BURs are resolved from the provider's {@code AGRIDATA-RESPONSE-PRODUCER-UIDS} and
 * {@code AGRIDATA-RESPONSE-PRODUCER-BURS} response headers, after which consent validation is performed.</p>
 *
 * @CommentLastReviewed 2026-02-27
 */
@ApplicationScoped
@RequiredArgsConstructor
public class UnboundPostValidationFlow implements Flowable {

  private final AgridataFlow agridataFlow;
  private final AgridataSecurityIdentity agridataSecurityIdentity;
  private final ResolveConsumerUidFromTokenTask resolveConsumerUidFromTokenTask;
  private final EnsureValidConsumerRequestTask ensureValidConsumerRequestTask;
  private final ResolveRequestedDateTask resolveRequestedDateTask;
  private final EnsureValidDataRequestTask ensureValidDataRequestTask;
  private final ResolveConsumerUidFromResponseHeaderTask resolveConsumerUidFromResponseHeaderTask;
  private final EnsureValidConsentForProducerBursTask ensureValidConsentForProducerBursTask;
  private final BuildProviderRequestTask buildProviderRequestTask;
  private final ResolveProducerIdentifiersFromResponseHeaderTask resolveProducerIdentifiersFromResponseHeaderTask;
  private final EnsureValidConsentForProducerUidsTask ensureValidConsentForProducerUidsTask;

  @Override
  public Response run(DataProductProviderConfigurationDto productProviderConfiguration,
                      Map<String, String> requestParameters) {

    var initContext = AgridataContext.builder()
        .dataTransferRequestId(MDC.get(REQUEST_ID_MDC_FIELD).toString())
        .flowEnum(FlowEnum.UNBOUND_POST_VALIDATION)
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
              resolveRequestedDateTask,
              ensureValidDataRequestTask,
              buildProviderRequestTask),
          List.of(
              resolveProducerIdentifiersFromResponseHeaderTask,
              ensureValidConsentForProducerUidsTask,
              ensureValidConsentForProducerBursTask
          ));
    }

    return agridataFlow.run(initContext,
        List.of(
            ensureValidConsumerRequestTask,
            resolveRequestedDateTask,
            buildProviderRequestTask),
        List.of(
            resolveConsumerUidFromResponseHeaderTask,
            ensureValidDataRequestTask,
            resolveProducerIdentifiersFromResponseHeaderTask,
            ensureValidConsentForProducerUidsTask,
            ensureValidConsentForProducerBursTask
        ));
  }
}
