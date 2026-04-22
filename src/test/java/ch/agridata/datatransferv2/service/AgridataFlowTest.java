package ch.agridata.datatransferv2.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AgridataFlowTest {

  private static final UUID PRODUCT_ID = UUID.randomUUID();
  private static final String REQUEST_ID = "test-request-id";

  private AgridataFlow agridataFlow;

  @BeforeEach
  void setUp() {
    agridataFlow = new AgridataFlow();
  }

  @Test
  void givenTasksAndContext_whenRunIsCalled_thenTasksAreExecutedInOrder() {
    var context = createContext();
    var taskResults = new java.util.ArrayList<String>();

    UnaryOperator<AgridataContext> task1 = ctx -> {
      taskResults.add("task1");
      return ctx;
    };
    UnaryOperator<AgridataContext> task2 = ctx -> {
      taskResults.add("task2");
      return ctx;
    };
    UnaryOperator<AgridataContext> task3 = ctx -> {
      taskResults.add("task3");
      ctx.setProviderRequest(() -> createMockResponse("response-body"));
      return ctx;
    };

    var response = agridataFlow.run(context, List.of(task1, task2, task3), List.of());

    assertThat(taskResults).containsExactly("task1", "task2", "task3");
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getHeaderString("AGRIDATA-REQUEST-ID")).isEqualTo(REQUEST_ID);
  }

  @Test
  void givenTaskThrowsException_whenRunIsCalled_thenExceptionIsPropagated() {
    var context = createContext();

    List<UnaryOperator<AgridataContext>> tasksBefore = List.of(ctx -> {
      throw new IllegalArgumentException("Task failed");
    });

    List<UnaryOperator<AgridataContext>> tasksAfter = List.of();

    assertThatThrownBy(() -> agridataFlow.run(context, tasksBefore, tasksAfter))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Task failed");
  }

  @Test
  void givenTasksAfter_whenRunIsCalled_thenTasksAfterAreExecutedAfterProxy() {
    var context = createContext();
    var taskResults = new java.util.ArrayList<String>();

    UnaryOperator<AgridataContext> taskBefore = ctx -> {
      taskResults.add("before");
      ctx.setProviderRequest(() -> createMockResponse("response"));
      return ctx;
    };

    UnaryOperator<AgridataContext> taskAfter = ctx -> {
      taskResults.add("after");
      assertThat(ctx.getResponseHeaders()).isNotNull();
      return ctx;
    };

    agridataFlow.run(context, List.of(taskBefore), List.of(taskAfter));

    assertThat(taskResults).containsExactly("before", "after");
  }

  @Test
  void givenUpstreamResponse_whenProxied_thenResponseIsForwarded() throws IOException {
    var context = createContext();

    UnaryOperator<AgridataContext> task = ctx -> {
      ctx.setProviderRequest(() -> createMockResponse("upstream-data"));
      return ctx;
    };

    var response = agridataFlow.run(context, List.of(task), List.of());
    var baos = new ByteArrayOutputStream();
    ((StreamingOutput) response.getEntity()).write(baos);

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getMediaType()).isEqualTo(MediaType.APPLICATION_JSON_TYPE);
    assertThat(baos.toByteArray()).isEqualTo("upstream-data".getBytes(StandardCharsets.UTF_8));
  }

  private AgridataContext createContext() {
    return AgridataContext.builder()
        .dataTransferRequestId(REQUEST_ID)
        .productId(PRODUCT_ID)
        .flowEnum(FlowEnum.UID_BASED_PRE_VALIDATION)
        .build();
  }

  private Response createMockResponse(String body) {
    var response = mock(Response.class);
    var headers = new MultivaluedHashMap<String, String>();
    headers.putSingle("Content-Type", "application/json");

    when(response.getStatus()).thenReturn(200);
    when(response.getMediaType()).thenReturn(MediaType.APPLICATION_JSON_TYPE);
    when(response.getStringHeaders()).thenReturn(headers);
    when(response.readEntity(java.io.InputStream.class))
        .thenReturn(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));

    return response;
  }
}
