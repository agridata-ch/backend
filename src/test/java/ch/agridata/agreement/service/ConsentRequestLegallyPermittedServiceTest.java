package ch.agridata.agreement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.agridata.agis.api.AgisApi;
import ch.agridata.agis.dto.AgisFarmType;
import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.user.api.UserApi;
import ch.agridata.user.dto.BurDto;
import io.quarkus.arc.Arc;
import io.quarkus.arc.ArcContainer;
import io.quarkus.arc.ManagedContext;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConsentRequestLegallyPermittedServiceTest {

  private static final UUID DATA_REQUEST_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final String UID = "CHE123456789";
  private static final String BUR = "BUR1";

  @Mock
  private ConsentRequestCreationService consentRequestCreationService;
  @Mock
  private AgisApi agisApi;
  @Mock
  private UserApi userApi;
  @Mock
  private AgridataSecurityIdentity identity;
  @Mock
  private ManagedContext context;
  @Mock
  private ArcContainer container;

  private ConsentRequestLegallyPermittedService service;
  private MockedStatic<Arc> arc;

  @BeforeEach
  void setUp() {
    service = new ConsentRequestLegallyPermittedService(consentRequestCreationService, agisApi, userApi, identity, true, 10);
    lenient().when(container.requestContext()).thenReturn(context);
    arc = mockStatic(Arc.class);
    arc.when(Arc::container).thenReturn(container);
  }

  @AfterEach
  void tearDown() {
    arc.close();
  }

  @Test
  void givenUid_whenCreateForUid_thenRunsAsTechnicalUserAndCreatesWithoutBur() throws Exception {
    invoke("createForUid", DATA_REQUEST_ID, UID);

    verify(identity).setRunAsUserId(ConsentRequestLegallyPermittedService.USER_ID_LEGALLY_PERMITTED_CONSENT);
    verify(consentRequestCreationService).createLegallyPermittedConsentRequestIfMissing(DATA_REQUEST_ID, UID, null, null);
  }

  @Test
  void givenBur_whenCreateForBur_thenResolvesUidAndRelationSinceViaAgisAndUserApi() throws Exception {
    var relationSince = LocalDateTime.parse("2026-02-19T10:15:30");
    when(agisApi.fetchFarmForBur(BUR)).thenReturn(Optional.of(new AgisFarmType().uid(UID)));
    when(userApi.getAuthorizedBurs(UID)).thenReturn(List.of(
        BurDto.builder().uid(UID).bur("OTHER").relationSince(LocalDateTime.MIN).build(),
        BurDto.builder().uid(UID).bur(BUR).relationSince(relationSince).build()));

    invoke("createForBur", DATA_REQUEST_ID, BUR);

    verify(consentRequestCreationService).createLegallyPermittedConsentRequestIfMissing(DATA_REQUEST_ID, UID, BUR, relationSince);
  }

  @Test
  void givenNoFarmInAgis_whenCreateForBur_thenBestEffortSwallowsAndCreatesNothing() throws Exception {
    when(agisApi.fetchFarmForBur(BUR)).thenReturn(Optional.empty());

    invoke("createForBur", DATA_REQUEST_ID, BUR);

    verify(consentRequestCreationService, never()).createLegallyPermittedConsentRequestIfMissing(any(), any(), any(), any());
  }

  @Test
  void givenSameKeyAlreadyQueued_whenEnqueue_thenSubmittedOnlyOnce() throws Exception {
    var field = ConsentRequestLegallyPermittedService.class.getDeclaredField("executor");
    field.setAccessible(true);
    var executor = (ThreadPoolExecutor) field.get(service);
    var release = new CountDownLatch(1);
    executor.execute(() -> {
      try {
        release.await();
      } catch (InterruptedException _) {
        Thread.currentThread().interrupt();
      }
    });

    service.enqueueUidBased(DATA_REQUEST_ID, UID);
    service.enqueueUidBased(DATA_REQUEST_ID, UID);
    service.enqueueBurBased(DATA_REQUEST_ID, BUR);
    service.enqueueBurBased(DATA_REQUEST_ID, BUR);

    assertThat(executor.getQueue()).hasSize(2);
    release.countDown();
  }

  @Test
  void givenDisabled_whenEnqueue_thenNothingIsSubmitted() throws Exception {
    var disabledService = new ConsentRequestLegallyPermittedService(consentRequestCreationService, agisApi, userApi, identity, false, 10);
    var field = ConsentRequestLegallyPermittedService.class.getDeclaredField("executor");
    field.setAccessible(true);
    var executor = (ThreadPoolExecutor) field.get(disabledService);

    disabledService.enqueueUidBased(DATA_REQUEST_ID, UID);
    disabledService.enqueueBurBased(DATA_REQUEST_ID, BUR);

    assertThat(executor.getTaskCount()).isZero();
  }

  private void invoke(String method, UUID dataRequestId, String producer) throws Exception {
    Method m = ConsentRequestLegallyPermittedService.class.getDeclaredMethod(method, UUID.class, String.class);
    m.setAccessible(true);
    m.invoke(service, dataRequestId, producer);
  }
}
