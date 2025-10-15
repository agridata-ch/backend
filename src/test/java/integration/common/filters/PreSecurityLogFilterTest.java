package integration.common.filters;

import static integration.testutils.TestUserEnum.ADMIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

import ch.agridata.common.filters.PreSecurityLogFilter;
import integration.testutils.AuthTestUtils;
import io.quarkus.test.InMemoryLogHandler;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.filter.log.ResponseLoggingFilter;
import jakarta.ws.rs.core.MediaType;
import java.util.function.Predicate;
import java.util.logging.LogRecord;
import org.jboss.logmanager.Level;
import org.jboss.logmanager.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


@QuarkusTest
@TestHTTPEndpoint(TestResource.class)
class PreSecurityLogFilterTest {


  /* 2️⃣ Predicate keeps only INFO+ lines from this filter’s logger */
  private static final Predicate<LogRecord> FILTER_LOGS =
      rec -> rec.getLoggerName().equals(PreSecurityLogFilter.class.getName()) &&
          rec.getLevel().intValue() >= Level.DEBUG.intValue();

  /* 3️⃣ In-memory handler registered as JUnit 5 extension */
  static final InMemoryLogHandler LOG_HANDLER = new InMemoryLogHandler(FILTER_LOGS);

  /* 4️⃣ Attach handler to that logger once */
  private static final Logger LOGGER =
      Logger.getLogger(PreSecurityLogFilter.class.getName());

  @BeforeAll
  static void attachHandler() {
    LOGGER.addHandler(LOG_HANDLER);
  }

  @AfterAll
  static void detachHandler() {
    LOGGER.removeHandler(LOG_HANDLER);
  }

  @AfterEach
  void clearLogs() {
    LOG_HANDLER.flush();
  }

  @Test
  void givenLogInfo_whenPostRequest_thenLogWithoutBody() {
    LOGGER.setLevel(Level.INFO);
    AuthTestUtils.requestAs(ADMIN)
        .filter(new ResponseLoggingFilter())
        .body("hello world")
        .header("Content-Type", MediaType.APPLICATION_JSON)
        .when()
        .post()
        .then()
        .statusCode(200)
        .body(is("test ok"));

    assertThat(LOG_HANDLER.getRecords())
        .anySatisfy(evt ->
            assertThat(evt.getMessage())
                .isEqualTo("REQUEST: POST /api/test application/json "))
        .anySatisfy(evt ->
            assertThat(evt.getMessage())
                .contains("RESPONSE: POST /api/test status: 200"));

  }

  @Test
  void givenLogDebug_whenPostRequest_thenLogWithBody() {
    LOGGER.setLevel(Level.DEBUG);
    AuthTestUtils.requestAs(ADMIN)
        .filter(new ResponseLoggingFilter())
        .body("hello world")
        .header("Content-Type", MediaType.APPLICATION_JSON)
        .when()
        .post()
        .then()
        .statusCode(200)
        .body(is("test ok"));

    assertThat(LOG_HANDLER.getRecords())
        .anySatisfy(evt ->
            assertThat(evt.getMessage())
                .isEqualTo("REQUEST: POST /api/test application/json  body: hello world"))
        .anySatisfy(evt ->
            assertThat(evt.getMessage())
                .contains("RESPONSE: POST /api/test status: 200"));

  }
}
