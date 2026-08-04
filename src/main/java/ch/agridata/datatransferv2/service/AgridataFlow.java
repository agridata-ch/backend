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
 * @CommentLastReviewed 2026-08-03
 */
@ApplicationScoped
@Slf4j
public class AgridataFlow {

  public Response run(AgridataContext context,
                      List<UnaryOperator<AgridataContext>> tasksBefore,
                      List<UnaryOperator<AgridataContext>> tasksAfter) {

    log.debug("Starting flow for productId={}, flowType={}", context.getProductId(), context.getFlowEnum());

    try {
      runTasks(context, tasksBefore);
      return proxy(context, tasksAfter);
    } finally {
      context.emitTimingLog();
    }
  }

  private void runTasks(
      AgridataContext context,
      List<UnaryOperator<AgridataContext>> tasks) {
    for (UnaryOperator<AgridataContext> task : tasks) {
      long taskStart = System.nanoTime();
      String taskName = task.getClass().getSimpleName();
      log.debug("Executing task: {}", taskName);
      try {
        context = task.apply(context);
      } catch (RuntimeException ex) {
        context.getFlowTiming().addTask(taskName, FlowTiming.Responsibility.AGRIDATA, taskStart);
        context.getFlowTiming().setFailedTask(taskName);
        log.warn("Task {} failed: {} | context: {}", taskName, ex.getMessage(), context);
        throw ex;
      }
      context.getFlowTiming().addTask(taskName, FlowTiming.Responsibility.AGRIDATA, taskStart);
    }
  }

  private Response proxy(AgridataContext context,
                         List<UnaryOperator<AgridataContext>> tasksAfter) {

    log.debug("Calling upstream provider");
    long providerStart = System.nanoTime();
    String taskName = "Provider Request";
    Response upstream;
    try {
      upstream = context.getProviderRequest().get();
    } catch (RuntimeException ex) {
      context.getFlowTiming().addTask(taskName, FlowTiming.Responsibility.PROVIDER, providerStart);
      context.getFlowTiming().setFailedTask(taskName);
      throw ex;
    }
    context.getFlowTiming().addTask(taskName, FlowTiming.Responsibility.PROVIDER, providerStart);
    log.debug("Upstream provider responded with status={}", upstream.getStatus());

    Map<String, String> headers = upstream.getStringHeaders().entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getFirst()));

    context.setResponseHeaders(headers);

    try {
      runTasks(context, tasksAfter);
    } catch (RuntimeException ex) {
      upstream.close();
      throw ex;
    }

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
