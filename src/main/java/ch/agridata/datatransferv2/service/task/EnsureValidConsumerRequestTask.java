package ch.agridata.datatransferv2.service.task;

import ch.agridata.datatransferv2.service.AgridataContext;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Validates that all required request parameters are present for the given flow type.
 * Throws IllegalArgumentException if required parameters are missing or blank.
 *
 * @CommentLastReviewed 2026-02-26
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class EnsureValidConsumerRequestTask implements UnaryOperator<AgridataContext> {

  @Override
  public AgridataContext apply(final AgridataContext context) {
    var requestParameters = context.getRequestParameters();
    var flowEnum = context.getFlowEnum();
    var requiredParams = flowEnum != null ? flowEnum.getRequiredRequestParameters() : Set.<String>of();

    log.debug("Validating request parameters for flow={}, required={}", flowEnum, requiredParams);

    var missingParams = requiredParams.stream()
        .filter(param ->
            requestParameters == null
                || !requestParameters.containsKey(param)
                || requestParameters.get(param) == null
                || requestParameters.get(param).isBlank()
        )
        .collect(Collectors.toSet());

    if (!missingParams.isEmpty()) {
      log.warn("Missing required request parameters: {} for flow={}", missingParams, flowEnum);
      throw new IllegalArgumentException("Missing required request parameters: "
          + String.join(", ", missingParams));
    }

    log.debug("Request parameters validated successfully");
    return context;
  }
}
