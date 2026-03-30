package ch.agridata.agreement.service;

import static ch.agridata.agreement.utils.DataRequestTestUtils.USER_UID;
import static ch.agridata.agreement.utils.DataRequestTestUtils.buildEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ch.agridata.agreement.dto.DataRequestDto;
import ch.agridata.agreement.dto.DataRequestStateEnum;
import ch.agridata.agreement.mapper.DataRequestMapper;
import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.agreement.persistence.DataRequestRepository;
import ch.agridata.common.security.AgridataSecurityIdentity;
import jakarta.validation.Validator;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DataRequestStateServiceTest {

  @InjectMocks
  private DataRequestStateService dataRequestStateService;
  @Mock
  private DataRequestMapper mapper;
  @Mock
  private DataRequestRepository repository;
  @Mock
  private AgridataSecurityIdentity agridataSecurityIdentity;
  @Mock
  private AuditingService auditingService;
  @Mock
  private Validator validator;
  @Captor
  private ArgumentCaptor<DataRequestEntity> dataRequestEntityCaptor;
  @Mock
  private DataRequestEnrichmentService dataRequestEnrichmentService;
  @Mock
  private ContractRevisionInitializationService contractRevisionInitializationService;

  @Test
  void givenSubmittedRequest_whenSetStateToInReview_thenThrowIllegalStateAsAdminException() {
    var id = UUID.randomUUID();
    var entity = DataRequestEntity.builder()
        .stateCode(DataRequestEntity.DataRequestStateEnum.IN_REVIEW)
        .build();

    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(USER_UID);
    when(repository.findByIdAndDataConsumerUid(id, USER_UID)).thenReturn(Optional.of(entity));

    assertThrows(IllegalStateException.class,
        () -> dataRequestStateService.setStateAsConsumer(id, DataRequestStateEnum.IN_REVIEW));
  }

  @Test
  void givenDraftRequest_whenSetStateAsToInReview_thenReturnDtoAndLogSubmission() {
    var id = UUID.randomUUID();

    var entity = buildEntity();

    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(USER_UID);
    when(repository.findByIdAndDataConsumerUid(id, USER_UID)).thenReturn(Optional.of(entity));

    var result = dataRequestStateService.setStateAsConsumer(id, DataRequestStateEnum.IN_REVIEW);

    DataRequestDto expectedDto = verify(dataRequestEnrichmentService).toEnrichedDto(dataRequestEntityCaptor.capture());
    assertThat(dataRequestEntityCaptor.getValue().getStateCode()).isEqualTo(DataRequestEntity.DataRequestStateEnum.IN_REVIEW);
    verify(auditingService).logDataRequestSubmitted(id);
    assertThat(result).isEqualTo(expectedDto);
  }

  @Test
  void givenInReviewRequest_whenSetStateAsAdminToDraft_thenReturnDtoAndLogRejection() {
    var id = UUID.randomUUID();

    var entity = buildEntity();
    entity.setStateCode(DataRequestEntity.DataRequestStateEnum.IN_REVIEW);
    when(repository.findByIdOptional(id)).thenReturn(Optional.of(entity));

    var result = dataRequestStateService.setStateAsAdmin(id, DataRequestStateEnum.DRAFT);

    DataRequestDto expectedDto = verify(dataRequestEnrichmentService).toEnrichedDto(dataRequestEntityCaptor.capture());
    assertThat(dataRequestEntityCaptor.getValue().getStateCode()).isEqualTo(DataRequestEntity.DataRequestStateEnum.DRAFT);
    verify(auditingService).logDataRequestRejected(id);
    assertThat(result).isEqualTo(expectedDto);
  }

  @Test
  void givenInReviewRequest_whenSetStateAsConsumerToDraft_thenReturnDtoAndLogWithdrawal() {
    var id = UUID.randomUUID();

    var entity = buildEntity();
    entity.setStateCode(DataRequestEntity.DataRequestStateEnum.IN_REVIEW);

    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(USER_UID);
    when(repository.findByIdAndDataConsumerUid(id, USER_UID)).thenReturn(Optional.of(entity));

    var result = dataRequestStateService.setStateAsConsumer(id, DataRequestStateEnum.DRAFT);

    DataRequestDto expectedDto = verify(dataRequestEnrichmentService).toEnrichedDto(dataRequestEntityCaptor.capture());
    assertThat(dataRequestEntityCaptor.getValue().getStateCode()).isEqualTo(DataRequestEntity.DataRequestStateEnum.DRAFT);
    verify(auditingService).logDataRequestWithdrawn(id);
    assertThat(result).isEqualTo(expectedDto);
  }

  @Test
  void givenInReviewRequest_whenSetStateAsAdminToBeSigned_thenReturnDtoAndLogApproved() {
    var id = UUID.randomUUID();

    var entity = buildEntity();
    entity.setStateCode(DataRequestEntity.DataRequestStateEnum.IN_REVIEW);
    when(repository.findByIdOptional(id)).thenReturn(Optional.of(entity));

    var result = dataRequestStateService.setStateAsAdmin(id, DataRequestStateEnum.TO_BE_SIGNED_BY_CONSUMER);

    DataRequestDto expectedDto = verify(dataRequestEnrichmentService).toEnrichedDto(dataRequestEntityCaptor.capture());
    assertThat(dataRequestEntityCaptor.getValue().getStateCode()).isEqualTo(
        DataRequestEntity.DataRequestStateEnum.TO_BE_SIGNED_BY_CONSUMER);
    verify(auditingService).logDataRequestApproved(id);
    verify(contractRevisionInitializationService).createAndAssignInitialRevision(entity);
    assertThat(result).isEqualTo(expectedDto);
  }

  @Test
  void givenToBeReleasedByConsumerRequest_whenSetStateAsConsumerToToBeSignedByProvider_thenReturnDto() {
    var id = UUID.randomUUID();

    var entity = buildEntity();
    entity.setStateCode(DataRequestEntity.DataRequestStateEnum.TO_BE_RELEASED_BY_CONSUMER);

    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(USER_UID);
    when(repository.findByIdAndDataConsumerUid(id, USER_UID)).thenReturn(Optional.of(entity));

    var result = dataRequestStateService.setStateAsConsumer(id, DataRequestStateEnum.TO_BE_SIGNED_BY_PROVIDER);

    DataRequestDto expectedDto = verify(dataRequestEnrichmentService).toEnrichedDto(dataRequestEntityCaptor.capture());
    assertThat(dataRequestEntityCaptor.getValue().getStateCode()).isEqualTo(
        DataRequestEntity.DataRequestStateEnum.TO_BE_SIGNED_BY_PROVIDER);
    assertThat(result).isEqualTo(expectedDto);
  }

  @Test
  void givenToBeReleasedByConsumerRequest_whenSetStateAsAdminToToBeSignedByProvider_thenThrowError() {
    var id = UUID.randomUUID();

    var entity = buildEntity();
    entity.setStateCode(DataRequestEntity.DataRequestStateEnum.TO_BE_RELEASED_BY_CONSUMER);

    when(repository.findByIdOptional(id)).thenReturn(Optional.of(entity));

    assertThatThrownBy(() -> dataRequestStateService.setStateAsAdmin(id, DataRequestStateEnum.TO_BE_SIGNED_BY_PROVIDER))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("TO_BE_SIGNED_BY_PROVIDER");

    verifyNoInteractions(auditingService);
  }

  @Test
  void givenToBeSignedByProviderRequest_whenSetStateAsConsumerToActive_thenThrowError() {
    var id = UUID.randomUUID();
    var entity = buildEntity();
    entity.setStateCode(DataRequestEntity.DataRequestStateEnum.TO_BE_SIGNED_BY_PROVIDER);

    assertThatThrownBy(() -> dataRequestStateService.setStateAsConsumer(id, DataRequestStateEnum.ACTIVE))
        .isInstanceOf(IllegalStateException.class);

    verifyNoInteractions(auditingService);
  }

  @Test
  void givenToBeSignedByProviderRequest_whenSetStateAsAdminToActive_thenReturnDtoAndLogActivation() {
    var id = UUID.randomUUID();

    var entity = buildEntity();
    entity.setStateCode(DataRequestEntity.DataRequestStateEnum.TO_BE_SIGNED_BY_PROVIDER);
    when(repository.findByIdOptional(id)).thenReturn(Optional.of(entity));

    var result = dataRequestStateService.setStateAsAdmin(id, DataRequestStateEnum.ACTIVE);

    DataRequestDto expectedDto = verify(dataRequestEnrichmentService).toEnrichedDto(dataRequestEntityCaptor.capture());
    assertThat(dataRequestEntityCaptor.getValue().getStateCode()).isEqualTo(DataRequestEntity.DataRequestStateEnum.ACTIVE);
    verify(auditingService).logDataRequestActivated(id);
    assertThat(result).isEqualTo(expectedDto);
  }

  @Test
  void givenInDraftRequest_whenSetStateAsAdminToBeSigned_thenThrowError() {
    var id = UUID.randomUUID();

    var entity = buildEntity();
    entity.setStateCode(DataRequestEntity.DataRequestStateEnum.DRAFT);
    when(repository.findByIdOptional(id)).thenReturn(Optional.of(entity));

    assertThrows(IllegalStateException.class,
        () -> dataRequestStateService.setStateAsAdmin(id, DataRequestStateEnum.TO_BE_SIGNED_BY_CONSUMER));
    verifyNoInteractions(auditingService);
  }

  @Test
  void givenInDraftRequest_whenSetStateAsConsumerToInReview_andPersistenceFails_thenThrowErrorAndNoAuditing() {
    var id = UUID.randomUUID();

    var entity = buildEntity();
    entity.setStateCode(DataRequestEntity.DataRequestStateEnum.DRAFT);

    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(USER_UID);
    when(repository.findByIdAndDataConsumerUid(id, USER_UID)).thenReturn(Optional.of(entity));

    doThrow(new RuntimeException("Database down")).when(repository).persist(entity);

    assertThrows(RuntimeException.class, () -> dataRequestStateService.setStateAsConsumer(id, DataRequestStateEnum.IN_REVIEW));

    verifyNoInteractions(auditingService);
  }
}
