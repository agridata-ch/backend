package ch.agridata.common.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Data Transfer Object for paginated responses.
 *
 * @CommentLastReviewed 2025-09-08
 */
@Builder
@Schema(description = "Generic paginated response wrapper.")
public record PageResponseDto<T>(

    @NotNull
    @Schema(
        description = "List of items on the current page.",
        examples = "[{\"id\":1,\"name\":\"Item A\"},{\"id\":2,\"name\":\"Item B\"}]"
    )
    List<T> items,

    @NotNull
    @Schema(
        description = "Total number of items across all pages.",
        examples = "42"
    )
    Long totalItems,

    @NotNull
    @Schema(
        description = "Total number of pages available.",
        examples = "5"
    )
    Integer totalPages,

    @NotNull
    @Schema(
        description = "Current page number (0-based ).",
        examples = "1"
    )
    Integer currentPage,

    @NotNull
    @Schema(
        description = "Number of items per page.",
        examples = "10"
    )
    Integer pageSize
) {
}
