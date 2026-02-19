package ch.agridata.agreement.api;

import ch.agridata.agreement.dto.DataRequestDto;
import java.util.List;

/**
 * Defines the API interface for managing data requests. It specifies the operations available to external clients.
 *
 * @CommentLastReviewed 2026-02-04
 */
public interface DataRequestApi {

  List<DataRequestDto> getActiveDataRequestsOfConsumer(String consumerUid);

}
