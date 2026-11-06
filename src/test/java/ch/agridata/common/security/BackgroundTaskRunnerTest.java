package ch.agridata.common.security;

import static ch.agridata.common.filters.PreSecurityMdcFilter.REQUEST_ID_MDC_FIELD;
import static ch.agridata.common.filters.PreSecurityMdcFilter.USER_ID_MDC_FIELD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.quarkus.arc.Arc;
import io.quarkus.arc.ArcContainer;
import io.quarkus.arc.InjectableContext;
import io.quarkus.arc.ManagedContext;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.jboss.logging.MDC;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BackgroundTaskRunnerTest {

  private static final UUID USER_ID = UUID.fromString("9fc651e8-def0-4456-a8d4-7d4c9d3dfc04");

  @Mock
  private AgridataSecurityIdentity identity;
  @Mock
  private ManagedExecutor managedExecutor;
  @Mock
  private ManagedContext context;
  @Mock
  private ArcContainer container;

  private BackgroundTaskRunner runner;
  private MockedStatic<Arc> arc;

  @BeforeEach
  void setUp() {
    runner = new BackgroundTaskRunner(identity, managedExecutor);
    lenient().when(container.requestContext()).thenReturn(context);
    arc = mockStatic(Arc.class);
    arc.when(Arc::container).thenReturn(container);
  }

  @AfterEach
  void tearDown() {
    arc.close();
    MDC.remove(REQUEST_ID_MDC_FIELD);
    MDC.remove(USER_ID_MDC_FIELD);
  }

  @Test
  void givenNoRequestId_runAs_runsAsUserUnderNewRequestIdInFreshContextAndTerminatesIt() {
    var requestIdDuringWork = new AtomicReference<>();
    var userIdDuringWork = new AtomicReference<>();

    runner.runAs(
        USER_ID, () -> {
          requestIdDuringWork.set(MDC.get(REQUEST_ID_MDC_FIELD));
          userIdDuringWork.set(MDC.get(USER_ID_MDC_FIELD));
        }
    );

    var order = inOrder(context, identity);
    order.verify(context).activate();
    order.verify(identity).setRunAsUserId(USER_ID);
    order.verify(context).terminate();
    assertThat(requestIdDuringWork.get()).isNotNull();
    assertThat(userIdDuringWork.get()).isEqualTo(USER_ID.toString());
    assertThat(MDC.get(REQUEST_ID_MDC_FIELD)).isNull();
    assertThat(MDC.get(USER_ID_MDC_FIELD)).isNull();
  }

  @Test
  void givenActiveRequest_runAs_runsInFreshContextUnderSameRequestIdAndRestoresPreviousContextAndLoggingContext() {
    var previousState = mock(InjectableContext.ContextState.class);
    when(context.isActive()).thenReturn(true);
    when(context.getState()).thenReturn(previousState);
    MDC.put(REQUEST_ID_MDC_FIELD, "request-1");
    MDC.put(USER_ID_MDC_FIELD, "request-user");

    var result = runner.runAs(USER_ID, () -> MDC.get(REQUEST_ID_MDC_FIELD) + "|" + MDC.get(USER_ID_MDC_FIELD));

    assertThat(result).isEqualTo("request-1|" + USER_ID);
    var order = inOrder(context);
    order.verify(context).activate();
    order.verify(context).terminate();
    order.verify(context).activate(previousState);
    assertThat(MDC.get(REQUEST_ID_MDC_FIELD)).isEqualTo("request-1");
    assertThat(MDC.get(USER_ID_MDC_FIELD)).isEqualTo("request-user");
  }

  @Test
  void givenWorkThrows_runAs_stillTerminatesContextAndRestoresLoggingContext() {
    assertThatThrownBy(() -> runner.runAs(
        USER_ID, () -> {
          throw new IllegalStateException("boom");
        }
    )).isInstanceOf(IllegalStateException.class);

    verify(context).terminate();
    verify(context, never()).activate(any());
    assertThat(MDC.get(REQUEST_ID_MDC_FIELD)).isNull();
    assertThat(MDC.get(USER_ID_MDC_FIELD)).isNull();
  }

  @Test
  void whenRunAsyncAs_thenSubmitsWorkToOwnExecutorRunningAsUserUnderSubmittingRequestId() {
    MDC.put(REQUEST_ID_MDC_FIELD, "request-2");
    var requestIdDuringWork = new AtomicReference<>();

    runner.runAsyncAs(USER_ID, () -> requestIdDuringWork.set(MDC.get(REQUEST_ID_MDC_FIELD)));

    var submitted = ArgumentCaptor.forClass(Runnable.class);
    verify(managedExecutor).execute(submitted.capture());
    MDC.put(REQUEST_ID_MDC_FIELD, "other-request");
    submitted.getValue().run();
    verify(identity).setRunAsUserId(USER_ID);
    verify(context).terminate();
    assertThat(requestIdDuringWork.get()).isEqualTo("request-2");
    assertThat(MDC.get(REQUEST_ID_MDC_FIELD)).isEqualTo("other-request");
  }

  @Test
  void givenActiveRequest_whenWorkThrows_runAs_stillTerminatesFreshContextAndRestoresPreviousContextAndLoggingContext() {
    var previousState = mock(InjectableContext.ContextState.class);
    when(context.isActive()).thenReturn(true);
    when(context.getState()).thenReturn(previousState);
    MDC.put(REQUEST_ID_MDC_FIELD, "request-1");
    MDC.put(USER_ID_MDC_FIELD, "request-user");

    assertThatThrownBy(() -> runner.runAs(
        USER_ID, () -> {
          throw new IllegalStateException("boom");
        }
    )).isInstanceOf(IllegalStateException.class);

    var order = inOrder(context);
    order.verify(context).activate();
    order.verify(context).terminate();
    order.verify(context).activate(previousState);
    assertThat(MDC.get(REQUEST_ID_MDC_FIELD)).isEqualTo("request-1");
    assertThat(MDC.get(USER_ID_MDC_FIELD)).isEqualTo("request-user");
  }

  @Test
  void givenAsyncWorkThrows_runAsyncAs_stillTerminatesContextAndLeavesNoLoggingContextBehind() {
    MDC.put(REQUEST_ID_MDC_FIELD, "request-2");
    runner.runAsyncAs(
        USER_ID, () -> {
          throw new IllegalStateException("boom");
        }
    );
    var submitted = ArgumentCaptor.forClass(Runnable.class);
    verify(managedExecutor).execute(submitted.capture());
    MDC.remove(REQUEST_ID_MDC_FIELD);

    assertThatThrownBy(() -> submitted.getValue().run()).isInstanceOf(IllegalStateException.class);

    verify(context).terminate();
    assertThat(MDC.get(REQUEST_ID_MDC_FIELD)).isNull();
    assertThat(MDC.get(USER_ID_MDC_FIELD)).isNull();
  }

  @Test
  void givenOwnExecutor_runAsyncAs_submitsWorkToItInsteadOfManagedExecutorAndRunsItAsUserUnderSubmittingRequestId() {
    MDC.put(REQUEST_ID_MDC_FIELD, "request-3");
    var executor = mock(Executor.class);
    var requestIdDuringWork = new AtomicReference<>();
    var submitted = ArgumentCaptor.forClass(Runnable.class);

    runner.runAsyncAs(USER_ID, () -> requestIdDuringWork.set(MDC.get(REQUEST_ID_MDC_FIELD)), executor);

    verify(executor).execute(submitted.capture());
    verify(managedExecutor, never()).execute(any());
    verify(identity, never()).setRunAsUserId(any());
    MDC.put(REQUEST_ID_MDC_FIELD, "other-request");
    submitted.getValue().run();
    verify(identity).setRunAsUserId(USER_ID);
    assertThat(requestIdDuringWork.get()).isEqualTo("request-3");
  }

  @Test
  void givenOwnExecutorRejects_runAsyncAs_passesOnRejectionAndDoesNotRunWork() {
    var executor = mock(Executor.class);
    doThrow(new RejectedExecutionException()).when(executor).execute(any());
    var ran = new AtomicReference<>(false);

    assertThatThrownBy(() -> runner.runAsyncAs(USER_ID, () -> ran.set(true), executor))
        .isInstanceOf(RejectedExecutionException.class);

    assertThat(ran.get()).isFalse();
    verify(identity, never()).setRunAsUserId(any());
  }
}
