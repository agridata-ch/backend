package ch.agridata.datatransferv2.service;

import ch.agridata.datatransferv2.service.flows.UidBasedPreValidationFlow;
import ch.agridata.product.api.DataProductApi;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;

/**
 * Provider that selects and provides the appropriate flow implementation based on product configuration.
 *
 * @CommentLastReviewed 2026-02-04
 */
@ApplicationScoped
@RequiredArgsConstructor
public class FlowProvider {

  private final DataProductApi dataProductApi;
  private final UidBasedPreValidationFlow uidBasedPreValidationFlow;

  public Flowable getFlowByProduct(UUID productId) {
    var providerConfiguration = dataProductApi.getProviderConfigurationById(productId);
    var flow = FlowEnum.valueOf(providerConfiguration.flowCode());

    return switch (flow) {
      case FlowEnum.UID_BASED_PRE_VALIDATION -> uidBasedPreValidationFlow;
      case FlowEnum.UID_BASED_POST_VALIDATION -> throw new NotImplementedException();
    };
  }

}
