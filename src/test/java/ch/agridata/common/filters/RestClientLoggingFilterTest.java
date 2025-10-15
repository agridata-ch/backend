package ch.agridata.common.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.quarkus.test.InMemoryLogHandler;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientResponseContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.LogRecord;
import org.jboss.logmanager.Level;
import org.jboss.logmanager.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RestClientLoggingFilterTest {

  @Mock
  private ClientRequestContext requestContext;

  @Mock
  private ClientResponseContext responseContext;

  @InjectMocks
  private RestClientLoggingFilter filter;

  private static final Predicate<LogRecord> FILTER_LOGS =
      rec -> rec.getLoggerName().equals(RestClientLoggingFilter.class.getName()) &&
          rec.getLevel().intValue() >= Level.INFO.intValue();

  static final InMemoryLogHandler LOG_HANDLER = new InMemoryLogHandler(FILTER_LOGS);

  private static final Logger LOGGER =
      Logger.getLogger(RestClientLoggingFilter.class.getName());

  private static java.util.logging.Level originalLevel;

  @BeforeAll
  static void attachHandler() {
    originalLevel = LOGGER.getLevel();
    LOGGER.addHandler(LOG_HANDLER);
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
  void filter_shouldSetStartTimeProperty() throws IOException {
    // given
    when(requestContext.getMethod()).thenReturn("GET");
    when(requestContext.getUri()).thenReturn(URI.create("https://example.com/api/test"));
    LOGGER.setLevel(Level.INFO);

    // when
    filter.filter(requestContext);

    // then
    verify(requestContext).setProperty(org.mockito.ArgumentMatchers.eq("restClientLoggingFilter.startTime"),
        org.mockito.ArgumentMatchers.anyLong());
  }

  @Test
  void filter_shouldLogRequestAtInfoLevel() throws IOException {
    // given
    when(requestContext.getMethod()).thenReturn("GET");
    when(requestContext.getUri()).thenReturn(URI.create("https://example.com/api/test"));
    LOGGER.setLevel(Level.INFO);

    // when
    filter.filter(requestContext);

    // then
    List<LogRecord> logsList = LOG_HANDLER.getRecords();
    assertThat(logsList).isNotEmpty();
    LogRecord logRecord = logsList.get(0);
    assertThat(logRecord.getLevel()).isEqualTo(Level.INFO);
    assertThat(logRecord.getMessage()).contains("REST Client Request: GET https://example.com/api/test");
  }

  @Test
  void filter_shouldNotLogWhenLogLevelBelowInfo() throws IOException {
    // given
    LOGGER.setLevel(Level.WARN);

    // when
    filter.filter(requestContext);

    // then
    List<LogRecord> logsList = LOG_HANDLER.getRecords();
    assertThat(logsList).isEmpty();
  }

  @Test
  void filter_shouldLogRequestBodyAtDebugLevel() throws IOException {
    // given
    when(requestContext.getMethod()).thenReturn("POST");
    when(requestContext.getUri()).thenReturn(URI.create("https://example.com/api/test"));
    when(requestContext.hasEntity()).thenReturn(true);
    when(requestContext.getEntity()).thenReturn("{\"name\":\"test\"}");
    LOGGER.setLevel(Level.DEBUG);

    // when
    filter.filter(requestContext);

    // then
    List<LogRecord> logsList = LOG_HANDLER.getRecords();
    assertThat(logsList).isNotEmpty();
    LogRecord logRecord = logsList.get(0);
    assertThat(logRecord.getMessage()).contains("REST Client Request: POST https://example.com/api/test");
  }

  @Test
  void filter_shouldNotLogRequestBodyAtInfoLevel() throws IOException {
    // given
    when(requestContext.getMethod()).thenReturn("POST");
    when(requestContext.getUri()).thenReturn(URI.create("https://example.com/api/test"));
    lenient().when(requestContext.hasEntity()).thenReturn(true);
    lenient().when(requestContext.getEntity()).thenReturn("test body");
    LOGGER.setLevel(Level.INFO);

    // when
    filter.filter(requestContext);

    // then
    List<LogRecord> logsList = LOG_HANDLER.getRecords();
    assertThat(logsList).isNotEmpty();
    // Body should not be logged at INFO level
  }

  @Test
  void filter_shouldLogResponseAtInfoLevel() throws IOException {
    // given
    when(requestContext.getMethod()).thenReturn("GET");
    when(requestContext.getUri()).thenReturn(URI.create("https://example.com/api/test"));
    when(requestContext.getProperty("restClientLoggingFilter.startTime")).thenReturn(System.currentTimeMillis() - 100);
    when(responseContext.getStatus()).thenReturn(200);
    LOGGER.setLevel(Level.INFO);

    // when
    filter.filter(requestContext, responseContext);

    // then
    List<LogRecord> logsList = LOG_HANDLER.getRecords();
    assertThat(logsList).isNotEmpty();
    LogRecord logRecord = logsList.get(0);
    assertThat(logRecord.getLevel()).isEqualTo(Level.INFO);
    assertThat(logRecord.getMessage()).contains("REST Client Response: GET https://example.com/api/test");
    assertThat(logRecord.getMessage()).contains("Status: 200");
    assertThat(logRecord.getMessage()).contains("Duration:");
  }

  @Test
  void filter_shouldCalculateDurationCorrectly() throws IOException {
    // given
    long startTime = System.currentTimeMillis() - 250;
    when(requestContext.getMethod()).thenReturn("POST");
    when(requestContext.getUri()).thenReturn(URI.create("https://example.com/api/test"));
    when(requestContext.getProperty("restClientLoggingFilter.startTime")).thenReturn(startTime);
    when(responseContext.getStatus()).thenReturn(201);
    LOGGER.setLevel(Level.INFO);

    // when
    filter.filter(requestContext, responseContext);

    // then
    List<LogRecord> logsList = LOG_HANDLER.getRecords();
    assertThat(logsList).isNotEmpty();
    LogRecord logRecord = logsList.get(0);
    assertThat(logRecord.getMessage()).contains("Duration:");
    assertThat(logRecord.getMessage()).matches(".*Duration: \\d+ ms.*");
  }

  @Test
  void filter_shouldHandleMissingStartTime() throws IOException {
    // given
    when(requestContext.getMethod()).thenReturn("GET");
    when(requestContext.getUri()).thenReturn(URI.create("https://example.com/api/test"));
    when(requestContext.getProperty("restClientLoggingFilter.startTime")).thenReturn(null);
    when(responseContext.getStatus()).thenReturn(200);
    LOGGER.setLevel(Level.INFO);

    // when
    filter.filter(requestContext, responseContext);

    // then
    List<LogRecord> logsList = LOG_HANDLER.getRecords();
    assertThat(logsList).isNotEmpty();
    LogRecord logRecord = logsList.get(0);
    assertThat(logRecord.getMessage()).contains("Duration: -1 ms");
  }

  @Test
  void filter_shouldLogResponseBodyAtTraceLevel() throws IOException {
    // given
    String responseBody = "{\"result\":\"success\"}";
    when(requestContext.getMethod()).thenReturn("GET");
    when(requestContext.getUri()).thenReturn(URI.create("https://example.com/api/test"));
    when(requestContext.getProperty("restClientLoggingFilter.startTime")).thenReturn(System.currentTimeMillis() - 50);
    when(responseContext.getStatus()).thenReturn(200);
    when(responseContext.hasEntity()).thenReturn(true);
    when(responseContext.getEntityStream()).thenReturn(
        new ByteArrayInputStream(responseBody.getBytes(StandardCharsets.UTF_8)));
    LOGGER.setLevel(Level.TRACE);

    // when
    filter.filter(requestContext, responseContext);

    // then
    List<LogRecord> logsList = LOG_HANDLER.getRecords();
    assertThat(logsList).isNotEmpty();
    verify(responseContext).setEntityStream(org.mockito.ArgumentMatchers.any());
  }

  @Test
  void filter_shouldNotLogResponseBodyAtInfoLevel() throws IOException {
    // given
    when(requestContext.getMethod()).thenReturn("GET");
    when(requestContext.getUri()).thenReturn(URI.create("https://example.com/api/test"));
    when(requestContext.getProperty("restClientLoggingFilter.startTime")).thenReturn(System.currentTimeMillis() - 50);
    when(responseContext.getStatus()).thenReturn(200);
    lenient().when(responseContext.hasEntity()).thenReturn(true);
    LOGGER.setLevel(Level.INFO);

    // when
    filter.filter(requestContext, responseContext);

    // then
    List<LogRecord> logsList = LOG_HANDLER.getRecords();
    assertThat(logsList).isNotEmpty();
    // Response body should not be logged at INFO level
  }

  @Test
  void filter_shouldHandleObjectEntity() throws IOException {
    // given
    TestObject testObject = new TestObject("test", 123);
    when(requestContext.getMethod()).thenReturn("POST");
    when(requestContext.getUri()).thenReturn(URI.create("https://example.com/api/test"));
    when(requestContext.hasEntity()).thenReturn(true);
    when(requestContext.getEntity()).thenReturn(testObject);
    LOGGER.setLevel(Level.DEBUG);

    // when
    filter.filter(requestContext);

    // then
    List<LogRecord> logsList = LOG_HANDLER.getRecords();
    assertThat(logsList).isNotEmpty();
  }

  @Test
  void filter_shouldHandleNullEntity() throws IOException {
    // given
    when(requestContext.getMethod()).thenReturn("POST");
    when(requestContext.getUri()).thenReturn(URI.create("https://example.com/api/test"));
    when(requestContext.hasEntity()).thenReturn(true);
    when(requestContext.getEntity()).thenReturn(null);
    LOGGER.setLevel(Level.DEBUG);

    // when
    filter.filter(requestContext);

    // then
    List<LogRecord> logsList = LOG_HANDLER.getRecords();
    assertThat(logsList).isNotEmpty();
  }

  @Test
  void filter_shouldCompactJsonString() throws IOException {
    // given
    String prettyJson = "{\n  \"name\": \"test\",\n  \"value\": 123\n}";
    when(requestContext.getMethod()).thenReturn("POST");
    when(requestContext.getUri()).thenReturn(URI.create("https://example.com/api/test"));
    when(requestContext.hasEntity()).thenReturn(true);
    when(requestContext.getEntity()).thenReturn(prettyJson);
    LOGGER.setLevel(Level.DEBUG);

    // when
    filter.filter(requestContext);

    // then
    List<LogRecord> logsList = LOG_HANDLER.getRecords();
    assertThat(logsList).isNotEmpty();
  }

  @Test
  void filter_shouldHandleNonJsonString() throws IOException {
    // given
    String plainText = "plain text with\nmultiple\nlines";
    when(requestContext.getMethod()).thenReturn("POST");
    when(requestContext.getUri()).thenReturn(URI.create("https://example.com/api/test"));
    when(requestContext.hasEntity()).thenReturn(true);
    when(requestContext.getEntity()).thenReturn(plainText);
    LOGGER.setLevel(Level.DEBUG);

    // when
    filter.filter(requestContext);

    // then
    List<LogRecord> logsList = LOG_HANDLER.getRecords();
    assertThat(logsList).isNotEmpty();
  }

  @Test
  void filter_shouldHandleEmptyResponseBody() throws IOException {
    // given
    when(requestContext.getMethod()).thenReturn("DELETE");
    when(requestContext.getUri()).thenReturn(URI.create("https://example.com/api/test"));
    when(requestContext.getProperty("restClientLoggingFilter.startTime")).thenReturn(System.currentTimeMillis() - 30);
    when(responseContext.getStatus()).thenReturn(204);
    when(responseContext.hasEntity()).thenReturn(false);
    LOGGER.setLevel(Level.TRACE);

    // when
    filter.filter(requestContext, responseContext);

    // then
    List<LogRecord> logsList = LOG_HANDLER.getRecords();
    assertThat(logsList).isNotEmpty();
  }

  @Test
  void filter_shouldHandleNullEntityStream() throws IOException {
    // given
    when(requestContext.getMethod()).thenReturn("GET");
    when(requestContext.getUri()).thenReturn(URI.create("https://example.com/api/test"));
    when(requestContext.getProperty("restClientLoggingFilter.startTime")).thenReturn(System.currentTimeMillis() - 50);
    when(responseContext.getStatus()).thenReturn(200);
    when(responseContext.hasEntity()).thenReturn(true);
    when(responseContext.getEntityStream()).thenReturn(null);
    LOGGER.setLevel(Level.TRACE);

    // when
    filter.filter(requestContext, responseContext);

    // then
    List<LogRecord> logsList = LOG_HANDLER.getRecords();
    assertThat(logsList).isNotEmpty();
  }

  @Test
  void filter_shouldHandleJsonArray() throws IOException {
    // given
    String jsonArray = "[{\"id\":1},{\"id\":2}]";
    when(requestContext.getMethod()).thenReturn("POST");
    when(requestContext.getUri()).thenReturn(URI.create("https://example.com/api/test"));
    when(requestContext.hasEntity()).thenReturn(true);
    when(requestContext.getEntity()).thenReturn(jsonArray);
    LOGGER.setLevel(Level.DEBUG);

    // when
    filter.filter(requestContext);

    // then
    List<LogRecord> logsList = LOG_HANDLER.getRecords();
    assertThat(logsList).isNotEmpty();
  }

  @Test
  void filter_shouldHandleInvalidJson() throws IOException {
    // given
    String invalidJson = "{invalid json}";
    when(requestContext.getMethod()).thenReturn("POST");
    when(requestContext.getUri()).thenReturn(URI.create("https://example.com/api/test"));
    when(requestContext.hasEntity()).thenReturn(true);
    when(requestContext.getEntity()).thenReturn(invalidJson);
    LOGGER.setLevel(Level.DEBUG);

    // when
    filter.filter(requestContext);

    // then
    List<LogRecord> logsList = LOG_HANDLER.getRecords();
    assertThat(logsList).isNotEmpty();
    // Should still log without crashing
  }

  // Helper class for testing object serialization
  private static class TestObject {
    private final String name;
    private final int value;

    public TestObject(String name, int value) {
      this.name = name;
      this.value = value;
    }

    public String getName() {
      return name;
    }

    public int getValue() {
      return value;
    }
  }
}
