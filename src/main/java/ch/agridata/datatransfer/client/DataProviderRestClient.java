package ch.agridata.datatransfer.client;

/**
 * Defines the base contract for data provider clients. It standardizes post and get operations across providers.
 *
 * @CommentLastReviewed 2025-08-25
 */

public interface DataProviderRestClient {
  Object post(String path,
              Object request);

  Object get(String path,
             Object request);
}
