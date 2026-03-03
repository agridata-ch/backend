package ch.agridata.agreement.service;

import static ch.agridata.agreement.service.ConsentRequestMutationServiceTest.UID1;
import static ch.agridata.agreement.utils.DataRequestTestUtils.PRODUCT_ID;
import static ch.agridata.agreement.utils.DataRequestTestUtils.USER_UID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.agridata.agreement.dto.DataRequestDto;
import ch.agridata.agreement.dto.DataRequestUpdateDto;
import ch.agridata.agreement.mapper.DataRequestMapper;
import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.agreement.persistence.DataRequestRepository;
import ch.agridata.agreement.utils.DataRequestTestUtils;
import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.product.api.DataProductApi;
import ch.agridata.uidregister.api.UidRegisterServiceApi;
import ch.agridata.uidregister.dto.UidRegisterOrganisationDto;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
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
  private DataRequestRepository dataRequestRepository;
  @Mock
  private UidRegisterServiceApi uidRegisterServiceApi;
  @Mock
  private AgridataSecurityIdentity agridataSecurityIdentity;
  @Mock
  private HumanFriendlyIdService humanFriendlyIdService;
  @Mock
  private DataProductApi dataProductApi;
  @Captor
  private ArgumentCaptor<DataRequestEntity> dataRequestEntityCaptor;
  @Mock
  private DataRequestEnrichmentService dataRequestEnrichmentService;


  @Test
  void givenValidInput_whenCreateDataRequestDraft_thenReturnMappedDto() {
    DataRequestUpdateDto dto = DataRequestTestUtils.updateDtoBuilder().build();
    UidRegisterOrganisationDto uidSearchResult = DataRequestTestUtils.buildUidSearchResult();

    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(USER_UID);
    when(dataRequestRepository.countByDataConsumerUidAndState(
        uidSearchResult.uid(),
        DataRequestEntity.DataRequestStateEnum.DRAFT
    )).thenReturn(0L);
    when(uidRegisterServiceApi.getByUidOfCurrentUser()).thenReturn(uidSearchResult);
    when(humanFriendlyIdService.getHumanFriendlyIdForDataRequest()).thenReturn("AB57");
    when(dataProductApi.getProductById(PRODUCT_ID))
        .thenReturn(DataRequestTestUtils.dataProductDtoBuilder(PRODUCT_ID).build());

    var result = dataRequestMutationService.createDataRequestDraft(dto);

    DataRequestDto expectedDto = verify(dataRequestEnrichmentService).toEnrichedDto(dataRequestEntityCaptor.capture());
    assertThat(dataRequestEntityCaptor.getValue().getHumanFriendlyId()).isEqualTo("AB57");
    assertThat(result).isEqualTo(expectedDto);
  }

  @Test
  void givenValidDraft_whenUpdateDataRequestDetails_thenReturnUpdatedDto() {
    var id = UUID.randomUUID();
    var entity = DataRequestTestUtils.buildEntity();

    var updateProductId = UUID.fromString("00000000-0000-0000-0000-000000000002");
    var updateDto = DataRequestTestUtils.updateDtoBuilder()
        .products(List.of(updateProductId))
        .build();

    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(USER_UID);
    when(dataRequestRepository.findByIdAndDataConsumerUid(id, USER_UID)).thenReturn(Optional.of(entity));
    when(dataProductApi.getProductById(updateProductId))
        .thenReturn(DataRequestTestUtils.dataProductDtoBuilder(updateProductId).build());

    var result = dataRequestMutationService.updateDataRequestDetails(id, updateDto);

    DataRequestDto expectedDto = verify(dataRequestEnrichmentService).toEnrichedDto(dataRequestEntityCaptor.capture());
    assertThat(dataRequestEntityCaptor.getValue().getStateCode()).isEqualTo(DataRequestEntity.DataRequestStateEnum.DRAFT);
    assertThat(result).isEqualTo(expectedDto);
  }

  @Test
  void givenDataProductsFromDifferentSystems_whenCreateDataRequestDraft_thenThrowValidationException() {
    var agisProductId = UUID.fromString("00000000-0000-0000-0000-000000000002");
    var tvdProductId = UUID.fromString("00000000-0000-0000-0000-000000000003");
    DataRequestUpdateDto updateDto = DataRequestTestUtils.updateDtoBuilder()
        .products(List.of(agisProductId, tvdProductId))
        .build();
    UidRegisterOrganisationDto uidSearchResult = DataRequestTestUtils.buildUidSearchResult();


    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(USER_UID);
    when(dataRequestRepository.countByDataConsumerUidAndState(
        uidSearchResult.uid(),
        DataRequestEntity.DataRequestStateEnum.DRAFT
    )).thenReturn(0L);
    when(uidRegisterServiceApi.getByUidOfCurrentUser()).thenReturn(uidSearchResult);
    when(humanFriendlyIdService.getHumanFriendlyIdForDataRequest()).thenReturn("AB57");
    when(dataProductApi.getProductById(agisProductId))
        .thenReturn(DataRequestTestUtils.dataProductDtoBuilder(tvdProductId).dataSourceSystemCode("AGIS").build());
    when(dataProductApi.getProductById(tvdProductId))
        .thenReturn(DataRequestTestUtils.dataProductDtoBuilder(agisProductId).dataSourceSystemCode("TVD").build());

    assertThatThrownBy(() -> dataRequestMutationService.createDataRequestDraft(updateDto))
        .isInstanceOf(ValidationException.class)
        .hasMessage("Cannot process request: all products must share the same data source system");
  }

  @Test
  void givenInexistentDataProduct_whenCreateDataRequestDraft_thenThrowValidationException() {
    var inexistentProductId = UUID.randomUUID();
    DataRequestUpdateDto dataRequestUpdateDto = DataRequestTestUtils.updateDtoBuilder()
        .products(List.of(inexistentProductId))
        .build();

    UidRegisterOrganisationDto uidSearchResult = DataRequestTestUtils.buildUidSearchResult();

    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(USER_UID);
    when(dataRequestRepository.countByDataConsumerUidAndState(
        uidSearchResult.uid(),
        DataRequestEntity.DataRequestStateEnum.DRAFT
    )).thenReturn(0L);
    when(uidRegisterServiceApi.getByUidOfCurrentUser()).thenReturn(uidSearchResult);
    when(humanFriendlyIdService.getHumanFriendlyIdForDataRequest()).thenReturn("AB57");
    when(dataProductApi.getProductById(inexistentProductId))
        .thenThrow(new NotFoundException(inexistentProductId.toString()));

    assertThatThrownBy(() -> dataRequestMutationService.createDataRequestDraft(dataRequestUpdateDto))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining(inexistentProductId.toString());
  }

  @Test
  void givenInexistentDataProduct_whenUpdateDataRequestDetails_thenThrowValidationException() {
    var id = UUID.randomUUID();
    var entity = DataRequestTestUtils.buildEntity();
    var inexistentProductId = UUID.randomUUID();
    DataRequestUpdateDto dataRequestUpdateDto = DataRequestTestUtils.updateDtoBuilder()
        .products(List.of(inexistentProductId))
        .build();

    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(USER_UID);
    when(dataRequestRepository.findByIdAndDataConsumerUid(id, USER_UID)).thenReturn(Optional.of(entity));
    when(dataProductApi.getProductById(inexistentProductId))
        .thenThrow(new NotFoundException(inexistentProductId.toString()));

    assertThatThrownBy(() -> dataRequestMutationService.updateDataRequestDetails(id, dataRequestUpdateDto))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining(inexistentProductId.toString());
  }

  @Test
  void givenNoDataProduct_whenCreateDataRequestDraft_thenReturnMappedDto() {
    DataRequestUpdateDto dto = DataRequestTestUtils.updateDtoBuilder()
        .products(List.of())
        .build();
    UidRegisterOrganisationDto uidSearchResult = DataRequestTestUtils.buildUidSearchResult();

    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(USER_UID);
    when(dataRequestRepository.countByDataConsumerUidAndState(
        uidSearchResult.uid(),
        DataRequestEntity.DataRequestStateEnum.DRAFT
    )).thenReturn(0L);
    when(uidRegisterServiceApi.getByUidOfCurrentUser()).thenReturn(uidSearchResult);
    when(humanFriendlyIdService.getHumanFriendlyIdForDataRequest()).thenReturn("AB57");

    var result = dataRequestMutationService.createDataRequestDraft(dto);

    DataRequestDto expectedDto = verify(dataRequestEnrichmentService).toEnrichedDto(dataRequestEntityCaptor.capture());
    assertThat(dataRequestEntityCaptor.getValue().getHumanFriendlyId()).isEqualTo("AB57");
    assertThat(result).isEqualTo(expectedDto);
  }

  @Test
  void given10DraftsExist_whenCreateDataRequestDraft_thenThrowValidationException() {
    DataRequestUpdateDto dto = DataRequestTestUtils.updateDtoBuilder().build();
    UidRegisterOrganisationDto uidSearchResult = DataRequestTestUtils.buildUidSearchResult();

    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(USER_UID);
    when(dataRequestRepository.countByDataConsumerUidAndState(uidSearchResult.uid(),
        DataRequestEntity.DataRequestStateEnum.DRAFT)).thenReturn(10L);

    assertThatThrownBy(() -> dataRequestMutationService.createDataRequestDraft(dto))
        .isInstanceOf(ValidationException.class)
        .hasMessage("Cannot create new data request: maximum number of 10 draft requests reached");
  }

  @Test
  void givenDraftDataRequestAndUid_whenDeleteDataRequest_thenReturnValidResponse() {
    UUID dataRequestId = UUID.randomUUID();

    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(UID1);
    when(dataRequestRepository.archiveDraftByIdAndConsumerUid(dataRequestId, UID1)).thenReturn(1L);

    dataRequestMutationService.deleteDataRequest(dataRequestId);
    verify(dataRequestRepository).archiveDraftByIdAndConsumerUid(dataRequestId, UID1);
  }

  @Test
  void givenInexistentOrNotOwnedDataRequestAndUid_whenDeleteDataRequest_thenThrowNotFoundException() {
    UUID dataRequestId = UUID.randomUUID();

    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(UID1);
    when(dataRequestRepository.archiveDraftByIdAndConsumerUid(dataRequestId, UID1)).thenReturn(0L);
    when(dataRequestRepository.existsByIdAndConsumerUid(dataRequestId, UID1)).thenReturn(false);

    assertThatThrownBy(() -> dataRequestMutationService.deleteDataRequest(dataRequestId))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining(dataRequestId.toString());
    verify(dataRequestRepository).archiveDraftByIdAndConsumerUid(dataRequestId, UID1);
    verify(dataRequestRepository).existsByIdAndConsumerUid(dataRequestId, UID1);
  }

  @Test
  void givenActiveDataRequestAndUid_whenDeleteDataRequest_thenThrowValidationException() {
    UUID dataRequestId = UUID.randomUUID();

    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(UID1);
    when(dataRequestRepository.archiveDraftByIdAndConsumerUid(dataRequestId, UID1)).thenReturn(0L);
    when(dataRequestRepository.existsByIdAndConsumerUid(dataRequestId, UID1)).thenReturn(true);

    assertThatThrownBy(() -> dataRequestMutationService.deleteDataRequest(dataRequestId))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("DRAFT");
    verify(dataRequestRepository).archiveDraftByIdAndConsumerUid(dataRequestId, UID1);
    verify(dataRequestRepository).existsByIdAndConsumerUid(dataRequestId, UID1);
  }
}
