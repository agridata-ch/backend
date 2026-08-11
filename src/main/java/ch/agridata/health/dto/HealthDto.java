package ch.agridata.health.dto;

import ch.agridata.common.dto.TranslationDto;
import java.util.List;

/**
 * Public health contract: agridata.ch's own readiness plus a per data source system summary of the connected data
 * providers, which never influences {@code agridataStatus}.
 *
 * @CommentLastReviewed 2026-08-11
 */
public record HealthDto(HealthStatus agridataStatus, List<DataProviderStatus> dataProviders) {

  /**
   * Coarse status; {@code HEALTH_CHECK_NOT_IMPLEMENTED} marks systems without a health probe.
   *
   * @CommentLastReviewed 2026-08-11
   */
  public enum HealthStatus {
    UP, DOWN, HEALTH_CHECK_NOT_IMPLEMENTED
  }

  /**
   * Status of a single data source system: its provider's name, its own name, and its current health.
   *
   * @CommentLastReviewed 2026-08-11
   */
  public record DataProviderStatus(TranslationDto providerName, TranslationDto systemName, HealthStatus status) {
  }
}
