package ch.agridata.datatransferv2.service;

import jakarta.ws.rs.core.Response;
import java.util.Map;
import java.util.UUID;

/**
 * Interface for data transfer flow implementations. Each flow defines how to process a data transfer request.
 *
 * @CommentLastReviewed 2026-02-04
 */
public interface Flowable {

  Response run(UUID productId,
               Map<String, String> queryParameters);

}
