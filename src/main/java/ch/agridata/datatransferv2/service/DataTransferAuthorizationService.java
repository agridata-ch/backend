package ch.agridata.datatransferv2.service;

import ch.agridata.common.security.AgridataSecurityIdentity;
import io.quarkus.security.ForbiddenException;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * Enforces authorization rules for data transfer requests. Determines whether a consumer role is
 * required based on the flow type, and throws {@link ForbiddenException} if the check fails.
 *
 * @CommentLastReviewed 2026-03-25
 */
@ApplicationScoped
@RequiredArgsConstructor
public class DataTransferAuthorizationService {

  private static final List<FlowEnum> FLOWS_WITH_PROVIDER_SIDE_AUTHORIZATION = List.of(
      FlowEnum.UID_BASED_POST_VALIDATION,
      FlowEnum.BUR_BASED_POST_VALIDATION,
      FlowEnum.UNBOUND_POST_VALIDATION);

  private final AgridataSecurityIdentity agridataSecurityIdentity;

  /**
   * Enforces authorization for the given flow. Requires the consumer role unless the flow
   * uses provider-side authorization.
   *
   * @throws ForbiddenException if the caller does not have the consumer role
   */
  public void enforceAuthorization(FlowProvider.FlowWithProductProviderConfiguration flowWithConfiguration) {
    var flowEnum = FlowEnum.valueOf(flowWithConfiguration.productProviderConfiguration().flowCode());
    if (FLOWS_WITH_PROVIDER_SIDE_AUTHORIZATION.contains(flowEnum)) {
      return;
    }
    if (!agridataSecurityIdentity.isConsumer()) {
      throw new ForbiddenException();
    }
  }
}
