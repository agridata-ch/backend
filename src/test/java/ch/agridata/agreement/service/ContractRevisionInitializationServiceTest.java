package ch.agridata.agreement.service;

import static ch.agridata.agreement.utils.DataRequestTestUtils.DATA_SOURCE_SYSTEM_ID;
import static ch.agridata.agreement.utils.DataRequestTestUtils.buildTranslationDto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.agridata.agreement.mapper.ContractRevisionMapper;
import ch.agridata.agreement.persistence.ContractRevisionEntity;
import ch.agridata.agreement.persistence.ContractRevisionRepository;
import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.agreement.utils.DataRequestTestUtils;
import ch.agridata.product.api.DataProductApi;
import ch.agridata.product.dto.DataProviderDto;
import ch.agridata.product.dto.DataSourceSystemDto;
import jakarta.ws.rs.NotFoundException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContractRevisionInitializationServiceTest {

  @Mock
  private ContractRevisionRepository contractRevisionRepository;

  @Mock
  private ContractRevisionMapper contractRevisionMapper;

  @Mock
  private DataProductApi dataProductApi;

  @InjectMocks
  private ContractRevisionInitializationService contractRevisionInitializationService;

  @Test
  void givenDataRequestWithoutCurrentContractRevision_whenCreateAndAssignInitialRevision_thenPersistAssignAndEnrichRevision() {
    DataRequestEntity dataRequest = DataRequestTestUtils.buildEntity();

    UUID contractRevisionId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    ContractRevisionEntity revision = ContractRevisionEntity.builder().
        id(contractRevisionId).dataProviderName("Provider DE").build();

    DataProviderDto dataProvider = DataProviderDto
        .builder().name(buildTranslationDto("Provider DE", "Provider FR", "Provider IT")).build();

    DataSourceSystemDto dataSourceSystem = DataSourceSystemDto.builder().dataProvider(dataProvider).build();

    when(contractRevisionMapper.toInitialEntity(same(dataRequest), eq("Provider DE"))).thenReturn(revision);
    when(dataProductApi.getDataSourceSystem(DATA_SOURCE_SYSTEM_ID)).thenReturn(dataSourceSystem);

    ContractRevisionEntity result = contractRevisionInitializationService.createAndAssignInitialRevision(dataRequest);

    assertSame(revision, result);
    assertSame(revision.getId(), dataRequest.getCurrentContractRevisionId());
    assertEquals("Provider DE", revision.getDataProviderName());

    verify(contractRevisionMapper).toInitialEntity(same(dataRequest), eq("Provider DE"));
    verify(dataProductApi).getDataSourceSystem(DATA_SOURCE_SYSTEM_ID);
    verify(contractRevisionRepository).persist(same(revision));
  }

  @Test
  void givenDataRequestWithCurrentContractRevision_whenCreateAndAssignInitialRevision_thenThrowIllegalStateException() {
    DataRequestEntity dataRequest = DataRequestTestUtils.buildEntity();
    UUID contractRevisionId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    dataRequest.setCurrentContractRevisionId(contractRevisionId);

    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> contractRevisionInitializationService.createAndAssignInitialRevision(dataRequest)
        );

    assertEquals("Data request already has a contract revision", exception.getMessage());

    verify(contractRevisionMapper, never()).toInitialEntity(same(dataRequest), eq("Provider DE"));
    verify(dataProductApi, never()).getDataSourceSystem(any());
    verify(contractRevisionRepository, never()).persist((ContractRevisionEntity) any());
  }

  @Test
  void givenMissingDataSourceSystem_whenCreateAndAssignInitialRevision_thenThrowIllegalStateException() {
    DataRequestEntity dataRequest = DataRequestTestUtils.buildEntity();

    when(dataProductApi.getDataSourceSystem(DATA_SOURCE_SYSTEM_ID)).thenThrow(
        new NotFoundException("Data source system not found for id: " + DATA_SOURCE_SYSTEM_ID)
    );

    NotFoundException exception =
        assertThrows(NotFoundException.class, () -> contractRevisionInitializationService.createAndAssignInitialRevision(dataRequest));

    assertEquals("Data source system not found for id: " + DATA_SOURCE_SYSTEM_ID, exception.getMessage());

    verify(dataProductApi).getDataSourceSystem(DATA_SOURCE_SYSTEM_ID);
    verify(contractRevisionMapper, never()).toInitialEntity(same(dataRequest), eq("Provider DE"));
    verify(contractRevisionRepository, never()).persist((ContractRevisionEntity) any());
    assertNull(dataRequest.getCurrentContractRevisionId());
  }
}
