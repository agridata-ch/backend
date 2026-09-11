package ch.agridata.common.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import java.util.List;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.jboss.resteasy.reactive.Separator;

/**
 * Data transfer object for resource query parameters including pagination, sorting, filtering and searching.
 *
 * @CommentLastReviewed 2026-09-14
 */
@Builder

public record ResourceQueryDto(
    @Schema(description = "page number")
    @QueryParam("page")
    @DefaultValue("0")
    @Min(0)
    int page,

    @Schema(description = "page size")
    @QueryParam("size")
    @DefaultValue("20")
    @Min(1) @Max(100)
    int size,

    @Schema(description = "List of field names to sort by. Default is ascending prefix with - for descending")
    @QueryParam("sortBy") List<String> sortParams,

    @Schema(description = "string to search for in the resource")
    @QueryParam("searchTerm") String searchTerm,

    @Schema(description = "Column filters in the form '<column>:<value>[,<value>]', multiple filters separated by ';'. Values of the "
        + "same column are combined with OR, filters on different columns with AND. Duplicate values are ignored. "
        + "The filterable columns are documented per endpoint.")
    @QueryParam("filter") @Separator(";") List<String> columnFilters,

    @Schema(description = "language code for multilingual fields. Must be supported by application.")
    @QueryParam("language") String language
) {
  public SupportedLanguage supportedLanguage() {
    return SupportedLanguage.from(language);
  }
}
