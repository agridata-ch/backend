package ch.agridata.workflowpoc.client;

import jakarta.ws.rs.core.Response;

/**
 * Dummy for workflow poc
 *
 * @CommentLastReviewed 2026-01-07
 */
public interface DataProviderRestClient {
  Response post(String path,
                Object request);

  Response get(String path,
               Object request);
}
