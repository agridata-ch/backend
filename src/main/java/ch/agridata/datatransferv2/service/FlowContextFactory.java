package ch.agridata.datatransferv2.service;

import static ch.agridata.common.filters.PreSecurityMdcFilter.REQUEST_ID_MDC_FIELD;
import static ch.agridata.common.filters.PreSecurityMdcFilter.REQUEST_START_NANOS_KEY;

import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.product.dto.DataProductProviderConfigurationDto;
import io.vertx.ext.web.RoutingContext;
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
  private final RoutingContext routingContext;

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
        .flowTiming(new FlowTiming(requestStartNanos()))
        .build();
  }

  /**
   * Arrival time of the HTTP request, so the timing log also covers authentication and everything else that ran before
   * the flow. Falls back to now if the route filter did not record it.
   */
  private long requestStartNanos() {
    Long startNanos = routingContext.get(REQUEST_START_NANOS_KEY);
    return startNanos != null ? startNanos : System.nanoTime();
  }
}
