package ch.agridata.user.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Aggregates the company and its authorized farms for an overview page of a producer.
 *
 * @CommentLastReviewed 2026-11-18
 */
@Schema(description = "Data transfer object representing the farm overview of a company")
@Builder
public record FarmOverviewDto(
    @NotNull
    CompanyDto company,
    @NotNull
    List<FarmDto> farms
) {
}
