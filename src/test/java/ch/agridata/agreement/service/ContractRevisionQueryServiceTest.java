package ch.agridata.agreement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ch.agridata.agreement.dto.ContractRevisionDto;
import ch.agridata.agreement.mapper.ContractRevisionMapper;
import ch.agridata.agreement.persistence.ContractRevisionEntity;
import ch.agridata.agreement.persistence.ContractRevisionRepository;
import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.common.security.AgridataSecurityIdentity;
import jakarta.ws.rs.NotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContractRevisionQueryServiceTest {
  @Mock
  private ContractRevisionRepository contractRevisionRepository;

  @Mock
  private ContractRevisionMapper contractRevisionMapper;

  @Mock
  private AgridataSecurityIdentity agridataSecurityIdentity;

  @Mock
  private DataRequestQueryService dataRequestQueryService;

  @InjectMocks
  ContractRevisionQueryService service;

  private static final UUID REVISION_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID DATA_REQUEST_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
  private static final String USER_UID = "CHE000000001";

  @Test
  void givenExistingContractRevision_whenGetDtoWithAccessCheckAsAdmin_thenReturnMappedDto() {
    ContractRevisionEntity entity = ContractRevisionEntity.builder()
        .id(REVISION_ID)
        .dataRequest(DataRequestEntity.builder().stateCode(DataRequestEntity.DataRequestStateEnum.ACTIVE).build())
        .build();
    ContractRevisionDto expectedDto = ContractRevisionDto.builder().id(REVISION_ID).build();

    when(contractRevisionRepository.findByIdOptional(REVISION_ID)).thenReturn(Optional.of(entity));
    when(contractRevisionMapper.toDto(entity)).thenReturn(expectedDto);

    ContractRevisionDto result = service.getDtoAsAdmin(REVISION_ID);

    assertThat(result).isSameAs(expectedDto);
    verify(contractRevisionMapper).toDto(entity);
  }

  @Test
  void givenMissingContractRevision_whenGetDtoWithAccessCheck_thenThrowNotFoundException() {
    when(contractRevisionRepository.findByIdOptional(REVISION_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getAsAdmin(REVISION_ID))
        .isInstanceOf(NotFoundException.class);

    verifyNoInteractions(contractRevisionMapper);
  }

  @Test
  void givenExistingContractRevision_whenGetContractRevisionAsAdmin_thenReturnEntity() {
    ContractRevisionEntity entity = ContractRevisionEntity.builder()
        .id(REVISION_ID)
        .dataRequest(DataRequestEntity.builder().stateCode(DataRequestEntity.DataRequestStateEnum.ACTIVE).build())
        .build();

    when(contractRevisionRepository.findByIdOptional(REVISION_ID)).thenReturn(Optional.ofNullable(entity));

    var result = service.getAsAdmin(REVISION_ID);
    assertThat(result.getId()).isEqualTo(REVISION_ID);
    verifyNoInteractions(dataRequestQueryService);
  }

  @Test
  void givenMissingContractRevision_whenGetContractRevisionAsAdmin_thenThrowNotFound() {
    when(contractRevisionRepository.findByIdOptional(REVISION_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getAsAdmin(REVISION_ID))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining(REVISION_ID.toString());
    verifyNoInteractions(dataRequestQueryService);
  }

  @Test
  void givenExistingContractRevision_whenGetContractRevisionAsConsumer_thenReturnEntity() {
    ContractRevisionEntity entity = ContractRevisionEntity.builder().id(REVISION_ID).build();
    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(USER_UID);
    when(contractRevisionRepository.findByIdAndDataConsumerUid(REVISION_ID, USER_UID)).thenReturn(Optional.of(entity));

    var result = service.getAsConsumer(REVISION_ID);

    assertThat(result.getId()).isEqualTo(REVISION_ID);
    verifyNoInteractions(dataRequestQueryService);
  }

  @Test
  void givenMissingContractRevision_whenGetContractRevisionAsConsumer_thenThrowNotFoundException() {
    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(USER_UID);
    when(contractRevisionRepository.findByIdAndDataConsumerUid(REVISION_ID, USER_UID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getAsConsumer(REVISION_ID))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining(REVISION_ID.toString());

    verifyNoInteractions(dataRequestQueryService);
  }

  @Test
  void givenExistingContractRevision_whenGetContractRevisionAsProvider_thenReturnEntity() {
    DataRequestEntity dataRequestEntity = DataRequestEntity.builder().id(DATA_REQUEST_ID).build();
    ContractRevisionEntity contractRevisionEntity = ContractRevisionEntity.builder()
        .id(REVISION_ID)
        .dataRequest(dataRequestEntity)
        .build();
    when(contractRevisionRepository.findByIdOptional(REVISION_ID)).thenReturn(Optional.of(contractRevisionEntity));
    when(dataRequestQueryService.isAssignedToCurrentProvider(dataRequestEntity)).thenReturn(true);

    var result = service.getAsProvider(REVISION_ID);

    assertThat(result.getId()).isEqualTo(REVISION_ID);

    verify(dataRequestQueryService).isAssignedToCurrentProvider(dataRequestEntity);
  }

  @Test
  void givenExistingContractRevision_whenGetContractRevisionAsUnauthorizedProvider_thenThrowNotFoundException() {
    DataRequestEntity dataRequestEntity = DataRequestEntity.builder().id(DATA_REQUEST_ID).build();
    ContractRevisionEntity contractRevisionEntity = ContractRevisionEntity.builder()
        .id(REVISION_ID)
        .dataRequest(dataRequestEntity)
        .build();
    when(contractRevisionRepository.findByIdOptional(REVISION_ID)).thenReturn(Optional.of(contractRevisionEntity));
    when(dataRequestQueryService.isAssignedToCurrentProvider(dataRequestEntity)).thenReturn(false);

    assertThatThrownBy(() -> service.getAsProvider(REVISION_ID))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining(REVISION_ID.toString());

    verify(dataRequestQueryService).isAssignedToCurrentProvider(dataRequestEntity);
  }

  @Test
  void givenMissingContractRevision_whenGetContractRevisionAsProvider_thenThrowNotFoundException() {
    when(contractRevisionRepository.findByIdOptional(REVISION_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getAsProvider(REVISION_ID))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining(REVISION_ID.toString());

    verifyNoInteractions(dataRequestQueryService);
  }
}
