package ch.agridata.agreement.service;

import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.DECLINED;
import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.GRANTED;
import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.LEGALLY_PERMITTED;
import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.OPENED;
import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.WITHDRAWN;
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
  private static final String BUR1 = "A99910001";
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

  static Stream<TransitionTestCase> producerTransitionCases() {
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
        new TransitionTestCase(DECLINED, OPENED, FIXED_LOCAL_NOW.minusDays(5), false),

        // Withdrawing is reserved for the consumer
        new TransitionTestCase(OPENED, WITHDRAWN, null, false),
        new TransitionTestCase(GRANTED, WITHDRAWN, FIXED_LOCAL_NOW.minusSeconds(10), false),
        new TransitionTestCase(DECLINED, WITHDRAWN, FIXED_LOCAL_NOW.minusSeconds(10), false),

        // Leaving WITHDRAWN is never allowed
        new TransitionTestCase(WITHDRAWN, OPENED, FIXED_LOCAL_NOW.minusSeconds(5), false),
        new TransitionTestCase(WITHDRAWN, GRANTED, FIXED_LOCAL_NOW.minusSeconds(10), false),
        new TransitionTestCase(WITHDRAWN, DECLINED, FIXED_LOCAL_NOW.minusSeconds(10), false),

        // Leaving LEGALLY_PERMITTED is never allowed
        new TransitionTestCase(LEGALLY_PERMITTED, WITHDRAWN, FIXED_LOCAL_NOW.minusSeconds(10), false)
    );
  }

  @ParameterizedTest(name = "[{index}] {0}")
  @MethodSource("producerTransitionCases")
  void testValidateProducerTransition(TransitionTestCase testCase) {
    var id = UUID.randomUUID();
    var uidConsentRequest = consentRequest(id, null, testCase.from, testCase.lastStateChangeDate);
    when(consentRequestRepository.findActiveByIdAndDataProducerUids(id, List.of(UID))).thenReturn(
        Optional.of(uidConsentRequest));
    when(consentRequestRepository.findActiveByDataRequestIdAndDataProducerUid(DATA_REQUEST_ID, UID)).thenReturn(List.of());

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

  // ---- Withdrawal by the consumer ----

  private static final String CONSUMER_UID = "CHE987654321";

  private void updateAsConsumer(UUID id, ConsentRequestEntity.StateEnum target) {
    consentRequestStateService.updateConsentRequestStateAsCurrentDataConsumer(id, ConsentRequestStateEnum.valueOf(target.name()));
  }

  static Stream<TransitionTestCase> consumerTransitionCases() {
    return Stream.of(
        // Withdrawing is always allowed from OPENED, GRANTED or DECLINED, regardless of time passed
        new TransitionTestCase(OPENED, WITHDRAWN, null, true),
        new TransitionTestCase(GRANTED, WITHDRAWN, FIXED_LOCAL_NOW.minusSeconds(10), true),
        new TransitionTestCase(DECLINED, WITHDRAWN, FIXED_LOCAL_NOW.minusDays(5), true),

        // Staying in WITHDRAWN is not allowed
        new TransitionTestCase(WITHDRAWN, WITHDRAWN, FIXED_LOCAL_NOW.minusSeconds(10), false),

        // Leaving WITHDRAWN is never allowed
        new TransitionTestCase(WITHDRAWN, OPENED, FIXED_LOCAL_NOW.minusSeconds(5), false),
        new TransitionTestCase(WITHDRAWN, GRANTED, FIXED_LOCAL_NOW.minusSeconds(10), false),
        new TransitionTestCase(WITHDRAWN, DECLINED, FIXED_LOCAL_NOW.minusSeconds(10), false),

        // Producer transitions are reserved for the producer
        new TransitionTestCase(OPENED, GRANTED, null, false),
        new TransitionTestCase(OPENED, DECLINED, null, false),
        new TransitionTestCase(GRANTED, DECLINED, FIXED_LOCAL_NOW.minusSeconds(10), false),
        new TransitionTestCase(DECLINED, GRANTED, FIXED_LOCAL_NOW.minusSeconds(40), false),
        new TransitionTestCase(GRANTED, OPENED, FIXED_LOCAL_NOW.minusSeconds(5), false),
        new TransitionTestCase(DECLINED, OPENED, FIXED_LOCAL_NOW.minusSeconds(5), false),

        // Leaving LEGALLY_PERMITTED is never allowed
        new TransitionTestCase(LEGALLY_PERMITTED, WITHDRAWN, FIXED_LOCAL_NOW.minusSeconds(10), false)
    );
  }

  @ParameterizedTest(name = "[{index}] {0}")
  @MethodSource("consumerTransitionCases")
  void testValidateConsumerTransition(TransitionTestCase testCase) {
    var id = UUID.randomUUID();
    var uidConsentRequest = consentRequest(id, null, testCase.from, testCase.lastStateChangeDate);
    when(identity.getUidOrElseThrow()).thenReturn(CONSUMER_UID);
    when(consentRequestRepository.findActiveByIdAndDataConsumerUid(id, CONSUMER_UID)).thenReturn(
        Optional.of(uidConsentRequest));
    when(consentRequestRepository.findActiveByDataRequestIdAndDataProducerUid(DATA_REQUEST_ID, UID)).thenReturn(List.of());

    boolean validationResult = true;
    try {
      updateAsConsumer(id, testCase.to);
    } catch (Exception _) {
      validationResult = false;
    }

    assertEquals(
        testCase.expectedAllowed(), validationResult,
        () -> String.format("Expected state transition: %s to be %s", testCase, testCase.expectedAllowed())
    );
  }

  @ParameterizedTest
  @MethodSource("withdrawableStates")
  void consumerCanWithdrawConsentRequest(ConsentRequestEntity.StateEnum from) {
    var id = UUID.randomUUID();
    var consentRequest = consentRequest(id, null, from, FIXED_LOCAL_NOW.minusDays(5));
    when(identity.getUidOrElseThrow()).thenReturn(CONSUMER_UID);
    when(consentRequestRepository.findActiveByIdAndDataConsumerUid(id, CONSUMER_UID)).thenReturn(Optional.of(consentRequest));

    updateAsConsumer(id, WITHDRAWN);

    assertEquals(WITHDRAWN, consentRequest.getStateCode());
    verify(auditingService).logConsentRequestStateChange(consentRequest);
  }

  static Stream<ConsentRequestEntity.StateEnum> withdrawableStates() {
    return Stream.of(OPENED, GRANTED, DECLINED);
  }

  @Test
  void consumerCannotWithdrawLegallyPermittedConsentRequest() {
    var id = UUID.randomUUID();
    var consentRequest = consentRequest(id, null, LEGALLY_PERMITTED, null);
    when(identity.getUidOrElseThrow()).thenReturn(CONSUMER_UID);
    when(consentRequestRepository.findActiveByIdAndDataConsumerUid(id, CONSUMER_UID)).thenReturn(Optional.of(consentRequest));

    assertThrows(ValidationException.class, () -> updateAsConsumer(id, WITHDRAWN));
    assertEquals(LEGALLY_PERMITTED, consentRequest.getStateCode());
    verify(auditingService, never()).logConsentRequestStateChange(any());
  }

  @Test
  void consumerWithdrawingBurConsentRequestDelegatesToSync() {
    var burId = UUID.randomUUID();
    var burConsentRequest = consentRequest(burId, BUR1, GRANTED, null);
    when(identity.getUidOrElseThrow()).thenReturn(CONSUMER_UID);
    when(consentRequestRepository.findActiveByIdAndDataConsumerUid(burId, CONSUMER_UID))
        .thenReturn(Optional.of(burConsentRequest));

    updateAsConsumer(burId, WITHDRAWN);

    assertEquals(WITHDRAWN, burConsentRequest.getStateCode());
    verify(auditingService).logConsentRequestStateChange(burConsentRequest);
    verify(consentRequestSyncService).syncUidConsentRequestStateWithBurConsentRequests(DATA_REQUEST_ID, UID);
  }

  @Test
  void consumerCannotWithdrawUidConsentRequestDirectlyWhenActiveBurExists() {
    var id = UUID.randomUUID();
    var uidConsentRequest = consentRequest(id, null, GRANTED, null);
    when(identity.getUidOrElseThrow()).thenReturn(CONSUMER_UID);
    when(consentRequestRepository.findActiveByIdAndDataConsumerUid(id, CONSUMER_UID)).thenReturn(
        Optional.of(uidConsentRequest));
    when(consentRequestRepository.findActiveByDataRequestIdAndDataProducerUid(DATA_REQUEST_ID, UID))
        .thenReturn(List.of(uidConsentRequest, consentRequest(UUID.randomUUID(), BUR1, GRANTED, null)));

    assertThrows(ValidationException.class, () -> updateAsConsumer(id, WITHDRAWN));
    assertEquals(GRANTED, uidConsentRequest.getStateCode());
    verify(auditingService, never()).logConsentRequestStateChange(any());
  }

  @Test
  void consumerCannotWithdrawAlreadyWithdrawnConsentRequest() {
    var id = UUID.randomUUID();
    var consentRequest = consentRequest(id, null, WITHDRAWN, null);
    when(identity.getUidOrElseThrow()).thenReturn(CONSUMER_UID);
    when(consentRequestRepository.findActiveByIdAndDataConsumerUid(id, CONSUMER_UID)).thenReturn(Optional.of(consentRequest));

    assertThrows(ValidationException.class, () -> updateAsConsumer(id, WITHDRAWN));
    verify(auditingService, never()).logConsentRequestStateChange(any());
  }

  @Test
  void consumerCannotPerformProducerTransition() {
    var id = UUID.randomUUID();
    var consentRequest = consentRequest(id, null, OPENED, null);
    when(identity.getUidOrElseThrow()).thenReturn(CONSUMER_UID);
    when(consentRequestRepository.findActiveByIdAndDataConsumerUid(id, CONSUMER_UID)).thenReturn(Optional.of(consentRequest));

    assertThrows(ValidationException.class, () -> updateAsConsumer(id, GRANTED));
    assertEquals(OPENED, consentRequest.getStateCode());
    verify(auditingService, never()).logConsentRequestStateChange(any());
  }

  @Test
  void consumerUpdatingUnknownConsentRequestThrowsNotFound() {
    var id = UUID.randomUUID();
    when(identity.getUidOrElseThrow()).thenReturn(CONSUMER_UID);
    when(consentRequestRepository.findActiveByIdAndDataConsumerUid(id, CONSUMER_UID)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> updateAsConsumer(id, WITHDRAWN));
  }

  // ---- Not found ----

  @Test
  void updatingUnknownConsentRequestThrowsNotFound() {
    var id = UUID.randomUUID();
    when(consentRequestRepository.findActiveByIdAndDataProducerUids(id, List.of(UID))).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> update(id, GRANTED));
  }

  // ---- Direct UID consent request edits ----

  @Test
  void directUidEditIsRejectedWhenActiveBurExists() {
    var id = UUID.randomUUID();
    var uidConsentRequest = consentRequest(id, null, OPENED, null);
    when(consentRequestRepository.findActiveByIdAndDataProducerUids(id, List.of(UID))).thenReturn(
        Optional.of(uidConsentRequest));
    when(consentRequestRepository.findActiveByDataRequestIdAndDataProducerUid(DATA_REQUEST_ID, UID))
        .thenReturn(List.of(uidConsentRequest, consentRequest(UUID.randomUUID(), BUR1, GRANTED, null)));

    assertThrows(ValidationException.class, () -> update(id, GRANTED));
    assertEquals(OPENED, uidConsentRequest.getStateCode());
    verify(auditingService, never()).logConsentRequestStateChange(any());
  }

  @Test
  void directUidEditIsAppliedWhenNoActiveBurExists() {
    var id = UUID.randomUUID();
    var uidConsentRequest = consentRequest(id, null, OPENED, null);
    when(consentRequestRepository.findActiveByIdAndDataProducerUids(id, List.of(UID))).thenReturn(
        Optional.of(uidConsentRequest));
    when(consentRequestRepository.findActiveByDataRequestIdAndDataProducerUid(DATA_REQUEST_ID, UID))
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
    when(consentRequestRepository.findActiveByIdAndDataProducerUids(burId, List.of(UID)))
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
    when(consentRequestRepository.findActiveByIdAndDataProducerUids(burId, List.of(UID)))
        .thenReturn(Optional.of(burConsentRequest));

    assertThrows(ValidationException.class, () -> update(burId, OPENED));

    assertEquals(OPENED, burConsentRequest.getStateCode());
    verify(auditingService, never()).logConsentRequestStateChange(any());
    verify(consentRequestSyncService, never()).syncUidConsentRequestStateWithBurConsentRequests(any(), any());
  }
}
