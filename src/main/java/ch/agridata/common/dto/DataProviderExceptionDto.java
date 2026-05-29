package ch.agridata.common.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Transport payload returned when an upstream data provider responded with an error. In addition to the standard {@code message},
 * {@code requestId} and {@code exceptionType} fields, it propagates the provider's HTTP status code and response body so the caller
 * can react to the underlying failure.
 *
 * @CommentLastReviewed 2026-05-29
 */
@Schema(name = "DataProviderExceptionDto")
public record DataProviderExceptionDto(
    String message,
    String requestId,
    ExceptionEnum exceptionType,
    Integer dataProviderHttpStatus,
    String dataProviderMessage
) {
}
