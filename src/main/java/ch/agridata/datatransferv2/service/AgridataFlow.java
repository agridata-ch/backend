package ch.agridata.datatransferv2.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * Orchestrates the execution of data transfer workflows by running task pipelines
 * and proxying responses from upstream data providers.
 *
 * @CommentLastReviewed 2026-02-04
 */
@ApplicationScoped
@Slf4j
public class AgridataFlow {

  public Response run(AgridataContext initContext,
                      List<UnaryOperator<AgridataContext>> tasksBefore,
                      List<UnaryOperator<AgridataContext>> tasksAfter) {

    log.debug("Starting flow for productId={}, flowType={}",
        initContext.getProductId(), initContext.getFlowEnum());

    AgridataContext context = runTasks(initContext, tasksBefore);

    return proxy(context, tasksAfter);
  }

  private AgridataContext runTasks(
      AgridataContext context,
      List<UnaryOperator<AgridataContext>> tasks) {
    for (UnaryOperator<AgridataContext> task : tasks) {
      String taskName = task.getClass().getSimpleName();
      log.debug("Executing task: {}", taskName);
      try {
        context = task.apply(context);
      } catch (RuntimeException ex) {
        log.warn("Task {} failed: {} | context: {}", taskName, ex.getMessage(), context);
        throw ex;
      }
    }
    return context;
  }

  private Response proxy(AgridataContext context,
                         List<UnaryOperator<AgridataContext>> tasksAfter) {

    log.debug("Calling upstream provider");
    Response upstream = context.getProviderRequest().get();
    log.debug("Upstream provider responded with status={}", upstream.getStatus());

    Map<String, String> headers = upstream.getStringHeaders().entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getFirst()));

    context.setResponseHeaders(headers);

    runTasks(context, tasksAfter);

    return forwardResponse(upstream, context);
  }

  private Response forwardResponse(Response upstream, AgridataContext context) {
    StreamingOutput out = os -> streamAndClose(upstream, os);
    return Response.status(upstream.getStatus())
        .header("AGRIDATA-REQUEST-ID", context.getDataTransferRequestId())
        .type(upstream.getMediaType())
        .entity(out)
        .build();
  }

  private void streamAndClose(Response upstream, OutputStream os) throws IOException {
    try (Response r = upstream; InputStream is = r.readEntity(InputStream.class)) {
      is.transferTo(os);
      os.flush();
    }
  }

}
