package ch.agridata.common;

import static ch.agridata.common.filters.PreSecurityMdcFilter.REQUEST_ID_MDC_FIELD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ch.agridata.common.dto.ExceptionDto;
import ch.agridata.common.dto.ExceptionEnum;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.MDC;

class ExceptionHandlerTest {

  private ExceptionHandler exceptionHandler;

  @BeforeEach
  void setUp() {
    exceptionHandler = new ExceptionHandler();
    MDC.put(REQUEST_ID_MDC_FIELD, "test-request-id");
  }

  @ParameterizedTest(name = "handleAll, debug={0}")
  @ValueSource(booleans = {false, true})
  void handleAll(boolean debug) {
    exceptionHandler.returnDebug = debug;
    RuntimeException ex = new RuntimeException("something went wrong");

    Response response = exceptionHandler.handleAll(ex);
    ExceptionDto dto = (ExceptionDto) response.getEntity();

    assertThat(response.getStatus())
        .isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    assertThat(dto.message()).isEqualTo("An error occurred");
    assertThat(dto.type()).isEqualTo(ExceptionEnum.GENERIC);
    assertThat(dto.requestId()).isEqualTo("test-request-id");

    if (debug) {
      assertThat(dto.debugMessage()).isEqualTo("something went wrong");
    } else {
      assertThat(dto.debugMessage()).isNull();
    }
  }

  @ParameterizedTest(name = "handleConstraintViolation, debug={0}")
  @ValueSource(booleans = {false, true})
  void handleConstraintViolation(boolean debug) {
    exceptionHandler.returnDebug = debug;

    ConstraintViolation<?> violation01 =
        mockConstraintViolation("test-property-01", "must not be null");
    ConstraintViolation<?> violation02 =
        mockConstraintViolation("test-property-02", "must be greater than 0");
    ConstraintViolationException ex =
        new ConstraintViolationException(Set.of(violation01, violation02));

    Response response = exceptionHandler.handleConstraintViolation(ex);
    ExceptionDto dto = (ExceptionDto) response.getEntity();

    assertThat(response.getStatus())
        .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    assertThat(dto.message()).isEqualTo("Validation failed");
    assertThat(dto.type()).isEqualTo(ExceptionEnum.GENERIC);

    if (debug) {
      assertThat(dto.debugMessage()).isEqualTo(
          "test-property-01: must not be null, test-property-02: must be greater than 0");
    } else {
      assertThat(dto.debugMessage()).isNull();
    }
  }

  @ParameterizedTest(name = "handleNotFound, debug={0}")
  @ValueSource(booleans = {false, true})
  void handleNotFound(boolean debug) {
    exceptionHandler.returnDebug = debug;
    NotFoundException exception = new NotFoundException("object not found");

    Response response = exceptionHandler.handleNotFound(exception);
    ExceptionDto dto = (ExceptionDto) response.getEntity();

    assertThat(response.getStatus())
        .isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    assertThat(dto.message()).isEqualTo("NOT_FOUND");
    assertThat(dto.type()).isEqualTo(ExceptionEnum.GENERIC);
    assertThat(dto.requestId()).isEqualTo("test-request-id");

    if (debug) {
      assertThat(dto.debugMessage()).isEqualTo("object not found");
    } else {
      assertThat(dto.debugMessage()).isNull();
    }
  }

  @ParameterizedTest(name = "handleWebAppException, debug={0}")
  @ValueSource(booleans = {false, true})
  void handleWebAppException(boolean debug) {
    exceptionHandler.returnDebug = debug;
    WebApplicationException exception = new WebApplicationException("access denied");

    Response response = exceptionHandler.handleWebAppException(exception);
    ExceptionDto dto = (ExceptionDto) response.getEntity();

    assertThat(response.getStatus())
        .isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    assertThat(dto.message()).isEqualTo("Web exception");
    assertThat(dto.type()).isEqualTo(ExceptionEnum.GENERIC);
    assertThat(dto.requestId()).isEqualTo("test-request-id");

    if (debug) {
      assertThat(dto.debugMessage()).isEqualTo("access denied");
    } else {
      assertThat(dto.debugMessage()).isNull();
    }
  }

  private ConstraintViolation<?> mockConstraintViolation(String path, String message) {
    ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
    Path propertyPath = mock(Path.class);
    when(violation.getPropertyPath()).thenReturn(propertyPath);
    when(propertyPath.toString()).thenReturn(path);
    when(violation.getMessage()).thenReturn(message);
    return violation;
  }
}
