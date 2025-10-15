package ch.agridata.common;

import static ch.agridata.common.filters.PreSecurityMdcFilter.REQUEST_ID_MDC_FIELD;

import ch.agridata.common.dto.ExceptionDto;
import ch.agridata.common.dto.ExceptionEnum;
import ch.agridata.common.dto.ExternalServiceExceptionDto;
import ch.agridata.common.exceptions.DataTransferFailedException;
import ch.agridata.common.exceptions.ExternalWebServiceException;
import ch.agridata.common.exceptions.UidMissingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.slf4j.MDC;

/**
 * Handles exceptions throughout the application and returns appropriate HTTP responses with detailed messages.
 *
 * @CommentLastReviewed 2025-08-25
 */

@ApplicationScoped
@Slf4j
public class ExceptionHandler {

  private static final String DEFAULT_EXCEPTION_MESSAGE = "An error occurred";

  @ConfigProperty(name = "agridata.exception-handling.return-debug", defaultValue = "false")
  boolean returnDebug;

  @ServerExceptionMapper(Throwable.class)
  public Response handleAll(Throwable ex) {
    log.error("Exception: {}", ex.getMessage(), ex);
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
        .entity(createResponse(DEFAULT_EXCEPTION_MESSAGE, ex.getMessage()))
        .build();
  }

  @ServerExceptionMapper(ValidationException.class)
  public Response handleValidationException(ValidationException ex) {
    log.warn("ValidationException: {}", ex.getMessage(), ex);
    return Response.status(Response.Status.BAD_REQUEST)
        .entity(createResponse(DEFAULT_EXCEPTION_MESSAGE, ex.getMessage()))
        .build();
  }

  @ServerExceptionMapper(MismatchedInputException.class)
  public Response handleValidationException(MismatchedInputException ex) {
    log.warn("MismatchedInputException: {}", ex.getMessage(), ex);
    return Response.status(Response.Status.BAD_REQUEST)
        .entity(createResponse(DEFAULT_EXCEPTION_MESSAGE, ex.getMessage()))
        .build();
  }


  @ServerExceptionMapper(IllegalArgumentException.class)
  public Response handleIllegalArgumentException(IllegalArgumentException ex) {
    log.warn("IllegalArgumentException: {}", ex.getMessage(), ex);
    return Response.status(Response.Status.BAD_REQUEST)
        .entity(createResponse(DEFAULT_EXCEPTION_MESSAGE, ex.getMessage()))
        .build();
  }

  @ServerExceptionMapper(IllegalStateException.class)
  public Response handleIllegalStateException(IllegalStateException ex) {
    log.warn("IllegalStateException: {}", ex.getMessage(), ex);
    return Response.status(Response.Status.BAD_REQUEST)
        .entity(createResponse(DEFAULT_EXCEPTION_MESSAGE, ex.getMessage()))
        .build();
  }

  @ServerExceptionMapper(UidMissingException.class)
  public Response handleUidMissingException(UidMissingException ex) {
    log.error("UidMissingException: {}", ex.getMessage(), ex);
    return Response.status(Status.FORBIDDEN)
        .entity(createResponse(DEFAULT_EXCEPTION_MESSAGE, ex.getMessage(), ExceptionEnum.UID_MISSING))
        .build();
  }

  @ServerExceptionMapper(ConstraintViolationException.class)
  public Response handleConstraintViolation(ConstraintViolationException ex) {
    String violations = ex.getConstraintViolations().stream()
        .map(violation -> violation.getPropertyPath().toString() + ": " + violation.getMessage())
        .sorted()
        .collect(Collectors.joining(", "));

    log.warn("ConstraintViolationException, violations: {}, exception: {}", violations,
        ex.getMessage());
    return Response.status(Response.Status.BAD_REQUEST).entity(
        createResponse("Validation failed", violations)).build();
  }

  @ServerExceptionMapper(NotFoundException.class)
  public Response handleNotFound(NotFoundException ex) {
    log.warn("NotFoundException: {}", ex.getMessage(), ex);
    return Response.status(Response.Status.NOT_FOUND)
        .entity(createResponse("NOT_FOUND", ex.getMessage()))
        .build();
  }

  @ServerExceptionMapper(WebApplicationException.class)
  public Response handleWebAppException(WebApplicationException ex) {
    log.error("WebApplicationException: {}", ex.getMessage(), ex);
    return Response.status(ex.getResponse().getStatus())
        .entity(createResponse("Web exception", ex.getMessage()))
        .build();
  }

  @ServerExceptionMapper(DataTransferFailedException.class)
  public Response handleDataTransferFailedException(DataTransferFailedException ex) {
    log.error("DataTransferFailedException: {}", ex.getMessage(), ex);
    return Response.status(Status.BAD_GATEWAY)
        .entity(createExternalServiceFailedResponse(ex.getMessage(), ex.getStatus(), ex.getMessage()))
        .build();
  }

  @ServerExceptionMapper(ExternalWebServiceException.class)
  public Response handleExternalWebServiceException(ExternalWebServiceException ex) {
    log.error("ExternalWebServiceException: {}", ex.getMessage(), ex);
    return Response.status(Status.BAD_GATEWAY)
        .entity(createExternalServiceFailedResponse("External service failed", 0, ex.getMessage()))
        .build();
  }

  private ExceptionDto createResponse(String message, String debugMessage) {
    return createResponse(message, debugMessage, ExceptionEnum.GENERIC);
  }

  private ExceptionDto createResponse(String message, String debugMessage, ExceptionEnum type) {
    var errorBuilder = ExceptionDto.builder()
        .message(message)
        .type(type)
        .requestId(MDC.get(REQUEST_ID_MDC_FIELD));
    if (returnDebug) {
      errorBuilder.debugMessage(debugMessage);
    }
    return errorBuilder.build();
  }

  private ExternalServiceExceptionDto createExternalServiceFailedResponse(String message, int status, String debugMessage) {
    var errorBuilder = ExternalServiceExceptionDto.builder()
        .message(message)
        .status(status)
        .type(ExceptionEnum.EXTERNAL_SERVICE_ERROR)
        .requestId(MDC.get(REQUEST_ID_MDC_FIELD));
    if (returnDebug) {
      errorBuilder.debugMessage(debugMessage);
    }
    return errorBuilder.build();
  }

}
