package ch.agridata.agreement.service;

import static ch.agridata.agreement.utils.DataRequestTestUtils.USER_UID;
import static ch.agridata.agreement.utils.DataRequestTestUtils.buildEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ch.agridata.agreement.dto.DataRequestDto;
import ch.agridata.agreement.dto.DataRequestStateEnum;
import ch.agridata.agreement.mapper.DataRequestMapper;
import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.agreement.persistence.DataRequestRepository;
import ch.agridata.agreement.persistence.SignatureTypeEnum;
import ch.agridata.agreement.utils.DataRequestTestUtils;
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
  private DataRequestStateAuditService dataRequestStateAuditService;
  @Mock
  private Validator validator;
  @Captor
  private ArgumentCaptor<DataRequestEntity> dataRequestEntityCaptor;
  @Mock
  private DataRequestEnrichmentService dataRequestEnrichmentService;
  @Mock
  private ContractRevisionInitializationService contractRevisionInitializationService;
  @Mock
  private ContractRevisionMutationService contractRevisionMutationService;

  @Test
  void givenSubmittedRequest_whenSetStateToInReview_thenThrowIllegalStateAsAdminException() {
    var id = UUID.randomUUID();
    var entity = DataRequestEntity.builder().stateCode(DataRequestEntity.DataRequestStateEnum.IN_REVIEW).build();

    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(USER_UID);
    when(repository.findByIdAndDataConsumerUid(id, USER_UID)).thenReturn(Optional.of(entity));

    assertThrows(IllegalStateException.class, () -> dataRequestStateService.setStateAsConsumer(id, DataRequestStateEnum.IN_REVIEW));
  }

  @Test
  void givenDraftRequest_whenSetStateAsToInReview_thenReturnDtoAndLogSubmission() {
    var id = UUID.randomUUID();

    var entity = buildEntity();
    var expectedDto = DataRequestTestUtils.dataRequestDtoBuilder()
        .stateCode(DataRequestStateEnum.IN_REVIEW)
        .build();

    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(USER_UID);
    when(repository.findByIdAndDataConsumerUid(id, USER_UID)).thenReturn(Optional.of(entity));
    when(dataRequestEnrichmentService.toEnrichedDto(any(DataRequestEntity.class)))
        .thenReturn(expectedDto);

    var result = dataRequestStateService.setStateAsConsumer(id, DataRequestStateEnum.IN_REVIEW);

    verify(dataRequestEnrichmentService).toEnrichedDto(dataRequestEntityCaptor.capture());
    assertThat(dataRequestEntityCaptor.getValue().getStateCode()).isEqualTo(DataRequestEntity.DataRequestStateEnum.IN_REVIEW);
    verify(dataRequestStateAuditService).auditConsumerStatusTransition(
        entity,
        DataRequestEntity.DataRequestStateEnum.DRAFT,
        DataRequestEntity.DataRequestStateEnum.IN_REVIEW
    );
    assertThat(result).isEqualTo(expectedDto);
  }

  @Test
  void givenInReviewRequest_whenSetStateAsAdminToDraft_thenReturnDtoAndLogRejection() {
    var id = UUID.randomUUID();

    var entity = buildEntity();
    var expectedDto = DataRequestTestUtils.dataRequestDtoBuilder()
        .stateCode(DataRequestStateEnum.DRAFT)
        .build();

    entity.setStateCode(DataRequestEntity.DataRequestStateEnum.IN_REVIEW);
    when(repository.findByIdOptional(id)).thenReturn(Optional.of(entity));
    when(dataRequestEnrichmentService.toEnrichedDto(any(DataRequestEntity.class)))
        .thenReturn(expectedDto);

    var result = dataRequestStateService.setStateAsAdmin(id, DataRequestStateEnum.DRAFT);

    verify(dataRequestEnrichmentService).toEnrichedDto(dataRequestEntityCaptor.capture());
    assertThat(dataRequestEntityCaptor.getValue().getStateCode()).isEqualTo(DataRequestEntity.DataRequestStateEnum.DRAFT);
    verify(dataRequestStateAuditService).auditAdminStatusTransition(
        entity,
        DataRequestEntity.DataRequestStateEnum.IN_REVIEW,
        DataRequestEntity.DataRequestStateEnum.DRAFT
    );
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
    verify(dataRequestStateAuditService).auditConsumerStatusTransition(
        entity,
        DataRequestEntity.DataRequestStateEnum.IN_REVIEW,
        DataRequestEntity.DataRequestStateEnum.DRAFT
    );
    verify(contractRevisionMutationService).archiveAllForDataRequest(entity.getId());
    assertThat(dataRequestEntityCaptor.getValue().getConsumerSignatureType()).isEqualTo(SignatureTypeEnum.COLLECTIVE_SIGNATURE);
    assertThat(dataRequestEntityCaptor.getValue().getProviderSignatureType()).isEqualTo(SignatureTypeEnum.COLLECTIVE_SIGNATURE);
    assertThat(result).isEqualTo(expectedDto);
  }

  @Test
  void givenToBeActivatedRequest_whenSetStateAsConsumerToDraft_thenReturnDtoAndLogWithdrawal() {
    var id = UUID.randomUUID();

    var entity = buildEntity();
    entity.setStateCode(DataRequestEntity.DataRequestStateEnum.TO_BE_ACTIVATED);

    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(USER_UID);
    when(repository.findByIdAndDataConsumerUid(id, USER_UID)).thenReturn(Optional.of(entity));

    var result = dataRequestStateService.setStateAsConsumer(id, DataRequestStateEnum.DRAFT);

    DataRequestDto expectedDto = verify(dataRequestEnrichmentService).toEnrichedDto(dataRequestEntityCaptor.capture());
    assertThat(dataRequestEntityCaptor.getValue().getStateCode()).isEqualTo(DataRequestEntity.DataRequestStateEnum.DRAFT);
    verify(contractRevisionMutationService).archiveAllForDataRequest(entity.getId());
    verify(dataRequestStateAuditService).auditConsumerStatusTransition(
        entity,
        DataRequestEntity.DataRequestStateEnum.TO_BE_ACTIVATED,
        DataRequestEntity.DataRequestStateEnum.DRAFT
    );
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
    assertThat(dataRequestEntityCaptor.getValue()
        .getStateCode()).isEqualTo(DataRequestEntity.DataRequestStateEnum.TO_BE_SIGNED_BY_CONSUMER);
    verify(dataRequestStateAuditService).auditAdminStatusTransition(
        entity,
        DataRequestEntity.DataRequestStateEnum.IN_REVIEW,
        DataRequestEntity.DataRequestStateEnum.TO_BE_SIGNED_BY_CONSUMER
    );
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
    assertThat(dataRequestEntityCaptor.getValue()
        .getStateCode()).isEqualTo(DataRequestEntity.DataRequestStateEnum.TO_BE_SIGNED_BY_PROVIDER);
    assertThat(result).isEqualTo(expectedDto);
  }

  @Test
  void givenToBeReleasedByConsumerRequest_whenSetStateAsAdminToToBeSignedByProvider_thenThrowError() {
    var id = UUID.randomUUID();

    var entity = buildEntity();
    entity.setStateCode(DataRequestEntity.DataRequestStateEnum.TO_BE_RELEASED_BY_CONSUMER);

    when(repository.findByIdOptional(id)).thenReturn(Optional.of(entity));

    assertThatThrownBy(() -> dataRequestStateService.setStateAsAdmin(id, DataRequestStateEnum.TO_BE_SIGNED_BY_PROVIDER)).isInstanceOf(
        IllegalStateException.class).hasMessageContaining("TO_BE_SIGNED_BY_PROVIDER");

    verifyNoInteractions(dataRequestStateAuditService);
  }

  @Test
  void givenToBeSignedByProviderRequest_whenSetStateAsConsumerToActive_thenThrowError() {
    var id = UUID.randomUUID();
    var entity = buildEntity();
    entity.setStateCode(DataRequestEntity.DataRequestStateEnum.TO_BE_SIGNED_BY_PROVIDER);

    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(USER_UID);
    when(repository.findByIdAndDataConsumerUid(id, USER_UID)).thenReturn(Optional.of(entity));

    assertThatThrownBy(() -> dataRequestStateService.setStateAsConsumer(
        id,
        DataRequestStateEnum.ACTIVE
    )).isInstanceOf(IllegalStateException.class);

    verifyNoInteractions(dataRequestStateAuditService);
  }

  @Test
  void givenToBeSignedByProviderRequest_whenSetStateAsAdminToActive_thenReturnDtoAndLogActivation() {
    var id = UUID.randomUUID();

    var entity = buildEntity();
    entity.setStateCode(DataRequestEntity.DataRequestStateEnum.TO_BE_ACTIVATED);
    when(repository.findByIdOptional(id)).thenReturn(Optional.of(entity));

    var result = dataRequestStateService.setStateAsAdmin(id, DataRequestStateEnum.ACTIVE);

    DataRequestDto expectedDto = verify(dataRequestEnrichmentService).toEnrichedDto(dataRequestEntityCaptor.capture());
    assertThat(dataRequestEntityCaptor.getValue().getStateCode()).isEqualTo(DataRequestEntity.DataRequestStateEnum.ACTIVE);
    verify(dataRequestStateAuditService).auditAdminStatusTransition(
        entity,
        DataRequestEntity.DataRequestStateEnum.TO_BE_ACTIVATED,
        DataRequestEntity.DataRequestStateEnum.ACTIVE
    );
    assertThat(result).isEqualTo(expectedDto);
  }

  @Test
  void givenInDraftRequest_whenSetStateAsAdminToBeSigned_thenThrowError() {
    var id = UUID.randomUUID();

    var entity = buildEntity();
    entity.setStateCode(DataRequestEntity.DataRequestStateEnum.DRAFT);
    when(repository.findByIdOptional(id)).thenReturn(Optional.of(entity));

    assertThrows(
        IllegalStateException.class,
        () -> dataRequestStateService.setStateAsAdmin(id, DataRequestStateEnum.TO_BE_SIGNED_BY_CONSUMER)
    );
    verifyNoInteractions(dataRequestStateAuditService);
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

    verifyNoInteractions(dataRequestStateAuditService);
  }
}
