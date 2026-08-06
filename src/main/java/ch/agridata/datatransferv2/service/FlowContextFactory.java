package ch.agridata.datatransferv2.service;

import static ch.agridata.common.filters.PreSecurityMdcFilter.REQUEST_ID_MDC_FIELD;

import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.product.dto.DataProductProviderConfigurationDto;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.jboss.logging.MDC;

/**
 * Builds the initial {@link AgridataContext} shared by every {@link Flowable} implementation
 *
 * @CommentLastReviewed 2026-08-04
 */
@ApplicationScoped
@RequiredArgsConstructor
public class FlowContextFactory {

  private final AgridataSecurityIdentity agridataSecurityIdentity;

  public AgridataContext create(FlowEnum flowEnum,
                                DataProductProviderConfigurationDto productProviderConfiguration,
                                Map<String, String> requestParameters) {
    return AgridataContext.builder()
        .dataTransferRequestId(MDC.get(REQUEST_ID_MDC_FIELD).toString())
        .flowEnum(flowEnum)
        .productId(productProviderConfiguration.id())
        .productProviderConfiguration(productProviderConfiguration)
        .consumerAgateLoginId(agridataSecurityIdentity.getAgateLoginId())
        .requestParameters(requestParameters)
        .build();
  }
}
