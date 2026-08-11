package ch.agridata.health.service;

import ch.agridata.health.dto.HealthDto;
import ch.agridata.health.dto.HealthDto.HealthStatus;
import io.quarkus.cache.CacheResult;
import io.smallrye.health.SmallRyeHealthReporter;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

/**
 * Assembles the cached health view: agridata.ch's readiness plus the data-provider summary from
 * {@link DataProviderHealthService}. Caching shields readiness checks and provider probes from external polling.
 *
 * @CommentLastReviewed 2026-08-11
 */
@ApplicationScoped
@RequiredArgsConstructor
public class HealthService {

  private final SmallRyeHealthReporter healthReporter;
  private final DataProviderHealthService dataProviderHealthService;

  @CacheResult(cacheName = "health")
  public HealthDto status() {
    var agridataStatus = healthReporter.getReadiness().isDown() ? HealthStatus.DOWN : HealthStatus.UP;
    return new HealthDto(agridataStatus, dataProviderHealthService.checkAll());
  }
}
