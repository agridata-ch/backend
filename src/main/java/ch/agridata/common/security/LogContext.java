package ch.agridata.common.security;

import static ch.agridata.common.filters.PreSecurityMdcFilter.REQUEST_ID_MDC_FIELD;
import static ch.agridata.common.filters.PreSecurityMdcFilter.USER_ID_MDC_FIELD;

import java.util.UUID;
import java.util.function.Supplier;
import org.jboss.logging.MDC;

/**
 * The logging context (MDC) of a request or job run, identified by its request id. {@link #runWith} and {@link #callWith} set
 * {@code requestId} and {@code agridataUserId} while the given work runs and restore the previous values afterward, so every log line of
 * the work can be correlated to the request or job run behind it and nothing is left over for the next task on the same thread. It only
 * deals with the MDC: request context and identity are handled by {@link BackgroundTaskRunner}, which is the only user of this class.
 *
 * @param requestId the request id the work runs under
 * @CommentLastReviewed 2026-11-04
 */
record LogContext(String requestId) {

  static LogContext newRequest() {
    return new LogContext(UUID.randomUUID().toString());
  }

  /**
   * Captures the request id of the current logging context, or starts a new request if there is none. The id is read when this method is
   * called, so call it where the logging context to capture is active.
   */
  static LogContext onCurrentRequestId() {
    var requestId = MDC.get(REQUEST_ID_MDC_FIELD);
    return requestId != null ? new LogContext(requestId.toString()) : newRequest();
  }

  void runWith(UUID userId, Runnable work) {
    callWith(
        userId, () -> {
          work.run();
          return null;
        }
    );
  }

  <T> T callWith(UUID userId, Supplier<T> work) {
    var previousRequestId = MDC.get(REQUEST_ID_MDC_FIELD);
    var previousUserId = MDC.get(USER_ID_MDC_FIELD);
    MDC.put(REQUEST_ID_MDC_FIELD, requestId);
    MDC.put(USER_ID_MDC_FIELD, userId.toString());
    try {
      return work.get();
    } finally {
      restore(REQUEST_ID_MDC_FIELD, previousRequestId);
      restore(USER_ID_MDC_FIELD, previousUserId);
    }
  }

  private void restore(String key, Object previousValue) {
    if (previousValue != null) {
      MDC.put(key, previousValue);
    } else {
      MDC.remove(key);
    }
  }
}
