package ch.agridata.datatransferv2.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import ch.agridata.common.utils.LogUtil;
import io.quarkus.test.InMemoryLogHandler;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.logging.LogRecord;
import org.jboss.logmanager.Level;
import org.jboss.logmanager.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AgridataContextTest {

  private static final UUID PRODUCT_ID = UUID.randomUUID();
  private static final String REQUEST_ID = "test-request-id";

  private static final Predicate<LogRecord> TIMING_LOGS =
      rec -> rec.getLoggerName().equals(AgridataContext.class.getName())
          && rec.getLevel().intValue() >= Level.INFO.intValue();
  private static final InMemoryLogHandler LOG_HANDLER = new InMemoryLogHandler(TIMING_LOGS);
  private static final Logger LOGGER = Logger.getLogger(AgridataContext.class.getName());
  private static java.util.logging.Level originalLevel;

  @BeforeAll
  static void attachHandler() {
    originalLevel = LOGGER.getLevel();
    LOGGER.addHandler(LOG_HANDLER);
    LOGGER.setLevel(Level.INFO);
  }

  @AfterAll
  static void detachHandler() {
    LOGGER.removeHandler(LOG_HANDLER);
    if (originalLevel != null) {
      LOGGER.setLevel(originalLevel);
    }
  }

  @AfterEach
  void clearLogs() {
    LOG_HANDLER.getRecords().clear();
  }

  @Test
  void givenSuccessfulTiming_whenEmitTimingLog_thenSingleOkLineWithIdentityAndTasksIsEmitted() {
    var context = createContext();
    context.getFlowTiming().addTask("BeforeTask", FlowTiming.Responsibility.AGRIDATA, System.nanoTime());
    context.getFlowTiming().addTask("Provider Request", FlowTiming.Responsibility.PROVIDER, System.nanoTime());

    context.emitTimingLog();

    assertThat(timingLineCount()).isEqualTo(1);
    var fields = timingFields();
    assertThat(fields)
        .containsEntry("status", "ok")
        .containsEntry("requestId", REQUEST_ID)
        .containsEntry("productId", PRODUCT_ID)
        .containsEntry("flowType", FlowEnum.UID_BASED_PRE_VALIDATION)
        .doesNotContainKey("failedTask");
    assertThat(fields.get("usedTimeInMsByProvider")).isInstanceOf(Long.class);
    assertThat((Long) fields.get("usedTimeInMsByProvider")).isGreaterThanOrEqualTo(0L);
    assertThat(timingTasks(fields))
        .extracting(FlowTiming.TaskTiming::name, FlowTiming.TaskTiming::responsibility)
        .containsExactly(
            tuple("BeforeTask", FlowTiming.Responsibility.AGRIDATA),
            tuple("Provider Request", FlowTiming.Responsibility.PROVIDER));
  }

  @Test
  void givenFailedTiming_whenEmitTimingLog_thenLineHasFailedStatusAndFailedTask() {
    var context = createContext();
    context.getFlowTiming().addTask("FailingTask", FlowTiming.Responsibility.AGRIDATA, System.nanoTime());
    context.getFlowTiming().setFailedTask("FailingTask");

    context.emitTimingLog();

    var fields = timingFields();
    assertThat(fields)
        .containsEntry("status", "failed")
        .containsEntry("failedTask", "FailingTask");
  }

  private static Map<String, Object> timingFields() {
    return LOG_HANDLER.getRecords().stream()
        .map(LogUtil::structuredFields)
        .filter(f -> "datatransfer.timing".equals(f.get("operation")))
        .findFirst()
        .orElseThrow(() -> new AssertionError("No datatransfer.timing log line was emitted"));
  }

  private static long timingLineCount() {
    return LOG_HANDLER.getRecords().stream()
        .map(LogUtil::structuredFields)
        .filter(f -> "datatransfer.timing".equals(f.get("operation")))
        .count();
  }

  @SuppressWarnings("unchecked")
  private static List<FlowTiming.TaskTiming> timingTasks(Map<String, Object> fields) {
    return (List<FlowTiming.TaskTiming>) fields.get("tasks");
  }

  private AgridataContext createContext() {
    return AgridataContext.builder()
        .dataTransferRequestId(REQUEST_ID)
        .productId(PRODUCT_ID)
        .flowEnum(FlowEnum.UID_BASED_PRE_VALIDATION)
        .build();
  }
}
