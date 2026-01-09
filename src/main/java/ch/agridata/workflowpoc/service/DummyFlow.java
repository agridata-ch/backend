package ch.agridata.workflowpoc.service;

import ch.agridata.workflowpoc.client.DummyClient;
import ch.agridata.workflowpoc.service.task.DummyTaskOne;
import ch.agridata.workflowpoc.service.task.DummyTaskTwo;
import ch.agridata.workflowpoc.service.workflowengine.AgridataContext;
import ch.agridata.workflowpoc.service.workflowengine.AgridataFlow;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Dummy for workflow poc
 *
 * @CommentLastReviewed 2026-01-07
 */
@ApplicationScoped
public class DummyFlow {

  private final DummyTaskOne dummyTaskOne;
  private final DummyTaskTwo dummyTaskTwo;
  private final DummyClient dummyClient;

  @Inject
  public DummyFlow(DummyTaskOne dummyTaskOne,
                   DummyTaskTwo dummyTaskTwo,
                   @RestClient DummyClient dummyClient) {
    this.dummyTaskOne = dummyTaskOne;
    this.dummyTaskTwo = dummyTaskTwo;
    this.dummyClient = dummyClient;
  }

  public Response run(UUID productId, Map<String, String> queryParameters) {
    return new AgridataFlow(AgridataContext.builder()
        .productId(productId)
        .requestQueryParameters(queryParameters)
        .build(),
        List.of(dummyTaskOne),
        List.of(dummyTaskTwo),
        dummyClient).run();
  }
}
