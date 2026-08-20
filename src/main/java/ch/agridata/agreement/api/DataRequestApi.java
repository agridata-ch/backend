package ch.agridata.agreement.api;

import java.util.List;
import java.util.UUID;

/**
 * Defines the API interface for managing data requests. It specifies the operations available to external clients.
 *
 * @CommentLastReviewed 2026-08-14
 */
public interface DataRequestApi {

  List<UUID> getActiveDataRequestIdsForConsumerAndProduct(String consumerUid, UUID productId);

}
