package ch.agridata.agreement.service;

import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.DECLINED;
import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.GRANTED;
import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.OPENED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.agridata.agreement.dto.ConsentRequestStateEnum;
import ch.agridata.agreement.mapper.ConsentRequestMapper;
import ch.agridata.agreement.mapper.ConsentRequestMapperImpl;
import ch.agridata.agreement.persistence.ConsentRequestEntity;
import ch.agridata.agreement.persistence.ConsentRequestRepository;
import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.user.api.UserApi;
import ch.agridata.user.dto.UidDto;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.NotFoundException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConsentRequestStateServiceTest {

  private static final UUID DATA_REQUEST_ID = UUID.randomUUID();
  private static final String UID = "CHE123456789";
  private static final String BUR1 = "99910001";
  private static final Instant FIXED_NOW = Instant.parse("2026-08-19T10:00:00Z");
  private static final LocalDateTime FIXED_LOCAL_NOW = LocalDateTime.ofInstant(FIXED_NOW, ZoneOffset.UTC);

  @Mock
  private ConsentRequestRepository consentRequestRepository;
  @Spy
  private final ConsentRequestMapper consentRequestMapper = new ConsentRequestMapperImpl();
  @Mock
  private AuditingService auditingService;
  @Mock
  private AgridataSecurityIdentity identity;
  @Mock
  private UserApi userApi;
  @Mock
  private Clock clock;
  @Mock
  private ConsentRequestSyncService consentRequestSyncService;

  @InjectMocks
  private ConsentRequestStateService consentRequestStateService;

  @BeforeEach
  void authorizeProducer() {
    lenient().when(identity.getKtIdP()).thenReturn("kt");
    lenient().when(identity.getAgateLoginId()).thenReturn("login");
    lenient().when(userApi.getAuthorizedUids("kt", "login")).thenReturn(List.of(UidDto.builder().uid(UID).build()));
    lenient().when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
    lenient().when(clock.instant()).thenReturn(FIXED_NOW);
  }

  private ConsentRequestEntity consentRequest(UUID id, String bur, ConsentRequestEntity.StateEnum stateCode, LocalDateTime lastChange) {
    return ConsentRequestEntity.builder()
        .id(id)
        .dataRequest(DataRequestEntity.builder().id(DATA_REQUEST_ID).build())
        .dataProducerUid(UID)
        .dataProducerBur(bur)
        .stateCode(stateCode)
        .lastStateChangeDate(lastChange)
        .build();
  }

  private void update(UUID id, ConsentRequestEntity.StateEnum target) {
    consentRequestStateService.updateConsentRequestStateAsCurrentDataProducer(id, ConsentRequestStateEnum.valueOf(target.name()));
  }

  // ---- Transition rules (exercised through the public entry point on a UID consent request) ----

  static Stream<TransitionTestCase> transitionCases() {
    return Stream.of(
        // From OPENED to GRANTED or DECLINED is always allowed
        new TransitionTestCase(OPENED, GRANTED, null, true),
        new TransitionTestCase(OPENED, DECLINED, null, true),

        // Staying in the same state is not allowed
        new TransitionTestCase(OPENED, OPENED, null, false),
        new TransitionTestCase(GRANTED, GRANTED, FIXED_LOCAL_NOW.minusSeconds(10), false),
        new TransitionTestCase(DECLINED, DECLINED, FIXED_LOCAL_NOW.minusSeconds(10), false),

        // Switching between GRANTED and DECLINED is always allowed
        new TransitionTestCase(GRANTED, DECLINED, FIXED_LOCAL_NOW.minusSeconds(10), true),
        new TransitionTestCase(DECLINED, GRANTED, FIXED_LOCAL_NOW.minusSeconds(40), true),

        // Reverting to OPENED is allowed within 30 seconds
        new TransitionTestCase(GRANTED, OPENED, FIXED_LOCAL_NOW.minusSeconds(5), true),
        new TransitionTestCase(DECLINED, OPENED, FIXED_LOCAL_NOW.minusSeconds(29), true),

        // Reverting to OPENED is not allowed if time has passed
        new TransitionTestCase(GRANTED, OPENED, FIXED_LOCAL_NOW.minusSeconds(31), false),
        new TransitionTestCase(DECLINED, OPENED, FIXED_LOCAL_NOW.minusDays(5), false)
    );
  }

  @ParameterizedTest(name = "[{index}] {0}")
  @MethodSource("transitionCases")
  void testValidateTransition(TransitionTestCase testCase) {
    var id = UUID.randomUUID();
    var uidConsentRequest = consentRequest(id, null, testCase.from, testCase.lastStateChangeDate);
    when(consentRequestRepository.findActiveUidAndBurBasedByIdAndDataProducerUids(id, List.of(UID))).thenReturn(
        Optional.of(uidConsentRequest));
    when(consentRequestRepository.findActiveUidAndBurBasedByDataRequestIdAndDataProducerUid(DATA_REQUEST_ID, UID)).thenReturn(List.of());

    boolean validationResult = true;
    try {
      update(id, testCase.to);
    } catch (Exception _) {
      validationResult = false;
    }

    assertEquals(
        testCase.expectedAllowed(), validationResult,
        () -> String.format("Expected state transition: %s to be %s", testCase, testCase.expectedAllowed())
    );
  }

  record TransitionTestCase(
      ConsentRequestEntity.StateEnum from,
      ConsentRequestEntity.StateEnum to,
      LocalDateTime lastStateChangeDate,
      boolean expectedAllowed
  ) {
  }

  // ---- Not found ----

  @Test
  void updatingUnknownConsentRequestThrowsNotFound() {
    var id = UUID.randomUUID();
    when(consentRequestRepository.findActiveUidAndBurBasedByIdAndDataProducerUids(id, List.of(UID))).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> update(id, GRANTED));
  }

  // ---- Direct UID consent request edits ----

  @Test
  void directUidEditIsRejectedWhenActiveBurExists() {
    var id = UUID.randomUUID();
    var uidConsentRequest = consentRequest(id, null, OPENED, null);
    when(consentRequestRepository.findActiveUidAndBurBasedByIdAndDataProducerUids(id, List.of(UID))).thenReturn(
        Optional.of(uidConsentRequest));
    when(consentRequestRepository.findActiveUidAndBurBasedByDataRequestIdAndDataProducerUid(DATA_REQUEST_ID, UID))
        .thenReturn(List.of(uidConsentRequest, consentRequest(UUID.randomUUID(), BUR1, GRANTED, null)));

    assertThrows(ValidationException.class, () -> update(id, GRANTED));
    assertEquals(OPENED, uidConsentRequest.getStateCode());
    verify(auditingService, never()).logConsentRequestStateChange(any());
  }

  @Test
  void directUidEditIsAppliedWhenNoActiveBurExists() {
    var id = UUID.randomUUID();
    var uidConsentRequest = consentRequest(id, null, OPENED, null);
    when(consentRequestRepository.findActiveUidAndBurBasedByIdAndDataProducerUids(id, List.of(UID))).thenReturn(
        Optional.of(uidConsentRequest));
    when(consentRequestRepository.findActiveUidAndBurBasedByDataRequestIdAndDataProducerUid(DATA_REQUEST_ID, UID))
        .thenReturn(List.of(uidConsentRequest));

    update(id, GRANTED);

    assertEquals(GRANTED, uidConsentRequest.getStateCode());
    verify(auditingService).logConsentRequestStateChange(uidConsentRequest);
  }

  // ---- BUR consent request edits delegate the UID roll-up to the sync service ----
  // (the roll-up derivation itself is covered by ConsentRequestSyncServiceTest)

  @Test
  void burEditAppliesTargetStateAuditsBurAndDelegatesToSync() {
    var burId = UUID.randomUUID();
    var burConsentRequest = consentRequest(burId, BUR1, OPENED, null);
    when(consentRequestRepository.findActiveUidAndBurBasedByIdAndDataProducerUids(burId, List.of(UID)))
        .thenReturn(Optional.of(burConsentRequest));

    update(burId, GRANTED);

    assertEquals(GRANTED, burConsentRequest.getStateCode());
    verify(auditingService).logConsentRequestStateChange(burConsentRequest);
    verify(consentRequestSyncService).syncUidConsentRequestStateWithBurConsentRequests(DATA_REQUEST_ID, UID);
  }

  @Test
  void burEditWithInvalidTransitionIsRejectedAndDoesNotDelegateToSync() {
    var burId = UUID.randomUUID();
    var burConsentRequest = consentRequest(burId, BUR1, OPENED, null);
    when(consentRequestRepository.findActiveUidAndBurBasedByIdAndDataProducerUids(burId, List.of(UID)))
        .thenReturn(Optional.of(burConsentRequest));

    assertThrows(ValidationException.class, () -> update(burId, OPENED));

    assertEquals(OPENED, burConsentRequest.getStateCode());
    verify(auditingService, never()).logConsentRequestStateChange(any());
    verify(consentRequestSyncService, never()).syncUidConsentRequestStateWithBurConsentRequests(any(), any());
  }
}
