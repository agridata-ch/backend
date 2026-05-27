package ch.agridata.datatransferv2.service;

import ch.agridata.datatransferv2.service.flows.BurBasedPostValidationFlow;
import ch.agridata.datatransferv2.service.flows.BurBasedPreValidationFlow;
import ch.agridata.datatransferv2.service.flows.UidBasedPostValidationFlow;
import ch.agridata.datatransferv2.service.flows.UidBasedPreValidationFlow;
import ch.agridata.datatransferv2.service.flows.UnboundPostValidationFlow;
import ch.agridata.product.api.DataProductApi;
import ch.agridata.product.dto.DataProductProviderConfigurationDto;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

/**
 * Provider that selects and provides the appropriate flow implementation based on product configuration.
 *
 * @CommentLastReviewed 2026-06-01
 */
@ApplicationScoped
@RequiredArgsConstructor
public class FlowProvider {

  private final DataProductApi dataProductApi;
  private final UidBasedPreValidationFlow uidBasedPreValidationFlow;
  private final UidBasedPostValidationFlow uidBasedPostValidationFlow;
  private final BurBasedPreValidationFlow burBasedPreValidationFlow;
  private final BurBasedPostValidationFlow burBasedPostValidationFlow;
  private final UnboundPostValidationFlow unboundPostValidationFlow;

  /**
   * Bundles a resolved {@link Flowable} with the already-fetched {@link DataProductProviderConfigurationDto}
   *
   * @CommentLastReviewed 2026-06-01
   */
  public record FlowWithProductProviderConfiguration(Flowable flow, DataProductProviderConfigurationDto productProviderConfiguration) {
  }

  public FlowWithProductProviderConfiguration getFlowByProduct(UUID productId) {
    var productProviderConfiguration = dataProductApi.getProviderConfigurationById(productId);
    var flowEnum = FlowEnum.valueOf(productProviderConfiguration.flowCode());

    Flowable flowable = switch (flowEnum) {
      case FlowEnum.UID_BASED_PRE_VALIDATION -> uidBasedPreValidationFlow;
      case FlowEnum.UID_BASED_POST_VALIDATION -> uidBasedPostValidationFlow;
      case FlowEnum.BUR_BASED_PRE_VALIDATION -> burBasedPreValidationFlow;
      case FlowEnum.BUR_BASED_POST_VALIDATION -> burBasedPostValidationFlow;
      case FlowEnum.UNBOUND_POST_VALIDATION -> unboundPostValidationFlow;
    };

    return new FlowWithProductProviderConfiguration(flowable, productProviderConfiguration);
  }

}
