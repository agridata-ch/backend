package ch.agridata.datatransferv2.service;

import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Context object that holds the state passed through all tasks in a data transfer flow.
 *
 * @CommentLastReviewed 2026-02-04
 */
@Getter
@Setter
@Builder
public final class AgridataContext {
  private String dataTransferRequestId;
  private UUID productId;
  private FlowEnum flowEnum;
  private Map<String, String> requestParameters;
  private List<String> consumerUids;
  private String consumerAgateLoginId;
  private List<String> producerUidsInPayload;
  private List<UUID> validDataRequestIds;
  private Map<String, String> responseHeaders;
  private Supplier<Response> providerRequest;

  @Override
  public String toString() {
    return "AgridataContext{"
        + "dataTransferRequestId='" + dataTransferRequestId
        + ", productId=" + productId
        + ", flowEnum=" + flowEnum
        + ", requestParameters=" + requestParameters
        + ", consumerUids=" + consumerUids
        + ", consumerAgateLoginId='" + consumerAgateLoginId
        + ", producerUidsInPayload=" + producerUidsInPayload
        + ", validDataRequestIds=" + validDataRequestIds
        + ", responseHeaders=" + responseHeaders
        + ", providerRequest=" + providerRequest
        + '}';
  }
}
