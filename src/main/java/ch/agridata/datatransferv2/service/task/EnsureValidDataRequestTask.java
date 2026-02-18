package ch.agridata.datatransferv2.service.task;

import ch.agridata.agreement.api.DataRequestApi;
import ch.agridata.agreement.dto.DataRequestDto;
import ch.agridata.common.exceptions.ConsentNotGrantedException;
import ch.agridata.datatransferv2.service.AgridataContext;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Verifies that the consumer has an active data request that includes the requested product.
 * A data request must be active and contain the productId in its product list.
 *
 * @CommentLastReviewed 2026-02-04
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class EnsureValidDataRequestTask implements UnaryOperator<AgridataContext> {

  private final DataRequestApi dataRequestApi;

  @Override
  public AgridataContext apply(final AgridataContext context) {
    var productId = context.getProductId();
    var consumerUid = context.getConsumerUid();

    log.debug("Checking data requests for consumerUid={}, productId={}", consumerUid, productId);

    List<UUID> validDataRequestIds = dataRequestApi.getActiveDataRequestsOfConsumer(consumerUid).stream()
        .filter(dr -> dr.products() != null)
        .filter(dr -> dr.products().contains(productId))
        .map(DataRequestDto::id)
        .toList();

    if (validDataRequestIds.isEmpty()) {
      log.warn("No valid data request found for consumerUid={}, productId={}",
          consumerUid, productId);
      throw new ConsentNotGrantedException(
          "No active data request found for the requested product");
    }

    context.setValidDataRequestIds(validDataRequestIds);
    log.debug("Found {} valid data request(s) for productId={}",
        validDataRequestIds.size(), productId);

    return context;
  }
}
