package ch.agridata.agreement.service;

import static ch.agridata.agreement.utils.DataRequestTestUtils.USER_UID;
import static ch.agridata.agreement.utils.DataRequestTestUtils.buildEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
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
  private Validator validator;
  @Captor
  private ArgumentCaptor<DataRequestEntity> dataRequestEntityCaptor;

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
  void givenDraftRequest_whenSetStateAsToInReview_thenReturnDto() {
    var id = UUID.randomUUID();

    var entity = buildEntity();

    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(USER_UID);
    when(repository.findByIdAndDataConsumerUid(id, USER_UID)).thenReturn(Optional.of(entity));

    var result = dataRequestStateService.setStateAsConsumer(id, DataRequestStateEnum.IN_REVIEW);

    DataRequestDto expectedDto = verify(mapper).toDto(dataRequestEntityCaptor.capture());
    assertThat(dataRequestEntityCaptor.getValue().getStateCode()).isEqualTo(DataRequestEntity.DataRequestStateEnum.IN_REVIEW);
    assertThat(result).isEqualTo(expectedDto);
  }

  @Test
  void givenInReviewRequest_whenSetStateAsAdminToDraft_thenReturnDto() {
    var id = UUID.randomUUID();

    var entity = buildEntity();
    entity.setStateCode(DataRequestEntity.DataRequestStateEnum.IN_REVIEW);
    when(repository.findByIdOptional(id)).thenReturn(Optional.of(entity));

    var result = dataRequestStateService.setStateAsAdmin(id, DataRequestStateEnum.DRAFT);

    DataRequestDto expectedDto = verify(mapper).toDto(dataRequestEntityCaptor.capture());
    assertThat(dataRequestEntityCaptor.getValue().getStateCode()).isEqualTo(DataRequestEntity.DataRequestStateEnum.DRAFT);
    assertThat(result).isEqualTo(expectedDto);
  }

  @Test
  void givenInReviewRequest_whenSetStateAsAdminToBeSigned_thenReturnDto() {
    var id = UUID.randomUUID();

    var entity = buildEntity();
    entity.setStateCode(DataRequestEntity.DataRequestStateEnum.IN_REVIEW);
    when(repository.findByIdOptional(id)).thenReturn(Optional.of(entity));

    var result = dataRequestStateService.setStateAsAdmin(id, DataRequestStateEnum.TO_BE_SIGNED);

    DataRequestDto expectedDto = verify(mapper).toDto(dataRequestEntityCaptor.capture());
    assertThat(dataRequestEntityCaptor.getValue().getStateCode()).isEqualTo(DataRequestEntity.DataRequestStateEnum.TO_BE_SIGNED);
    assertThat(result).isEqualTo(expectedDto);
  }

  @Test
  void givenInDraftRequest_whenSetStateAsAdminToBeSigned_thenThrowError() {
    var id = UUID.randomUUID();

    var entity = buildEntity();
    entity.setStateCode(DataRequestEntity.DataRequestStateEnum.DRAFT);
    when(repository.findByIdOptional(id)).thenReturn(Optional.of(entity));

    assertThrows(IllegalStateException.class, () -> dataRequestStateService.setStateAsAdmin(id, DataRequestStateEnum.TO_BE_SIGNED));
  }
}
