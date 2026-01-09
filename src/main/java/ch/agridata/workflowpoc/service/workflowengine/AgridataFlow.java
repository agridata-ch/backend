package ch.agridata.workflowpoc.service.workflowengine;


import ch.agridata.workflowpoc.client.DataProviderRestClient;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Dummy for workflow poc
 *
 * @CommentLastReviewed 2026-01-07
 */
@Getter
@RequiredArgsConstructor
public final class AgridataFlow {

  private final AgridataContext initContext;
  private final List<UnaryOperator<AgridataContext>> tasksBefore;
  private final List<UnaryOperator<AgridataContext>> tasksAfter;
  private final DataProviderRestClient dataProviderRestClient;

  public Response run() {

    AgridataContext context = runTasks(initContext, tasksBefore);

    return proxy(
        dataProviderRestClient,
        "dummy",
        null,
        context,
        tasksAfter);
  }

  public Response proxy(DataProviderRestClient client,
                        String path,
                        Object request,
                        AgridataContext context,
                        List<UnaryOperator<AgridataContext>> tasksAfter) {

    Response upstream = client.post(path, request);

    Map<String, String> headers = upstream.getStringHeaders().entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getFirst()));

    context.setResponseHeaders(headers);

    runTasks(context, tasksAfter);

    return forwardResponse(upstream);
  }

  private AgridataContext runTasks(AgridataContext context,
                                   List<UnaryOperator<AgridataContext>> tasks) {
    for (UnaryOperator<AgridataContext> task : tasks) {
      context = task.apply(context);
    }
    return context;
  }

  private Response forwardResponse(Response upstream) {
    StreamingOutput out = os -> streamAndClose(upstream, os);
    return Response.status(upstream.getStatus())
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
