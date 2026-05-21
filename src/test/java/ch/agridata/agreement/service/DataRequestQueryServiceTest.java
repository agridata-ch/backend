package ch.agridata.agreement.service;

import static ch.agridata.agreement.utils.DataRequestTestUtils.USER_UID;
import static ch.agridata.agreement.utils.DataRequestTestUtils.buildEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import ch.agridata.agreement.mapper.DataRequestMapper;
import ch.agridata.agreement.mapper.DataRequestMapperImpl;
import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.agreement.persistence.DataRequestRepository;
import ch.agridata.common.security.AgridataSecurityIdentity;
import ch.agridata.product.api.DataProductApi;
import ch.agridata.product.dto.DataProviderDto;
import ch.agridata.product.dto.DataSourceSystemDto;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DataRequestQueryServiceTest {

  @Spy
  private final DataRequestMapper mapper = new DataRequestMapperImpl();
  @InjectMocks
  private DataRequestQueryService service;
  @Mock
  private DataRequestRepository repository;
  @Mock
  private DataProductApi dataProductApi;
  @Mock
  private AgridataSecurityIdentity agridataSecurityIdentity;
  @Mock
  private DataRequestEnrichmentService dataRequestEnrichmentService;

  private static final Set<DataRequestEntity.DataRequestStateEnum> PROVIDER_ACCESSIBLE_STATES = Set.of(
      DataRequestEntity.DataRequestStateEnum.ACTIVE,
      DataRequestEntity.DataRequestStateEnum.TO_BE_ACTIVATED,
      DataRequestEntity.DataRequestStateEnum.TO_BE_SIGNED_BY_PROVIDER,
      DataRequestEntity.DataRequestStateEnum.TO_BE_RELEASED_BY_PROVIDER
  );

  @Test
  void givenRequestsExist_whenGetAllRequests_OfConsumer_thenReturnDtos() {
    var dataConsumer = buildEntity();
    when(repository.findByDataConsumerUid(USER_UID)).thenReturn(List.of(dataConsumer));
    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(USER_UID);
    when(dataRequestEnrichmentService.toEnrichedDto(any(DataRequestEntity.class)))
        .thenAnswer(inv -> mapper.toDto(inv.getArgument(0), null));

    var result = service.getAllDataRequestsOfCurrentConsumer();

    assertEquals(1, result.size());
  }

  @Test
  void givenActiveRequestsExist_whenGetAllActiveRequests_OfProvider_thenReturnDtos() {
    var dataRequest = buildEntity();
    var dataProviderDto = DataProviderDto.builder()
        .uid(USER_UID)
        .build();
    var dataSourceSystemDto = DataSourceSystemDto.builder()
        .dataProvider(dataProviderDto)
        .build();
    when(repository.findAllByStates(PROVIDER_ACCESSIBLE_STATES)).thenReturn(List.of(dataRequest));
    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(USER_UID);
    when(dataRequestEnrichmentService.toEnrichedDto(any(DataRequestEntity.class)))
        .thenAnswer(inv -> mapper.toDto(inv.getArgument(0), dataSourceSystemDto));

    var result = service.getRelevantDataRequestsForCurrentProvider();

    assertEquals(1, result.size());
  }

  @Test
  void givenRequestsExist_whenGetAllNonDraftRequests_OfAdmin_thenReturnDtos() {
    var entity = buildEntity();
    when(repository.findAllNotDraft()).thenReturn(List.of(entity));
    when(dataRequestEnrichmentService.toEnrichedDto(any(DataRequestEntity.class)))
        .thenAnswer(inv -> mapper.toDto(inv.getArgument(0), null));

    var result = service.getAllNonDraftDataRequests();

    assertEquals(1, result.size());
  }

  @Test
  void givenNonDraftRequestExists_whenGetNonDraftRequests_OfAdmin_thenReturnDto() {
    var id = UUID.randomUUID();
    var entity = buildEntity();
    entity.setStateCode(DataRequestEntity.DataRequestStateEnum.IN_REVIEW);
    when(dataRequestEnrichmentService.toEnrichedDto(any(DataRequestEntity.class)))
        .thenAnswer(inv -> mapper.toDto(inv.getArgument(0), null));

    when(repository.findByIdAndStateCodeNotDraft(id)).thenReturn(Optional.of(entity));

    var result = service.getNonDraftDataRequest(id);

    assertThat(result).isNotNull();
  }

  @Test
  void givenDraftRequestExists_whenGetNonDraftRequests_OfAdmin_thenThrowNotFound() {
    var id = UUID.randomUUID();

    when(repository.findByIdAndStateCodeNotDraft(id)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> service.getNonDraftDataRequest(id));
  }

  @Test
  void givenValidId_whenGetDataRequestOfCurrentConsumer_thenReturnDto() {
    var id = UUID.randomUUID();
    var entity = buildEntity();

    when(repository.findByIdAndDataConsumerUid(id, USER_UID)).thenReturn(Optional.of(entity));
    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(USER_UID);
    when(dataRequestEnrichmentService.toEnrichedDto(any(DataRequestEntity.class)))
        .thenAnswer(inv -> mapper.toDto(inv.getArgument(0), null));

    var result = service.getDataRequestOfCurrentConsumer(id);

    assertThat(result).isNotNull();
  }

  @Test
  void givenInvalidId_whenGetDataRequestOfCurrentConsumer_thenThrowNotFound() {
    UUID id = UUID.randomUUID();

    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(USER_UID);
    when(repository.findByIdAndDataConsumerUid(id, USER_UID)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> service.getDataRequestOfCurrentConsumer(id));
  }

  @Test
  void givenValidId_whenGetDataRequestForCurrentProvider_thenReturnDto() {
    var id = UUID.randomUUID();
    var entity = buildEntity();
    var dataProviderDto = DataProviderDto.builder()
        .uid(USER_UID)
        .build();
    var dataSourceSystemDto = DataSourceSystemDto.builder()
        .dataProvider(dataProviderDto)
        .build();

    when(repository.findByIdAndStates(id, PROVIDER_ACCESSIBLE_STATES)).thenReturn(Optional.of(entity));
    when(agridataSecurityIdentity.getUidOrElseThrow()).thenReturn(USER_UID);
    when(dataRequestEnrichmentService.toEnrichedDto(any(DataRequestEntity.class)))
        .thenAnswer(inv -> mapper.toDto(inv.getArgument(0), dataSourceSystemDto));

    var result = service.getDataRequestForCurrentProvider(id);

    assertThat(result).isNotNull();
  }

  @Test
  void givenInvalidId_whenGetDataRequestForCurrentProvider_thenThrowNotFound() {
    UUID id = UUID.randomUUID();

    when(repository.findByIdAndStates(id, PROVIDER_ACCESSIBLE_STATES)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> service.getDataRequestForCurrentProvider(id));
  }
}
