package ch.agridata.product.api;

import ch.agridata.product.dto.RestClientIdentifier;

/**
 * Declares operations for retrieving a restClient. It ensures a stable contract for other modules.
 *
 * @CommentLastReviewed 2026-07-07
 */

public interface DataProviderRestClientProviderApi {
  DataProviderRestClient get(RestClientIdentifier restClientIdentifier);
}
