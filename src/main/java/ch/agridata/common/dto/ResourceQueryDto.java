package ch.agridata.common.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import java.util.List;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Data transfer object for resource query parameters including pagination, sorting, and searching.
 *
 * @CommentLastReviewed 2025-09-11
 */
@Builder

public record ResourceQueryDto(
    @Schema(description = "page number")
    @QueryParam("page")
    @DefaultValue("0")
    @Min(0)
    @Max(100)
    int page,

    @Schema(description = "page size")
    @QueryParam("size")
    @DefaultValue("20")
    @Min(1) @Max(100)
    int size,

    @Schema(description = "List of field names to sort by. Default is ascending prefix with - for descending")
    @QueryParam("sortBy") List<String> sortParams,

    @Schema(description = "string to search for in the resource")
    @QueryParam("searchTerm") String searchTerm
) {
}
