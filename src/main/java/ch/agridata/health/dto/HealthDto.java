package ch.agridata.health.dto;

import ch.agridata.common.dto.TranslationDto;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Public health contract: agridata.ch's own readiness plus a per data source system summary of the connected data
 * providers, which never influences {@code agridataStatus}.
 *
 * @CommentLastReviewed 2026-08-11
 */
@Schema(description = "Public health status of agridata.ch and its connected data source systems")
public record HealthDto(
    @Schema(description = "Overall readiness of agridata.ch itself. Not affected by the data provider statuses")
    HealthStatus agridataStatus,
    @Schema(description = "Per data source system health summary")
    List<DataProviderStatus> dataProviders) {

  /**
   * Coarse status; {@code HEALTH_CHECK_NOT_IMPLEMENTED} marks systems without a health probe.
   *
   * @CommentLastReviewed 2026-08-11
   */
  @Schema(description = "Health status. HEALTH_CHECK_NOT_IMPLEMENTED marks systems without a health probe")
  public enum HealthStatus {
    UP, DOWN, HEALTH_CHECK_NOT_IMPLEMENTED
  }

  /**
   * Status of a single data source system: its provider's name, its own name, and its current health.
   *
   * @CommentLastReviewed 2026-08-11
   */
  @Schema(description = "Health status of a single data source system")
  public record DataProviderStatus(
      @Schema(description = "Name of the data provider operating the system")
      TranslationDto providerName,
      @Schema(description = "Name of the data source system")
      TranslationDto systemName,
      @Schema(description = "Current health of the data source system")
      HealthStatus status) {
  }
}
