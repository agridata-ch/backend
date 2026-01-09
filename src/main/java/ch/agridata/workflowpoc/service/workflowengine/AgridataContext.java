package ch.agridata.workflowpoc.service.workflowengine;

import java.util.Map;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Dummy for workflow poc
 *
 * @CommentLastReviewed 2026-01-07
 */
@Getter
@Setter
@Builder
public final class AgridataContext {
  private UUID productId;
  private Map<String, String> requestQueryParameters;
  private Map<String, String> responseHeaders;
  private String exampleAttribute1;
  private String exampleAttribute2;
}
