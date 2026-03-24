package ch.agridata.agreement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import ch.agridata.agreement.dto.OtpChallengeDto;
import ch.agridata.agreement.dto.SignatureSlotCodeEnum;
import ch.agridata.agreement.persistence.ContractRevisionEntity;
import ch.agridata.agreement.persistence.ContractRevisionRepository;
import ch.agridata.agreement.persistence.OtpChallengeEntity;
import ch.agridata.common.security.AgridataSecurityIdentity;
import jakarta.validation.ValidationException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContractRevisionOtpChallengeServiceTest {
  @InjectMocks
  private ContractRevisionOtpChallengeService contractRevisionOtpChallengeService;

  @Mock
  private OtpChallengeService otpChallengeService;

  @Mock
  private AgridataSecurityIdentity agridataSecurityIdentity;

  @Mock
  private ContractRevisionRepository contractRevisionRepository;

  private static final String USER_UID = "012345678";
  private static final UUID USER_ID = UUID.randomUUID();
  private static final UUID REVISION_ID = UUID.randomUUID();
  private static final String PHONE = "+41791234567";

  @Test
  void givenValidInput_whenCreateOtpChallenge_thenReturnDto() {
    SignatureSlotCodeEnum slotCode = SignatureSlotCodeEnum.DATA_CONSUMER_01;
    LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);

    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(USER_UID);
    when(agridataSecurityIdentity.getUserId()).thenReturn(USER_ID);
    when(agridataSecurityIdentity.getPhoneNumberOrElseThrow()).thenReturn(PHONE);

    when(contractRevisionRepository.findByIdAndDataConsumerUid(REVISION_ID, USER_UID))
        .thenReturn(Optional.of(ContractRevisionEntity.builder().build()));

    OtpChallengeEntity challengeEntity = OtpChallengeEntity.builder()
        .id(UUID.randomUUID())
        .expiresAt(expiry)
        .phoneNumberSnapshot(PHONE)
        .build();

    when(otpChallengeService.createChallenge(USER_ID, REVISION_ID, slotCode, PHONE))
        .thenReturn(challengeEntity);
    when(otpChallengeService.getResendCooldown()).thenReturn(Duration.ofSeconds(30));

    OtpChallengeDto result = contractRevisionOtpChallengeService.createOtpChallenge(REVISION_ID, slotCode);

    assertThat(result).isNotNull();
    assertThat(result.challengeId()).isEqualTo(challengeEntity.getId());
    assertThat(result.expiresAt()).isEqualTo(expiry);
    assertThat(result.retryAfterSeconds()).isEqualTo(30);
    assertThat(result.maskedPhoneNumber()).isEqualTo("+41 *******67");
  }

  @Test
  void givenContractNotFound_whenCreateOtpChallenge_thenThrowValidationException() {
    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(USER_UID);
    when(contractRevisionRepository.findByIdAndDataConsumerUid(REVISION_ID, USER_UID))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> contractRevisionOtpChallengeService.createOtpChallenge(REVISION_ID, SignatureSlotCodeEnum.DATA_CONSUMER_01))
        .isInstanceOf(ValidationException.class)
        .hasMessage("Contract revision not found");
  }

  @ParameterizedTest
  @EnumSource(value = SignatureSlotCodeEnum.class, names = {"DATA_PROVIDER_01", "DATA_PROVIDER_02"})
  void givenInvalidSlotId_whenCreateOtpChallenge_thenThrowIllegalArgumentException(SignatureSlotCodeEnum signatureSlotCode) {
    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(USER_UID);
    when(contractRevisionRepository.findByIdAndDataConsumerUid(REVISION_ID, USER_UID))
        .thenReturn(Optional.of(ContractRevisionEntity.builder().build()));

    assertThatThrownBy(() -> contractRevisionOtpChallengeService.createOtpChallenge(REVISION_ID, signatureSlotCode))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid signature slot id");
  }

  @Test
  void givenShortPhoneNumber_whenCreateOtpChallenge_thenMaskCorrectly() {
    String shortPhone = "123";
    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(USER_UID);
    when(agridataSecurityIdentity.getUserId()).thenReturn(USER_ID);
    when(agridataSecurityIdentity.getPhoneNumberOrElseThrow()).thenReturn(shortPhone);
    when(contractRevisionRepository.findByIdAndDataConsumerUid(any(), any()))
        .thenReturn(Optional.of(ContractRevisionEntity.builder().build()));

    OtpChallengeEntity challengeEntity = OtpChallengeEntity.builder()
        .phoneNumberSnapshot(shortPhone)
        .build();

    when(otpChallengeService.createChallenge(any(), any(), any(), any())).thenReturn(challengeEntity);
    when(otpChallengeService.getResendCooldown()).thenReturn(Duration.ZERO);

    OtpChallengeDto result = contractRevisionOtpChallengeService.createOtpChallenge(REVISION_ID, SignatureSlotCodeEnum.DATA_CONSUMER_01);

    assertThat(result.maskedPhoneNumber()).isEqualTo("****");
  }
}
