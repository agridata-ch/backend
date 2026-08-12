package ch.agridata.health.service;

import ch.agridata.health.dto.HealthDto.DataProviderStatus;
import ch.agridata.health.dto.HealthDto.HealthStatus;
import ch.agridata.product.api.DataSourceSystemApi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

/**
 * Determines the health of every data source system loaded from the product module, mapping each
 * {@code data_source_system.code} to a {@link DataProviderProbe} (probes run in parallel, bounded by a timeout);
 * systems without a matching probe are reported as {@code HEALTH_CHECK_NOT_IMPLEMENTED}.
 *
 * @CommentLastReviewed 2026-08-11
 */
@ApplicationScoped
@RequiredArgsConstructor
public class DataProviderHealthService {

  private static final Duration PROBE_TIMEOUT = Duration.ofSeconds(10);

  private final DataSourceSystemApi dataSourceSystemApi;
  private final Instance<DataProviderProbe> probes;

  public List<DataProviderStatus> checkAll() {
    var probesByCode = probes.stream()
        .collect(Collectors.toMap(DataProviderProbe::dataSourceSystemCode, Function.identity()));
    var systems = dataSourceSystemApi.getDataSourceSystems();

    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      var futures = systems.stream()
          .map(system -> {
            var probe = probesByCode.get(system.code());
            return CompletableFuture
                .supplyAsync(() -> statusFor(probe), executor)
                .completeOnTimeout(timeoutStatusFor(probe), PROBE_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
                .thenApply(status ->
                    new DataProviderStatus(system.dataProvider().name(), system.name(), status));
          })
          .toList();

      return futures.stream().map(CompletableFuture::join).toList();
    }
  }

  private static HealthStatus statusFor(DataProviderProbe probe) {
    if (probe == null) {
      return HealthStatus.HEALTH_CHECK_NOT_IMPLEMENTED;
    }
    try {
      return probe.isUp() ? HealthStatus.UP : HealthStatus.DOWN;
    } catch (Exception _) {
      return HealthStatus.DOWN;
    }
  }

  private static HealthStatus timeoutStatusFor(DataProviderProbe probe) {
    return probe == null ? HealthStatus.HEALTH_CHECK_NOT_IMPLEMENTED : HealthStatus.DOWN;
  }
}
