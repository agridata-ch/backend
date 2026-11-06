package ch.agridata.common.security;

import static ch.agridata.common.filters.PreSecurityMdcFilter.REQUEST_ID_MDC_FIELD;
import static ch.agridata.common.filters.PreSecurityMdcFilter.USER_ID_MDC_FIELD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import org.jboss.logging.MDC;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class LogContextTest {

  private static final UUID USER_ID = UUID.fromString("9fc651e8-def0-4456-a8d4-7d4c9d3dfc04");

  @AfterEach
  void clearMdc() {
    MDC.remove(REQUEST_ID_MDC_FIELD);
    MDC.remove(USER_ID_MDC_FIELD);
  }

  @Test
  void whenCallWith_thenSetsRequestIdAndGivenUserIdOnlyDuringWork() {
    var requestIdDuringWork = new AtomicReference<>();
    var userIdDuringWork = new AtomicReference<>();

    new LogContext("request-1").callWith(
        USER_ID, () -> {
          requestIdDuringWork.set(MDC.get(REQUEST_ID_MDC_FIELD));
          userIdDuringWork.set(MDC.get(USER_ID_MDC_FIELD));
          return null;
        }
    );

    assertThat(requestIdDuringWork.get()).isEqualTo("request-1");
    assertThat(userIdDuringWork.get()).isEqualTo(USER_ID.toString());
    assertThat(MDC.get(REQUEST_ID_MDC_FIELD)).isNull();
    assertThat(MDC.get(USER_ID_MDC_FIELD)).isNull();
  }

  @Test
  void givenWorkThrows_callWith_restoresPreviousValues() {
    MDC.put(REQUEST_ID_MDC_FIELD, "previous-request");
    MDC.put(USER_ID_MDC_FIELD, "previous-user");
    var logContext = new LogContext("request-1");

    assertThatThrownBy(() -> logContext.callWith(
        USER_ID, () -> {
          throw new IllegalStateException("boom");
        }
    )).isInstanceOf(IllegalStateException.class);

    assertThat(MDC.get(REQUEST_ID_MDC_FIELD)).isEqualTo("previous-request");
    assertThat(MDC.get(USER_ID_MDC_FIELD)).isEqualTo("previous-user");
  }

  @Test
  void whenCallWithOnPooledThread_thenLeavesNothingBehindForNextTask() throws Exception {
    var logContext = new LogContext("request-1");

    try (var executor = Executors.newSingleThreadExecutor()) {
      executor.submit(() -> logContext.callWith(USER_ID, () -> null)).get();

      assertThat(executor.submit(() -> MDC.get(REQUEST_ID_MDC_FIELD)).get()).isNull();
      assertThat(executor.submit(() -> MDC.get(USER_ID_MDC_FIELD)).get()).isNull();
    }
  }

  @Test
  void whenCallWith_thenReturnsResultOfWorkUnderGivenIds() {
    var idsDuringWork = new LogContext("request-2").callWith(
        USER_ID, () -> MDC.get(REQUEST_ID_MDC_FIELD) + "|" + MDC.get(USER_ID_MDC_FIELD));

    assertThat(idsDuringWork).isEqualTo("request-2|" + USER_ID);
  }

  @Test
  void givenRequestIdInLoggingContext_current_capturesIt() {
    MDC.put(REQUEST_ID_MDC_FIELD, "request-3");

    var logContext = LogContext.onCurrentRequestId();
    MDC.put(REQUEST_ID_MDC_FIELD, "other-request");

    assertThat(logContext.requestId()).isEqualTo("request-3");
  }

  @Test
  void givenNoRequestIdInLoggingContext_current_startsNewRequest() {
    assertThat(LogContext.onCurrentRequestId().requestId()).isNotBlank();
  }

  @Test
  void givenNoRequestIdInLoggingContext_current_startsDifferentRequestEachTime() {
    assertThat(LogContext.onCurrentRequestId().requestId()).isNotEqualTo(LogContext.onCurrentRequestId().requestId());
  }
}
