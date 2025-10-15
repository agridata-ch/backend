package ch.agridata.agreement.service;

import static ch.agridata.agreement.utils.DataRequestTestUtils.USER_UID;
import static ch.agridata.agreement.utils.DataRequestTestUtils.buildEntity;
import static ch.agridata.agreement.utils.DataRequestTestUtils.buildUpdateDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.agridata.agreement.dto.DataRequestDto;
import ch.agridata.agreement.dto.DataRequestUpdateDto;
import ch.agridata.agreement.mapper.DataRequestMapper;
import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.agreement.persistence.DataRequestRepository;
import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.uidregister.api.UidRegisterServiceApi;
import ch.agridata.uidregister.dto.UidRegisterOrganisationDto;
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
class DataRequestMutationServiceTest {

  @InjectMocks
  private DataRequestMutationService dataRequestMutationService;
  @Mock
  private DataRequestMapper mapper;
  @Mock
  private DataRequestRepository repository;
  @Mock
  private UidRegisterServiceApi uidRegisterServiceApi;
  @Mock
  private AgridataSecurityIdentity agridataSecurityIdentity;
  @Mock
  private HumanFriendlyIdService humanFriendlyIdService;
  @Captor
  private ArgumentCaptor<DataRequestEntity> dataRequestEntityCaptor;


  @Test
  void givenValidInput_whenCreateDataRequestDraft_thenReturnMappedDto() {
    DataRequestUpdateDto dto = buildUpdateDto();
    UidRegisterOrganisationDto uidSearchResult = UidRegisterOrganisationDto.builder()
        .uid("CHE101708094")
        .legalName("Test Organisation")
        .build();

    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(USER_UID);
    when(uidRegisterServiceApi.getByUidOfCurrentUser()).thenReturn(uidSearchResult);
    when(humanFriendlyIdService.getHumanFriendlyIdForDataRequest()).thenReturn("AB57");
    var result = dataRequestMutationService.createDataRequestDraft(dto);

    DataRequestDto expectedDto = verify(mapper).toDto(dataRequestEntityCaptor.capture());
    assertThat(dataRequestEntityCaptor.getValue().getHumanFriendlyId()).isEqualTo("AB57");
    assertThat(result).isEqualTo(expectedDto);
  }

  @Test
  void givenValidDraft_whenUpdateDataRequestDetails_thenReturnUpdatedDto() {
    var id = UUID.randomUUID();
    var entity = buildEntity();

    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(USER_UID);
    when(repository.findByIdAndDataConsumerUid(id, USER_UID)).thenReturn(Optional.of(entity));

    var result = dataRequestMutationService.updateDataRequestDetails(id, buildUpdateDto());

    DataRequestDto expectedDto = verify(mapper).toDto(dataRequestEntityCaptor.capture());
    assertThat(dataRequestEntityCaptor.getValue().getStateCode()).isEqualTo(DataRequestEntity.DataRequestStateEnum.DRAFT);
    assertThat(result).isEqualTo(expectedDto);
  }

}
