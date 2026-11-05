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
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
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
  private DataRequestStateEventDispatcher dataRequestStateEventDispatcher;
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
  @Mock
  private DataRequestQueryService dataRequestQueryService;
  @Mock
  private Clock clock;

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
    verify(dataRequestStateEventDispatcher).dispatchConsumerStatusTransition(
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
    verify(dataRequestStateEventDispatcher).dispatchAdminStatusTransition(
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
    verify(dataRequestStateEventDispatcher).dispatchConsumerStatusTransition(
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
    verify(dataRequestStateEventDispatcher).dispatchConsumerStatusTransition(
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
    verify(dataRequestStateEventDispatcher).dispatchAdminStatusTransition(
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

    verifyNoInteractions(dataRequestStateEventDispatcher);
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

    verifyNoInteractions(dataRequestStateEventDispatcher);
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
    verify(dataRequestStateEventDispatcher).dispatchAdminStatusTransition(
        entity,
        DataRequestEntity.DataRequestStateEnum.TO_BE_ACTIVATED,
        DataRequestEntity.DataRequestStateEnum.ACTIVE
    );
    assertThat(result).isEqualTo(expectedDto);
  }

  @Test
  void givenActiveRequest_whenSetStateAsAdminToPaused_thenReturnDtoAndDispatch() {
    var id = UUID.randomUUID();

    var entity = buildEntity();
    entity.setStateCode(DataRequestEntity.DataRequestStateEnum.ACTIVE);
    when(repository.findByIdOptional(id)).thenReturn(Optional.of(entity));
    var expectedDto = DataRequestDto.builder().build();
    when(dataRequestEnrichmentService.toEnrichedDto(entity)).thenReturn(expectedDto);

    var result = dataRequestStateService.setStateAsAdmin(id, DataRequestStateEnum.PAUSED);

    assertThat(result).isSameAs(expectedDto);
    assertThat(entity.getStateCode()).isEqualTo(DataRequestEntity.DataRequestStateEnum.PAUSED);
    verify(dataRequestStateEventDispatcher).dispatchAdminStatusTransition(
        entity,
        DataRequestEntity.DataRequestStateEnum.ACTIVE,
        DataRequestEntity.DataRequestStateEnum.PAUSED
    );
  }

  @Test
  void givenToBeActivatedRequest_whenSetStateAsAdminToPaused_thenThrowError() {
    var id = UUID.randomUUID();
    var entity = buildEntity();
    entity.setStateCode(DataRequestEntity.DataRequestStateEnum.TO_BE_ACTIVATED);
    when(repository.findByIdOptional(id)).thenReturn(Optional.of(entity));

    assertThatThrownBy(() -> dataRequestStateService.setStateAsAdmin(id, DataRequestStateEnum.PAUSED))
        .isInstanceOf(IllegalStateException.class);

    verifyNoInteractions(dataRequestStateEventDispatcher);
  }

  @Test
  void givenActiveRequest_whenSetStateAsConsumerToPaused_thenThrowError() {
    var id = UUID.randomUUID();
    var entity = buildEntity();
    entity.setStateCode(DataRequestEntity.DataRequestStateEnum.ACTIVE);

    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(USER_UID);
    when(repository.findByIdAndDataConsumerUid(id, USER_UID)).thenReturn(Optional.of(entity));

    assertThatThrownBy(() -> dataRequestStateService.setStateAsConsumer(id, DataRequestStateEnum.PAUSED))
        .isInstanceOf(IllegalStateException.class);

    verifyNoInteractions(dataRequestStateEventDispatcher);
  }

  @Test
  void givenPausedRequest_whenSetStateAsAdminToActive_thenReturnDtoAndDispatch() {
    var id = UUID.randomUUID();

    var entity = buildEntity();
    entity.setStateCode(DataRequestEntity.DataRequestStateEnum.PAUSED);
    when(repository.findByIdOptional(id)).thenReturn(Optional.of(entity));
    var expectedDto = DataRequestDto.builder().build();
    when(dataRequestEnrichmentService.toEnrichedDto(entity)).thenReturn(expectedDto);

    var result = dataRequestStateService.setStateAsAdmin(id, DataRequestStateEnum.ACTIVE);

    assertThat(result).isSameAs(expectedDto);
    assertThat(entity.getStateCode()).isEqualTo(DataRequestEntity.DataRequestStateEnum.ACTIVE);
    verify(dataRequestStateEventDispatcher).dispatchAdminStatusTransition(
        entity,
        DataRequestEntity.DataRequestStateEnum.PAUSED,
        DataRequestEntity.DataRequestStateEnum.ACTIVE
    );
  }

  @Test
  void givenPausedRequest_whenSetStateAsConsumerToActive_thenThrowError() {
    var id = UUID.randomUUID();
    var entity = buildEntity();
    entity.setStateCode(DataRequestEntity.DataRequestStateEnum.PAUSED);

    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(USER_UID);
    when(repository.findByIdAndDataConsumerUid(id, USER_UID)).thenReturn(Optional.of(entity));

    assertThatThrownBy(() -> dataRequestStateService.setStateAsConsumer(id, DataRequestStateEnum.ACTIVE))
        .isInstanceOf(IllegalStateException.class);

    verifyNoInteractions(dataRequestStateEventDispatcher);
  }

  @Test
  void givenActiveRequest_whenSetStateAsConsumerToTerminationPending_thenSetLastDayOfNoticePeriodAndDispatch() {
    var id = UUID.randomUUID();
    var entity = buildEntity();
    entity.setStateCode(DataRequestEntity.DataRequestStateEnum.ACTIVE);
    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(USER_UID);
    when(repository.findByIdAndDataConsumerUid(id, USER_UID)).thenReturn(Optional.of(entity));
    var expectedDto = DataRequestDto.builder().build();
    when(dataRequestEnrichmentService.toEnrichedDto(entity)).thenReturn(expectedDto);
    fixClockAt(LocalDate.of(2026, 10, 13));

    var result = dataRequestStateService.setStateAsConsumer(id, DataRequestStateEnum.TERMINATION_PENDING);

    assertThat(result).isSameAs(expectedDto);
    assertThat(entity.getStateCode()).isEqualTo(DataRequestEntity.DataRequestStateEnum.TERMINATION_PENDING);
    assertThat(entity.getTerminationPeriodUntil()).isEqualTo(LocalDate.of(2027, 1, 13));
    verify(dataRequestStateEventDispatcher).dispatchConsumerStatusTransition(
        entity,
        DataRequestEntity.DataRequestStateEnum.ACTIVE,
        DataRequestEntity.DataRequestStateEnum.TERMINATION_PENDING
    );
  }

  @ParameterizedTest
  @CsvSource(
      {
          "2026-01-01, 2026-04-01",
          "2026-10-31, 2027-01-31",
          "2026-11-30, 2027-02-28",
          "2027-11-30, 2028-02-29",
          "2026-12-31, 2027-03-31",
          "2026-05-31, 2026-08-31"
      }
  )
  void givenActiveRequest_whenSetStateAsProviderToTerminationPending_thenClampLastValidDayToMonthEnd(
      LocalDate today, LocalDate expectedTerminationPeriodUntil
  ) {
    var id = UUID.randomUUID();
    var entity = buildEntity();
    entity.setStateCode(DataRequestEntity.DataRequestStateEnum.ACTIVE);
    when(repository.findByIdOptional(id)).thenReturn(Optional.of(entity));
    when(dataRequestQueryService.isAssignedToCurrentProvider(entity)).thenReturn(true);
    fixClockAt(today);

    dataRequestStateService.setStateAsProvider(id, DataRequestStateEnum.TERMINATION_PENDING);

    assertThat(entity.getTerminationPeriodUntil()).isEqualTo(expectedTerminationPeriodUntil);
    verify(dataRequestStateEventDispatcher).dispatchProviderStatusTransition(
        entity,
        DataRequestEntity.DataRequestStateEnum.ACTIVE,
        DataRequestEntity.DataRequestStateEnum.TERMINATION_PENDING
    );
  }

  @Test
  void givenActiveRequest_whenSetStateAsAdminToTerminationPending_thenThrowError() {
    var id = UUID.randomUUID();
    var entity = buildEntity();
    entity.setStateCode(DataRequestEntity.DataRequestStateEnum.ACTIVE);
    when(repository.findByIdOptional(id)).thenReturn(Optional.of(entity));

    assertThatThrownBy(() -> dataRequestStateService.setStateAsAdmin(id, DataRequestStateEnum.TERMINATION_PENDING))
        .isInstanceOf(IllegalStateException.class);

    verifyNoInteractions(dataRequestStateEventDispatcher);
  }

  @Test
  void givenPausedRequest_whenSetStateAsConsumerToTerminationPending_thenThrowError() {
    var id = UUID.randomUUID();
    var entity = buildEntity();
    entity.setStateCode(DataRequestEntity.DataRequestStateEnum.PAUSED);
    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(USER_UID);
    when(repository.findByIdAndDataConsumerUid(id, USER_UID)).thenReturn(Optional.of(entity));

    assertThatThrownBy(() -> dataRequestStateService.setStateAsConsumer(id, DataRequestStateEnum.TERMINATION_PENDING))
        .isInstanceOf(IllegalStateException.class);

    verifyNoInteractions(dataRequestStateEventDispatcher);
  }

  @ParameterizedTest
  @EnumSource(value = DataRequestStateEnum.class, names = {"ACTIVE", "PAUSED", "TERMINATED"})
  void givenTerminationPendingRequest_whenSetStateAsConsumer_thenThrowErrorBecauseTerminationIsFinal(DataRequestStateEnum newState) {
    var id = UUID.randomUUID();
    var entity = buildEntity();
    entity.setStateCode(DataRequestEntity.DataRequestStateEnum.TERMINATION_PENDING);
    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(USER_UID);
    when(repository.findByIdAndDataConsumerUid(id, USER_UID)).thenReturn(Optional.of(entity));

    assertThatThrownBy(() -> dataRequestStateService.setStateAsConsumer(id, newState))
        .isInstanceOf(IllegalStateException.class);

    verifyNoInteractions(dataRequestStateEventDispatcher);
  }

  @Test
  void givenTerminationPendingRequest_whenSetStateAsAdminToActive_thenThrowErrorBecauseTerminationIsFinal() {
    var id = UUID.randomUUID();
    var entity = buildEntity();
    entity.setStateCode(DataRequestEntity.DataRequestStateEnum.TERMINATION_PENDING);
    when(repository.findByIdOptional(id)).thenReturn(Optional.of(entity));

    assertThatThrownBy(() -> dataRequestStateService.setStateAsAdmin(id, DataRequestStateEnum.ACTIVE))
        .isInstanceOf(IllegalStateException.class);

    verifyNoInteractions(dataRequestStateEventDispatcher);
  }

  private void fixClockAt(LocalDate date) {
    when(clock.instant()).thenReturn(date.atStartOfDay(ZoneOffset.UTC).toInstant());
    when(clock.getZone()).thenReturn(ZoneOffset.UTC);
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
    verifyNoInteractions(dataRequestStateEventDispatcher);
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

    verifyNoInteractions(dataRequestStateEventDispatcher);
  }
}
