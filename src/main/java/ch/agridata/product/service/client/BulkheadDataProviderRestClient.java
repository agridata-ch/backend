package ch.agridata.product.service.client;

import ch.agridata.common.exceptions.DataProviderUnavailableException;
import ch.agridata.product.api.DataProviderRestClient;
import ch.agridata.product.dto.RestClientIdentifier;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Response;
import java.net.ConnectException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Limits the number of concurrent requests to a data provider and rejects further requests immediately, so an overloaded provider
 * does not pile up waiting requests in agridata.ch. The permit is released once the provider's response headers arrived.
 * Also translates read timeouts and connection failures into {@link DataProviderUnavailableException}.
 *
 * @CommentLastReviewed 2026-10-06
 */
record BulkheadDataProviderRestClient(RestClientIdentifier identifier,
                                      DataProviderRestClient delegate,
                                      Semaphore permits) implements DataProviderRestClient {

  @Override
  public Response post(String path, Headers headers, Object body) {
    return call(() -> delegate.post(path, headers, body));
  }

  @Override
  public Response get(String path, Headers headers) {
    return call(() -> delegate.get(path, headers));
  }

  private Response call(Supplier<Response> request) {
    if (!permits.tryAcquire()) {
      throw new DataProviderUnavailableException(
          "Maximum number of concurrent connections to data provider " + identifier + " is exhausted");
    }
    try {
      return request.get();
    } catch (ProcessingException ex) {
      if (ExceptionUtils.indexOfType(ex, TimeoutException.class) >= 0) {
        throw new DataProviderUnavailableException("Data provider " + identifier + " did not respond in time", ex);
      }
      // also covers connect timeouts, as netty's ConnectTimeoutException extends ConnectException
      if (ExceptionUtils.indexOfType(ex, ConnectException.class) >= 0) {
        throw new DataProviderUnavailableException("Data provider " + identifier + " is not reachable", ex);
      }
      throw ex;
    } finally {
      permits.release();
    }
  }
}
