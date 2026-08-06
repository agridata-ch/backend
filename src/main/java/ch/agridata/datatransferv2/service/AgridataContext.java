package ch.agridata.datatransferv2.service;

import static io.quarkiverse.loggingjson.providers.KeyValueStructuredArgument.kv;

import ch.agridata.product.dto.DataProductProviderConfigurationDto;
import com.google.common.collect.Range;
import jakarta.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * Context object that holds the state passed through all tasks in a data transfer flow.
 *
 * @CommentLastReviewed 2026-02-26
 */
@Getter
@Setter
@Builder
@Slf4j
public final class AgridataContext {
  private String dataTransferRequestId;
  private UUID productId;
  private FlowEnum flowEnum;
  private DataProductProviderConfigurationDto productProviderConfiguration;
  private Map<String, String> requestParameters;
  private String consumerUid;
  private String consumerAgateLoginId;
  private List<String> producerUids;
  private List<String> producerBurs;
  private Range<@NotNull LocalDate> requestedDateRange;
  private List<UUID> validDataRequestIds;
  private Map<String, String> responseHeaders;
  private Supplier<Response> providerRequest;

  @Builder.Default
  private FlowTiming flowTiming = new FlowTiming();

  @Override
  public String toString() {
    return "AgridataContext{"
        + "dataTransferRequestId='" + dataTransferRequestId + '\''
        + ", productId=" + productId
        + ", flowEnum=" + flowEnum
        + ", productProviderConfiguration=" + productProviderConfiguration
        + ", requestParameters=" + requestParameters
        + ", consumerUid='" + consumerUid + '\''
        + ", consumerAgateLoginId='" + consumerAgateLoginId + '\''
        + ", producerUids=" + producerUids
        + ", producerBurs=" + producerBurs
        + ", requestedDateRange=" + requestedDateRange
        + ", validDataRequestIds=" + validDataRequestIds
        + ", responseHeaders=" + responseHeaders
        + ", providerRequest=" + providerRequest
        + '}';
  }

  public void emitTimingLog() {
    var totalTimeInMs = flowTiming.getTotalTimeInMsSinceInitialization();
    var usedTimeInMsByAgridata = flowTiming.getUsedTimeInMsByResponsibility(FlowTiming.Responsibility.AGRIDATA);
    var usedTimeInMsByProvider = flowTiming.getUsedTimeInMsByResponsibility(FlowTiming.Responsibility.PROVIDER);

    var failed = flowTiming.getFailedTask() != null;
    var status = failed ? "failed" : "ok";
    var logArguments = new ArrayList<>(List.of(
        String.valueOf(flowEnum),
        status,
        totalTimeInMs,
        usedTimeInMsByAgridata,
        usedTimeInMsByProvider,
        kv("operation", "datatransfer.timing"),
        kv("requestId", dataTransferRequestId),
        kv("productId", productId),
        kv("flowType", flowEnum),
        kv("status", status),
        kv("totalTimeInMs", totalTimeInMs),
        kv("usedTimeInMsByAgridata", usedTimeInMsByAgridata),
        kv("usedTimeInMsByProvider", usedTimeInMsByProvider),
        kv("tasks", flowTiming.getTasks())));

    if (failed) {
      logArguments.add(kv("failedTask", flowTiming.getFailedTask()));
    }

    log.info("datatransfer.timing flow={} status={} totalTimeInMs={}ms usedTimeInMsByAgridata={}ms usedTimeInMsByProvider={}ms",
        logArguments.toArray());
  }
}
