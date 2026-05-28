package ch.agridata.agreement.service;

import static ch.agridata.agreement.utils.DataRequestTestUtils.CONTRACT_REVISION_ID;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ch.agridata.agreement.dto.ContractRevisionDto;
import ch.agridata.agreement.dto.SignatureSlotCodeEnum;
import ch.agridata.agreement.mapper.ContractRevisionMapper;
import ch.agridata.agreement.persistence.ContractRevisionEntity;
import ch.agridata.agreement.persistence.ContractRevisionRepository;
import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.agreement.persistence.SignatureTypeEnum;
import ch.agridata.agreement.utils.DataRequestTestUtils;
import ch.agridata.common.security.AgridataSecurityIdentity;
import io.quarkus.oidc.UserInfo;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.NotFoundException;
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
  @Mock
  private DataRequestStateService dataRequestStateService;
  @Mock
  private ContractRevisionQueryService contractRevisionQueryService;
  @Mock
  private ContractRevisionPdfService contractRevisionPdfService;
  @Mock
  private AuditingService auditingService;

  private static final String USER_UID = "CHE123456789";
  private static final UUID USER_ID = UUID.randomUUID();
  private static final UUID VERIFICATION_ID = UUID.randomUUID();
  private static final String OTP_CODE = "123456";

  private ContractRevisionEntity existingRevision;
  private DataRequestEntity dataRequest;

  @BeforeEach
  void setUp() {
    existingRevision = DataRequestTestUtils.buildContractRevision();
    dataRequest = existingRevision.getDataRequest();
  }

  @Test
  void givenValidInput_whenSignContractRevision_thenPersistNewRevisionAndReturnDto() {
    SignatureSlotCodeEnum signatureSlotCode = SignatureSlotCodeEnum.DATA_CONSUMER_01;
    ContractRevisionEntity nextRevision = new ContractRevisionEntity();
    nextRevision.setId(UUID.randomUUID());

    setupSecurityContext();
    when(contractRevisionRepository.findByIdAndDataConsumerUid(CONTRACT_REVISION_ID, USER_UID))
        .thenReturn(Optional.of(existingRevision));
    when(contractRevisionMapper.toNextRevisionEntity(existingRevision)).thenReturn(nextRevision);
    when(contractRevisionMapper.toDto(nextRevision)).thenReturn(ContractRevisionDto.builder().build());

    ContractRevisionDto result = signatureService.signContractRevisionAsConsumer(CONTRACT_REVISION_ID, signatureSlotCode, VERIFICATION_ID, OTP_CODE);

    verify(otpChallengeService).verifyAndConsume(VERIFICATION_ID, USER_ID, CONTRACT_REVISION_ID, signatureSlotCode, OTP_CODE);
    verify(contractRevisionRepository).persist(nextRevision);
    verify(auditingService)
        .logContractRevisionSigned(CONTRACT_REVISION_ID, signatureSlotCode);

    assertThat(dataRequest.getCurrentContractRevisionId()).isEqualTo(nextRevision.getId());
    assertThat(nextRevision.getConsumerSignatureUserId1()).isEqualTo(USER_ID);
    assertThat(nextRevision.getConsumerSignatureName1()).isEqualTo("John Doe");
    assertThat(nextRevision.getConsumerSignatureType()).isEqualTo(SignatureTypeEnum.COLLECTIVE_SIGNATURE);
    assertThat(result).isNotNull();

    verifyNoInteractions(dataRequestStateService);
  }

  @Test
  void givenRevisionWithSingleSignature_whenSignContractRevision_thenChangeDataRequestState() {
    UUID existingSignatureUUID = UUID.fromString("12345678-1234-1234-1234-123456789012");
    existingRevision.setConsumerSignatureUserId1(existingSignatureUUID);
    SignatureSlotCodeEnum signatureSlotCode = SignatureSlotCodeEnum.DATA_CONSUMER_02;
    ContractRevisionEntity nextRevision = ContractRevisionEntity.builder()
        .consumerSignatureUserId1(UUID.fromString(existingSignatureUUID.toString()))
        .consumerSignatureUserId2(UUID.fromString(USER_ID.toString()))
        .consumerSignatureType(SignatureTypeEnum.COLLECTIVE_SIGNATURE)
        .build();
    nextRevision.setId(UUID.randomUUID());

    setupSecurityContext();

    when(contractRevisionRepository.findByIdAndDataConsumerUid(CONTRACT_REVISION_ID, USER_UID))
        .thenReturn(Optional.of(existingRevision));
    when(contractRevisionMapper.toNextRevisionEntity(existingRevision)).thenReturn(nextRevision);
    when(contractRevisionMapper.toDto(nextRevision)).thenReturn(ContractRevisionDto.builder().build());

    ContractRevisionDto result = signatureService.signContractRevisionAsConsumer(CONTRACT_REVISION_ID, signatureSlotCode, VERIFICATION_ID, OTP_CODE);

    verify(auditingService)
        .logContractRevisionSigned(CONTRACT_REVISION_ID, signatureSlotCode);
    assertThat(dataRequest.getCurrentContractRevisionId()).isEqualTo(nextRevision.getId());
    assertThat(nextRevision.getConsumerSignatureUserId1()).isEqualTo(existingSignatureUUID);
    assertThat(nextRevision.getConsumerSignatureUserId2()).isEqualTo(USER_ID);
    assertThat(nextRevision.getConsumerSignatureName2()).isEqualTo("John Doe");
    assertThat(result).isNotNull();

    verify(dataRequestStateService).transitionToPendingReleaseByConsumer(dataRequest);
  }

  @Test
  void givenRevisionNotCurrent_whenSignContractRevision_thenThrowValidationException() {
    dataRequest.setCurrentContractRevisionId(UUID.randomUUID());
    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(USER_UID);
    when(contractRevisionRepository.findByIdAndDataConsumerUid(CONTRACT_REVISION_ID, USER_UID))
        .thenReturn(Optional.of(existingRevision));

    assertThatThrownBy(
        () -> signatureService.signContractRevisionAsConsumer(CONTRACT_REVISION_ID, SignatureSlotCodeEnum.DATA_CONSUMER_01, VERIFICATION_ID,
            OTP_CODE))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("no longer current");

    verifyNoInteractions(dataRequestStateService);
  }

  @Test
  void givenUserAlreadySigned_whenSignContractRevision_thenThrowValidationException() {
    existingRevision.setConsumerSignatureUserId1(USER_ID);
    setupSecurityContext();
    when(contractRevisionRepository.findByIdAndDataConsumerUid(CONTRACT_REVISION_ID, USER_UID))
        .thenReturn(Optional.of(existingRevision));

    assertThatThrownBy(
        () -> signatureService.signContractRevisionAsConsumer(CONTRACT_REVISION_ID, SignatureSlotCodeEnum.DATA_CONSUMER_02, VERIFICATION_ID,
            OTP_CODE))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("already signed");

    verifyNoInteractions(dataRequestStateService);
  }

  @Test
  void givenWrongSigningOrder_whenSignContractRevisionAsConsumer_thenThrowValidationException() {
    setupSecurityContext();
    when(contractRevisionRepository.findByIdAndDataConsumerUid(CONTRACT_REVISION_ID, USER_UID))
        .thenReturn(Optional.of(existingRevision));

    assertThatThrownBy(
        () -> signatureService.signContractRevisionAsConsumer(CONTRACT_REVISION_ID, SignatureSlotCodeEnum.DATA_CONSUMER_02, VERIFICATION_ID,
            OTP_CODE))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("Must sign first signature slot before signing the second one");

    verifyNoInteractions(dataRequestStateService);
  }

  @Test
  void givenWrongSigningOrder_whenSignContractRevisionAsProvider_thenThrowValidationException() {
    setupSecurityContext();
    when(contractRevisionRepository.findByIdOptional(CONTRACT_REVISION_ID))
        .thenReturn(Optional.of(existingRevision));
    when(contractRevisionQueryService.isAssignedToCurrentProvider(existingRevision)).thenReturn(true);

    assertThatThrownBy(
        () -> signatureService.signContractRevisionAsProvider(CONTRACT_REVISION_ID, SignatureSlotCodeEnum.DATA_PROVIDER_02, VERIFICATION_ID,
            OTP_CODE))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("Must sign first signature slot before signing the second one");

    verifyNoInteractions(dataRequestStateService);
  }

  @Test
  void givenSlotAlreadyOccupied_whenSignContractRevision_thenThrowValidationException() {
    existingRevision.setConsumerSignatureTimestamp1(LocalDateTime.now());
    setupSecurityContext();
    when(contractRevisionRepository.findByIdAndDataConsumerUid(CONTRACT_REVISION_ID, USER_UID))
        .thenReturn(Optional.of(existingRevision));

    assertThatThrownBy(
        () -> signatureService.signContractRevisionAsConsumer(CONTRACT_REVISION_ID, SignatureSlotCodeEnum.DATA_CONSUMER_01, VERIFICATION_ID,
            OTP_CODE))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("Signature already exists for this slot");

    verifyNoInteractions(dataRequestStateService);
  }

  @Test
  void givenInvalidSlotId_whenSignContractRevisionByConsumer_thenThrowValidationException() {
    setupSecurityContext();
    when(contractRevisionRepository.findByIdAndDataConsumerUid(CONTRACT_REVISION_ID, USER_UID))
        .thenReturn(Optional.of(existingRevision));

    assertThatThrownBy(
        () -> signatureService.signContractRevisionAsConsumer(CONTRACT_REVISION_ID, SignatureSlotCodeEnum.DATA_PROVIDER_01, VERIFICATION_ID,
            OTP_CODE))
        .isInstanceOf(ValidationException.class)
        .hasMessage("Invalid consumer signature slot id");

    verifyNoInteractions(dataRequestStateService);
  }

  @Test
  void givenInvalidSlotId_whenSignContractRevisionByProvider_thenThrowValidationException() {
    setupSecurityContext();

    when(contractRevisionRepository.findByIdOptional(CONTRACT_REVISION_ID))
        .thenReturn(Optional.of(existingRevision));
    when(contractRevisionQueryService.isAssignedToCurrentProvider(existingRevision))
        .thenReturn(true);

    assertThatThrownBy(
        () -> signatureService.signContractRevisionAsProvider(CONTRACT_REVISION_ID, SignatureSlotCodeEnum.DATA_CONSUMER_01, VERIFICATION_ID,
            OTP_CODE))
        .isInstanceOf(ValidationException.class)
        .hasMessage("Invalid provider signature slot id");

    verifyNoInteractions(dataRequestStateService);
  }

  @Test
  void givenRevisionNotAssignedToCurrentProvider_whenSignContractRevision_thenThrowNotFoundException() {
    setupSecurityContext();

    when(contractRevisionRepository.findByIdOptional(CONTRACT_REVISION_ID))
        .thenReturn(Optional.of(existingRevision));
    when(contractRevisionQueryService.isAssignedToCurrentProvider(existingRevision))
        .thenReturn(false);

    assertThatThrownBy(
        () -> signatureService.signContractRevisionAsProvider(CONTRACT_REVISION_ID,
            SignatureSlotCodeEnum.DATA_PROVIDER_01, VERIFICATION_ID,
            OTP_CODE))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining(CONTRACT_REVISION_ID.toString());

    verifyNoInteractions(dataRequestStateService);
  }

  private void setupSecurityContext() {
    UserInfo userInfo = org.mockito.Mockito.mock(UserInfo.class);
    lenient().when(userInfo.getString("given_name")).thenReturn("John");
    lenient().when(userInfo.getString("family_name")).thenReturn("Doe");

    lenient().when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(USER_UID);
    lenient().when(agridataSecurityIdentity.getUserId()).thenReturn(USER_ID);
    lenient().when(agridataSecurityIdentity.getUserInfoOrElseThrow()).thenReturn(userInfo);
  }
}
