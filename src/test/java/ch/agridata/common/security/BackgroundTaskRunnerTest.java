package ch.agridata.common.security;

import static ch.agridata.common.filters.PreSecurityMdcFilter.REQUEST_ID_MDC_FIELD;
import static ch.agridata.common.filters.PreSecurityMdcFilter.USER_ID_MDC_FIELD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
  void whenrunInNewRequest_thenActivatesFreshContextRunsAsUserAndDeactivatesContext() {
    var requestIdDuringWork = new AtomicReference<>();
    var userIdDuringWork = new AtomicReference<>();

    runner.runInNewRequest(USER_ID, () -> {
      requestIdDuringWork.set(MDC.get(REQUEST_ID_MDC_FIELD));
      userIdDuringWork.set(MDC.get(USER_ID_MDC_FIELD));
    });

    var order = inOrder(context, identity);
    order.verify(context).activate();
    order.verify(identity).setRunAsUserId(USER_ID);
    order.verify(identity).setRunAsUserId(null);
    order.verify(context).deactivate();
    assertThat(requestIdDuringWork.get()).isNotNull();
    assertThat(userIdDuringWork.get()).isEqualTo(USER_ID.toString());
    assertThat(MDC.get(REQUEST_ID_MDC_FIELD)).isNull();
    assertThat(MDC.get(USER_ID_MDC_FIELD)).isNull();
  }

  @Test
  void givenActiveContext_runInNewRequest_runsInFreshContextAndRestoresPreviousState() {
    var previousState = mock(InjectableContext.ContextState.class);
    when(context.isActive()).thenReturn(true);
    when(context.getState()).thenReturn(previousState);

    runner.runInNewRequest(USER_ID, () -> {});

    var order = inOrder(context);
    order.verify(context).activate();
    order.verify(context).deactivate();
    order.verify(context).activate(previousState);
  }

  @Test
  void givenNoActiveContext_callInCurrentRequest_activatesContextAndDeactivatesItAfterwards() {
    when(context.isActive()).thenReturn(false);

    runner.callInCurrentRequest(USER_ID, () -> "result");

    var order = inOrder(context, identity);
    order.verify(context).activate();
    order.verify(identity).setRunAsUserId(USER_ID);
    order.verify(context).deactivate();
  }

  @Test
  void givenActiveContext_callInCurrentRequest_reusesContextAndRestoresPreviousUserAndLoggingContext() {
    var previousUserId = UUID.randomUUID();
    when(context.isActive()).thenReturn(true);
    when(identity.getRunAsUserId()).thenReturn(previousUserId);
    MDC.put(REQUEST_ID_MDC_FIELD, "request-1");
    MDC.put(USER_ID_MDC_FIELD, "request-user");
    var idsDuringWork = new AtomicReference<String>();

    var result = runner.callInCurrentRequest(USER_ID, () -> {
      idsDuringWork.set(MDC.get(REQUEST_ID_MDC_FIELD) + "|" + MDC.get(USER_ID_MDC_FIELD));
      return "result";
    });

    assertThat(result).isEqualTo("result");
    verify(context, never()).activate();
    verify(context, never()).deactivate();
    verify(identity).setRunAsUserId(USER_ID);
    verify(identity).setRunAsUserId(previousUserId);
    assertThat(idsDuringWork.get()).isEqualTo("request-1|" + USER_ID);
    assertThat(MDC.get(USER_ID_MDC_FIELD)).isEqualTo("request-user");
  }

  @Test
  void givenWorkThrows_runInNewRequest_stillRestoresIdentityAndDeactivatesContext() {
    assertThatThrownBy(() -> runner.runInNewRequest(
        USER_ID, () -> {
          throw new IllegalStateException("boom");
        }
    )).isInstanceOf(IllegalStateException.class);

    verify(identity).setRunAsUserId(null);
    verify(context).deactivate();
    assertThat(MDC.get(REQUEST_ID_MDC_FIELD)).isNull();
  }

  @Test
  void givenRequestId_wrapForAsync_runsWorkUnderSubmittingRequestIdInFreshContext() {
    MDC.put(REQUEST_ID_MDC_FIELD, "request-2");
    var idsDuringWork = new AtomicReference<String>();

    var wrapped = runner.wrapForAsync(USER_ID, () -> idsDuringWork.set(MDC.get(REQUEST_ID_MDC_FIELD) + "|" + MDC.get(USER_ID_MDC_FIELD)));
    MDC.put(REQUEST_ID_MDC_FIELD, "other-request");
    wrapped.run();

    verify(context).activate();
    verify(identity).setRunAsUserId(USER_ID);
    assertThat(idsDuringWork.get()).isEqualTo("request-2|" + USER_ID);
    assertThat(MDC.get(REQUEST_ID_MDC_FIELD)).isEqualTo("other-request");
  }

  @Test
  void whenRunAsync_thenSubmitsWorkToOwnExecutorRunningAsUserUnderSubmittingRequestId() {
    MDC.put(REQUEST_ID_MDC_FIELD, "request-3");
    var requestIdDuringWork = new AtomicReference<>();

    runner.runAsync(USER_ID, () -> requestIdDuringWork.set(MDC.get(REQUEST_ID_MDC_FIELD)));

    var submitted = ArgumentCaptor.forClass(Runnable.class);
    verify(managedExecutor).execute(submitted.capture());
    MDC.put(REQUEST_ID_MDC_FIELD, "other-request");
    submitted.getValue().run();
    verify(identity).setRunAsUserId(USER_ID);
    assertThat(requestIdDuringWork.get()).isEqualTo("request-3");
  }
}
