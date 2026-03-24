package ch.agridata.agreement.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.agridata.agreement.dto.ContractRevisionDto;
import ch.agridata.agreement.dto.SignatureSlotCodeEnum;
import ch.agridata.agreement.mapper.ContractRevisionMapper;
import ch.agridata.agreement.persistence.ContractRevisionEntity;
import ch.agridata.agreement.persistence.ContractRevisionRepository;
import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.common.security.AgridataSecurityIdentity;
import io.quarkus.oidc.UserInfo;
import jakarta.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContractRevisionSignatureServiceTest {
  @InjectMocks
  private ContractRevisionSignatureService signatureService;

  @Mock
  private ContractRevisionRepository contractRevisionRepository;
  @Mock
  private ContractRevisionMapper contractRevisionMapper;
  @Mock
  private OtpChallengeService otpChallengeService;
  @Mock
  private AgridataSecurityIdentity agridataSecurityIdentity;

  private static final String CONSUMER_UID = "CHE123456789";
  private static final UUID USER_ID = UUID.randomUUID();
  private static final UUID REVISION_ID = UUID.randomUUID();
  private static final UUID VERIFICATION_ID = UUID.randomUUID();
  private static final String OTP_CODE = "123456";

  private ContractRevisionEntity existingRevision;
  private DataRequestEntity dataRequest;

  @BeforeEach
  void setUp() {
    dataRequest = new DataRequestEntity();
    dataRequest.setCurrentContractRevisionId(REVISION_ID);

    existingRevision = ContractRevisionEntity.builder()
        .id(REVISION_ID)
        .dataRequest(dataRequest)
        .build();
  }

  @Test
  void givenValidInput_whenSignContractRevision_thenPersistNewRevisionAndReturnDto() {
    SignatureSlotCodeEnum signatureSlotCode = SignatureSlotCodeEnum.DATA_CONSUMER_01;
    ContractRevisionEntity nextRevision = new ContractRevisionEntity();
    nextRevision.setId(UUID.randomUUID());

    setupSecurityContext("John", "Doe");
    when(contractRevisionRepository.findByIdAndDataConsumerUid(REVISION_ID, CONSUMER_UID))
        .thenReturn(Optional.of(existingRevision));
    when(contractRevisionMapper.toNextRevisionEntity(existingRevision)).thenReturn(nextRevision);
    when(contractRevisionMapper.toDto(nextRevision)).thenReturn(ContractRevisionDto.builder().build());

    ContractRevisionDto result = signatureService.signContractRevision(REVISION_ID, signatureSlotCode, VERIFICATION_ID, OTP_CODE);

    verify(otpChallengeService).verifyAndConsume(VERIFICATION_ID, USER_ID, REVISION_ID, signatureSlotCode, OTP_CODE);
    verify(contractRevisionRepository).persist(nextRevision);

    assertThat(dataRequest.getCurrentContractRevisionId()).isEqualTo(nextRevision.getId());
    assertThat(nextRevision.getConsumerSignatureUserId1()).isEqualTo(USER_ID);
    assertThat(nextRevision.getConsumerSignatureName1()).isEqualTo("John Doe");
    assertThat(result).isNotNull();
  }

  @Test
  void givenRevisionNotCurrent_whenSignContractRevision_thenThrowValidationException() {
    dataRequest.setCurrentContractRevisionId(UUID.randomUUID());
    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(CONSUMER_UID);
    when(contractRevisionRepository.findByIdAndDataConsumerUid(REVISION_ID, CONSUMER_UID))
        .thenReturn(Optional.of(existingRevision));

    assertThatThrownBy(
        () -> signatureService.signContractRevision(REVISION_ID, SignatureSlotCodeEnum.DATA_CONSUMER_01, VERIFICATION_ID, OTP_CODE))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("no longer current");
  }

  @Test
  void givenUserAlreadySigned_whenSignContractRevision_thenThrowValidationException() {
    existingRevision.setConsumerSignatureUserId1(USER_ID);
    setupSecurityContext("John", "Doe");
    when(contractRevisionRepository.findByIdAndDataConsumerUid(REVISION_ID, CONSUMER_UID))
        .thenReturn(Optional.of(existingRevision));

    assertThatThrownBy(
        () -> signatureService.signContractRevision(REVISION_ID, SignatureSlotCodeEnum.DATA_CONSUMER_02, VERIFICATION_ID, OTP_CODE))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("already signed");
  }

  @Test
  void givenSlotAlreadyOccupied_whenSignContractRevision_thenThrowValidationException() {
    existingRevision.setConsumerSignatureTimestamp1(LocalDateTime.now());
    setupSecurityContext("John", "Doe");
    when(contractRevisionRepository.findByIdAndDataConsumerUid(REVISION_ID, CONSUMER_UID))
        .thenReturn(Optional.of(existingRevision));

    assertThatThrownBy(
        () -> signatureService.signContractRevision(REVISION_ID, SignatureSlotCodeEnum.DATA_CONSUMER_01, VERIFICATION_ID, OTP_CODE))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("Signature already exists for this slot");
  }

  @Test
  void givenInvalidSlotId_whenSignContractRevision_thenThrowValidationException() {
    setupSecurityContext("John", "Doe");
    when(contractRevisionRepository.findByIdAndDataConsumerUid(REVISION_ID, CONSUMER_UID))
        .thenReturn(Optional.of(existingRevision));

    assertThatThrownBy(
        () -> signatureService.signContractRevision(REVISION_ID, SignatureSlotCodeEnum.DATA_PROVIDER_01, VERIFICATION_ID, OTP_CODE))
        .isInstanceOf(ValidationException.class)
        .hasMessage("Invalid signature slot id");
  }

  private void setupSecurityContext(String firstName, String familyName) {
    UserInfo userInfo = org.mockito.Mockito.mock(UserInfo.class);
    lenient().when(userInfo.getFirstName()).thenReturn(firstName);
    lenient().when(userInfo.getFamilyName()).thenReturn(familyName);

    lenient().when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(CONSUMER_UID);
    lenient().when(agridataSecurityIdentity.getUserId()).thenReturn(USER_ID);
    lenient().when(agridataSecurityIdentity.getUserInfoOrElseThrow()).thenReturn(userInfo);
  }
}
