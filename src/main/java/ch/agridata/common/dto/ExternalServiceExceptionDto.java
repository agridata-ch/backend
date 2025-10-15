package ch.agridata.common.dto;

import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Defines a DTO for representing exceptions caused by external services. It includes diagnostic fields like request ID, type, status, and
 * debug message.
 *
 * @CommentLastReviewed 2025-08-25
 */

@Builder
@Schema(name = "ExceptionDto")
public record ExternalServiceExceptionDto(
    String message,
    String requestId,
    ExceptionEnum type,
    int status,
    String debugMessage
) {
}
