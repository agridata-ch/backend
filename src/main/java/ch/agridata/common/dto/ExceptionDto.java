package ch.agridata.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Encapsulates exception details for system-level errors. It provides a transport format for communicating messages, request IDs, types,
 * and debug information.
 *
 * @CommentLastReviewed 2025-08-25
 */

@Builder
@Schema(name = "ExceptionDto")
public record ExceptionDto(
    String message,
    @NotNull
    String requestId,
    ExceptionEnum type,
    String debugMessage
) {
}
