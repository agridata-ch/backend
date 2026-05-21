package ch.agridata.agreement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ch.agridata.agreement.dto.SignatureSlotCodeEnum;
import ch.agridata.agreement.persistence.OtpChallengeEntity;
import ch.agridata.agreement.persistence.OtpChallengeRepository;
import ch.agridata.aws.api.SmsApi;
import ch.agridata.common.exceptions.ExternalWebServiceException;
import ch.agridata.common.exceptions.OtpExpiredException;
import ch.agridata.common.exceptions.OtpInvalidException;
import ch.agridata.common.exceptions.OtpLockedException;
import ch.agridata.common.exceptions.OtpResendCooldownException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OtpChallengeServiceTest {

  @Mock
  private OtpChallengeRepository otpChallengeRepository;

  @Mock
  private OtpChallengeAttemptRecorder attemptRecorder;

  @Mock
  private Clock clock;

  @Mock
  private SmsApi smsApi;

  @Captor
  private ArgumentCaptor<String> stringArgumentCaptor;

  @InjectMocks
  private OtpChallengeService otpChallengeService;

  private static final UUID USER_ID = UUID.randomUUID();
  private static final UUID REVISION_ID = UUID.randomUUID();
  private static final SignatureSlotCodeEnum SLOT_CODE = SignatureSlotCodeEnum.DATA_CONSUMER_01;
  private static final OtpChallengeEntity.SignatureSlotCodeEnum PERSISTENCE_SLOT_CODE =
      OtpChallengeEntity.SignatureSlotCodeEnum.DATA_CONSUMER_01;
  private static final String PHONE = "+41791234567";
  // Hex-encoded SHA-256 of "123456".
  private static final String HASH_OF_123456 = "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92";

  private final Instant fixedNow = Instant.parse("2026-03-19T10:00:00Z");

  @BeforeEach
  void setUp() {
    lenient().when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
    lenient().when(clock.instant()).thenReturn(fixedNow);
    otpChallengeService.validationEnabled = true;
  }

  @Test
  void givenNoRecentChallenge_whenCreateChallenge_thenPersistAndReturnEntity() {
    when(otpChallengeRepository.existsRecentChallenge(eq(USER_ID), eq(REVISION_ID), eq(PERSISTENCE_SLOT_CODE), any()))
        .thenReturn(false);

    OtpChallengeEntity result = otpChallengeService.createChallenge(USER_ID, REVISION_ID, SLOT_CODE, PHONE);

    assertThat(result).isNotNull();
    assertThat(result.getUserId()).isEqualTo(USER_ID);
    assertThat(result.getOtpHash()).isNotBlank();
    assertThat(result.getMaxAttempts()).isEqualTo(3);

    verify(otpChallengeRepository).persist(result);
  }

  @Test
  void givenRecentChallengeExists_whenCreateChallenge_thenThrowOtpResendCooldownException() {
    when(otpChallengeRepository.existsRecentChallenge(eq(USER_ID), eq(REVISION_ID), eq(PERSISTENCE_SLOT_CODE), any()))
        .thenReturn(true);

    assertThatThrownBy(() -> otpChallengeService.createChallenge(USER_ID, REVISION_ID, SLOT_CODE, PHONE))
        .isInstanceOfSatisfying(OtpResendCooldownException.class,
            ex -> assertThat(ex.getRetryAfterSeconds()).isEqualTo(30L));

    verify(otpChallengeRepository, never()).persist((OtpChallengeEntity) any());
    verifyNoInteractions(smsApi);
  }

  @Test
  void givenCorrectCode_whenVerifyAndConsume_thenSetConsumedAt() {
    UUID challengeId = UUID.randomUUID();
    OtpChallengeEntity entity = buildValidEntity(challengeId);
    when(otpChallengeRepository.findByIdOptional(challengeId)).thenReturn(Optional.of(entity));

    otpChallengeService.verifyAndConsume(challengeId, USER_ID, REVISION_ID, SLOT_CODE, "123456");

    assertThat(entity.getConsumedAt()).isEqualTo(LocalDateTime.now(clock));
    verify(attemptRecorder).incrementAttemptCount(challengeId);
  }

  @Test
  void givenWrongCodeFirstAttempt_whenVerifyAndConsume_thenOtpInvalidAndIncrementOnce() {
    UUID challengeId = UUID.randomUUID();
    OtpChallengeEntity entity = buildValidEntity(challengeId);
    entity.setAttemptCount(0);
    when(otpChallengeRepository.findByIdOptional(challengeId)).thenReturn(Optional.of(entity));

    assertThatThrownBy(() ->
        otpChallengeService.verifyAndConsume(challengeId, USER_ID, REVISION_ID, SLOT_CODE, "999999"))
        .isInstanceOf(OtpInvalidException.class);

    verify(attemptRecorder).incrementAttemptCount(challengeId);
    assertThat(entity.getConsumedAt()).isNull();
  }

  @Test
  void givenWrongCodeSecondAttempt_whenVerifyAndConsume_thenOtpInvalid() {
    UUID challengeId = UUID.randomUUID();
    OtpChallengeEntity entity = buildValidEntity(challengeId);
    entity.setAttemptCount(1);
    when(otpChallengeRepository.findByIdOptional(challengeId)).thenReturn(Optional.of(entity));

    assertThatThrownBy(() ->
        otpChallengeService.verifyAndConsume(challengeId, USER_ID, REVISION_ID, SLOT_CODE, "999999"))
        .isInstanceOf(OtpInvalidException.class);

    verify(attemptRecorder).incrementAttemptCount(challengeId);
  }

  @Test
  void givenWrongCodeThirdAttempt_whenVerifyAndConsume_thenOtpLocked() {
    UUID challengeId = UUID.randomUUID();
    OtpChallengeEntity entity = buildValidEntity(challengeId);
    entity.setAttemptCount(2);
    when(otpChallengeRepository.findByIdOptional(challengeId)).thenReturn(Optional.of(entity));
    // Simulate that the recorder has bumped the persisted counter to 3 (== max_attempts) in its
    // own transaction. The in-memory entity stays at 2 because the outer code has not reloaded.
    doAnswer(invocation -> null).when(attemptRecorder).incrementAttemptCount(challengeId);

    assertThatThrownBy(() ->
        otpChallengeService.verifyAndConsume(challengeId, USER_ID, REVISION_ID, SLOT_CODE, "999999"))
        .isInstanceOf(OtpLockedException.class);

    verify(attemptRecorder).incrementAttemptCount(challengeId);
  }

  @Test
  void givenAlreadyLockedChallenge_whenVerifyAndConsume_thenOtpLockedWithoutIncrement() {
    UUID challengeId = UUID.randomUUID();
    OtpChallengeEntity entity = buildValidEntity(challengeId);
    entity.setAttemptCount(3);
    when(otpChallengeRepository.findByIdOptional(challengeId)).thenReturn(Optional.of(entity));

    assertThatThrownBy(() ->
        otpChallengeService.verifyAndConsume(challengeId, USER_ID, REVISION_ID, SLOT_CODE, "123456"))
        .isInstanceOf(OtpLockedException.class);

    verifyNoInteractions(attemptRecorder);
  }

  @Test
  void givenExpiredChallenge_whenVerifyAndConsume_thenOtpExpiredAndNoIncrement() {
    UUID challengeId = UUID.randomUUID();
    OtpChallengeEntity entity = buildValidEntity(challengeId);
    entity.setExpiresAt(LocalDateTime.now(clock).minusSeconds(1));
    when(otpChallengeRepository.findByIdOptional(challengeId)).thenReturn(Optional.of(entity));

    assertThatThrownBy(() ->
        otpChallengeService.verifyAndConsume(challengeId, USER_ID, REVISION_ID, SLOT_CODE, "123456"))
        .isInstanceOf(OtpExpiredException.class);

    verifyNoInteractions(attemptRecorder);
  }

  @Test
  void givenAlreadyConsumedChallenge_whenVerifyAndConsume_thenOtpInvalid() {
    UUID challengeId = UUID.randomUUID();
    OtpChallengeEntity entity = buildValidEntity(challengeId);
    entity.setConsumedAt(LocalDateTime.now(clock).minusMinutes(1));
    when(otpChallengeRepository.findByIdOptional(challengeId)).thenReturn(Optional.of(entity));

    assertThatThrownBy(() ->
        otpChallengeService.verifyAndConsume(challengeId, USER_ID, REVISION_ID, SLOT_CODE, "123456"))
        .isInstanceOf(OtpInvalidException.class);

    verifyNoInteractions(attemptRecorder);
  }

  @Test
  void givenMismatchingMetadata_whenVerifyAndConsume_thenOtpInvalid() {
    UUID challengeId = UUID.randomUUID();
    OtpChallengeEntity entity = buildValidEntity(challengeId);
    UUID wrongUserId = UUID.randomUUID();
    when(otpChallengeRepository.findByIdOptional(challengeId)).thenReturn(Optional.of(entity));

    assertThatThrownBy(() ->
        otpChallengeService.verifyAndConsume(challengeId, wrongUserId, REVISION_ID, SLOT_CODE, "123456"))
        .isInstanceOf(OtpInvalidException.class);

    verifyNoInteractions(attemptRecorder);
  }

  @Test
  void givenChallengeNotFound_whenVerifyAndConsume_thenOtpInvalid() {
    UUID challengeId = UUID.randomUUID();
    when(otpChallengeRepository.findByIdOptional(challengeId)).thenReturn(Optional.empty());

    assertThatThrownBy(() ->
        otpChallengeService.verifyAndConsume(challengeId, USER_ID, REVISION_ID, SLOT_CODE, "123456"))
        .isInstanceOf(OtpInvalidException.class);

    verifyNoInteractions(attemptRecorder);
  }

  @Test
  void givenValidationDisabled_whenCreateChallenge_thenSkipCooldownCheckAndPersist() {
    otpChallengeService.validationEnabled = false;

    OtpChallengeEntity result = otpChallengeService.createChallenge(USER_ID, REVISION_ID, SLOT_CODE, PHONE);

    verify(otpChallengeRepository, never()).existsRecentChallenge(any(), any(), any(), any());
    verify(otpChallengeRepository).persist(result);
  }

  @Test
  void givenValidationDisabled_whenVerifyAndConsumeWithAnyCode_thenAccept() {
    UUID challengeId = UUID.randomUUID();
    otpChallengeService.validationEnabled = false;
    OtpChallengeEntity entity = buildValidEntity(challengeId);
    entity.setExpiresAt(LocalDateTime.now(clock).minusSeconds(5));
    entity.setAttemptCount(99);
    when(otpChallengeRepository.findByIdOptional(challengeId)).thenReturn(Optional.of(entity));

    otpChallengeService.verifyAndConsume(challengeId, USER_ID, REVISION_ID, SLOT_CODE, "any-code");

    assertThat(entity.getConsumedAt()).isEqualTo(LocalDateTime.now(clock));
    verifyNoInteractions(attemptRecorder);
  }

  @Test
  void givenValidationDisabledAndIdentityMismatch_whenVerifyAndConsume_thenOtpInvalid() {
    UUID challengeId = UUID.randomUUID();
    otpChallengeService.validationEnabled = false;
    OtpChallengeEntity entity = buildValidEntity(challengeId);
    UUID wrongUserId = UUID.randomUUID();
    when(otpChallengeRepository.findByIdOptional(challengeId)).thenReturn(Optional.of(entity));

    assertThatThrownBy(() ->
        otpChallengeService.verifyAndConsume(challengeId, wrongUserId, REVISION_ID, SLOT_CODE, "123456"))
        .isInstanceOf(OtpInvalidException.class);

    assertThat(entity.getConsumedAt()).isNull();
    verifyNoInteractions(attemptRecorder);
  }

  @Test
  void givenValidRequest_whenCreateChallenge_thenSmsApiIsCalledWithCorrectData() {
    when(otpChallengeRepository.existsRecentChallenge(any(), any(), any(), any())).thenReturn(false);

    otpChallengeService.createChallenge(USER_ID, REVISION_ID, SLOT_CODE, PHONE);

    verify(smsApi).sendSms(eq(PHONE), stringArgumentCaptor.capture());
    assertThat(stringArgumentCaptor.getValue()).contains("Sicherheitscode");
    assertThat(stringArgumentCaptor.getValue()).containsPattern("\\d{6}");
  }

  @Test
  void givenSmsApiThrowsException_whenCreateChallenge_thenExceptionPropagates() {
    when(otpChallengeRepository.existsRecentChallenge(any(), any(), any(), any())).thenReturn(false);
    doThrow(new ExternalWebServiceException("Service temporarily unavailable. Please try again later."))
        .when(smsApi).sendSms(any(), any());

    assertThatThrownBy(() -> otpChallengeService.createChallenge(USER_ID, REVISION_ID, SLOT_CODE, PHONE))
        .isInstanceOf(ExternalWebServiceException.class)
        .hasMessageContaining("Service temporarily unavailable");
  }

  private OtpChallengeEntity buildValidEntity(UUID challengeId) {
    return OtpChallengeEntity.builder()
        .id(challengeId)
        .userId(USER_ID)
        .contractRevisionId(REVISION_ID)
        .signatureSlotCode(PERSISTENCE_SLOT_CODE)
        .otpHash(HASH_OF_123456)
        .expiresAt(LocalDateTime.now(clock).plusMinutes(4))
        .maxAttempts(3)
        .attemptCount(0)
        .build();
  }
}
