package ch.agridata.agreement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.agridata.agreement.dto.SignatureSlotCodeEnum;
import ch.agridata.agreement.persistence.OtpChallengeEntity;
import ch.agridata.agreement.persistence.OtpChallengeRepository;
import ch.agridata.aws.api.SmsApi;
import ch.agridata.common.exceptions.ExternalWebServiceException;
import jakarta.validation.ValidationException;
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
  private final Instant fixedNow = Instant.parse("2026-03-19T10:00:00Z");

  @BeforeEach
  void setUp() {
    lenient().when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
    lenient().when(clock.instant()).thenReturn(fixedNow);
  }

  @Test
  void givenNoRecentChallenge_whenCreateChallenge_thenPersistAndReturnEntity() {
    when(otpChallengeRepository.existsRecentChallenge(eq(USER_ID), eq(REVISION_ID), eq(PERSISTENCE_SLOT_CODE), any()))
        .thenReturn(false);

    OtpChallengeEntity result = otpChallengeService.createChallenge(USER_ID, REVISION_ID, SLOT_CODE, PHONE);

    assertThat(result).isNotNull();
    assertThat(result.getUserId()).isEqualTo(USER_ID);
    assertThat(result.getOtpHash()).isNotBlank();

    verify(otpChallengeRepository).persist(result);
  }

  @Test
  void givenRecentChallengeExists_whenCreateChallenge_thenThrowValidationException() {
    when(otpChallengeRepository.existsRecentChallenge(eq(USER_ID), eq(REVISION_ID), eq(PERSISTENCE_SLOT_CODE), any()))
        .thenReturn(true);

    assertThatThrownBy(() -> otpChallengeService.createChallenge(USER_ID, REVISION_ID, SLOT_CODE, PHONE))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("sent too recently");

    verify(otpChallengeRepository, never()).persist((OtpChallengeEntity) any());
  }

  @Test
  void givenValidActiveChallenge_whenVerifyAndConsume_thenSetConsumedAt() {
    UUID challengeId = UUID.randomUUID();
    OtpChallengeEntity entity = buildValidEntity(challengeId);
    when(otpChallengeRepository.findActiveById(challengeId, LocalDateTime.now(clock))).thenReturn(Optional.of(entity));

    otpChallengeService.verifyAndConsume(challengeId, USER_ID, REVISION_ID, SLOT_CODE, "123456");

    assertThat(entity.getConsumedAt()).isEqualTo(LocalDateTime.now(clock));
    assertThat(entity.getAttemptCount()).isEqualTo(1);
  }

  @Test
  void givenExpiredChallenge_whenVerifyAndConsume_thenThrowValidationException() {
    UUID challengeId = UUID.randomUUID();
    OtpChallengeEntity entity = buildValidEntity(challengeId);
    entity.setExpiresAt(LocalDateTime.now(clock).minusSeconds(1));
    when(otpChallengeRepository.findActiveById(challengeId, LocalDateTime.now(clock))).thenReturn(Optional.empty());

    assertThatThrownBy(() ->
        otpChallengeService.verifyAndConsume(challengeId, USER_ID, REVISION_ID, SLOT_CODE, "123456"))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("expired");
  }

  @Test
  void givenMismatchingMetadata_whenVerifyAndConsume_thenThrowValidationException() {
    UUID challengeId = UUID.randomUUID();
    OtpChallengeEntity entity = buildValidEntity(challengeId);
    UUID wrongUserId = UUID.randomUUID();
    when(otpChallengeRepository.findActiveById(challengeId, LocalDateTime.now(clock))).thenReturn(Optional.of(entity));

    assertThatThrownBy(() ->
        otpChallengeService.verifyAndConsume(challengeId, wrongUserId, REVISION_ID, SLOT_CODE, "123456"))
        .isInstanceOf(ValidationException.class)
        .hasMessage("Invalid OTP challenge.");
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
        .expiresAt(LocalDateTime.now(clock).plusMinutes(4))
        .maxAttempts(3)
        .attemptCount(0)
        .build();
  }
}
