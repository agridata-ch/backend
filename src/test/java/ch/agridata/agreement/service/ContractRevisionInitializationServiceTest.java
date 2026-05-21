package ch.agridata.agreement.service;

import static ch.agridata.agreement.utils.DataRequestTestUtils.DATA_SOURCE_SYSTEM_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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
import ch.agridata.product.dto.DataProductDto;
import ch.agridata.product.dto.DataProviderDto;
import ch.agridata.product.dto.DataSourceSystemDto;
import ch.agridata.uidregister.api.UidRegisterServiceApi;
import ch.agridata.uidregister.dto.UidRegisterOrganisationDto;
import jakarta.ws.rs.NotFoundException;
import java.math.BigInteger;
import java.util.List;
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

  @Mock
  private UidRegisterServiceApi uidRegisterServiceApi;

  @Mock
  private ContractRevisionPdfService contractRevisionPdfService;

  @InjectMocks
  private ContractRevisionInitializationService contractRevisionInitializationService;

  @Test
  void givenDataRequestWithoutCurrentContractRevision_whenCreateAndAssignInitialRevision_thenPersistAssignAndEnrichRevision() {
    DataRequestEntity dataRequest = DataRequestTestUtils.buildEntity();

    UUID contractRevisionId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    ContractRevisionEntity revision = ContractRevisionEntity.builder().
        id(contractRevisionId).dataProviderName("Provider Name").build();

    DataProviderDto dataProvider = DataProviderDto.builder().uid("CHE123456789").build();
    DataSourceSystemDto dataSourceSystem = DataSourceSystemDto.builder().dataProvider(dataProvider).build();
    List<DataProductDto> dataProductDtos = List.of(DataProductDto.builder().build());
    UidRegisterOrganisationDto dataProviderUidResult = UidRegisterOrganisationDto.builder().legalName("Provider Name").build();

    when(contractRevisionMapper.toInitialEntity(
        same(dataRequest),
        eq(dataProviderUidResult),
        eq(dataSourceSystem),
        eq(dataProductDtos)
    )).thenReturn(revision);
    when(dataProductApi.getDataSourceSystem(DATA_SOURCE_SYSTEM_ID)).thenReturn(dataSourceSystem);
    when(uidRegisterServiceApi.getByUid(BigInteger.valueOf(123456789))).thenReturn(dataProviderUidResult);
    when(dataProductApi.getProductsByIds(anyList())).thenReturn(dataProductDtos);


    ContractRevisionEntity result = contractRevisionInitializationService.createAndAssignInitialRevision(dataRequest);

    assertSame(revision, result);
    assertSame(revision.getId(), dataRequest.getCurrentContractRevisionId());
    assertEquals("Provider Name", revision.getDataProviderName());

    verify(contractRevisionMapper).toInitialEntity(
        same(dataRequest),
        eq(dataProviderUidResult),
        eq(dataSourceSystem),
        eq(dataProductDtos)
    );
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

    verify(contractRevisionMapper, never()).toInitialEntity(
        same(dataRequest),
        any(),
        any(),
        any()
    );

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
    verify(contractRevisionMapper, never()).toInitialEntity(
        same(dataRequest),
        any(),
        any(),
        any()
    );
    verify(contractRevisionRepository, never()).persist((ContractRevisionEntity) any());
    assertNull(dataRequest.getCurrentContractRevisionId());
  }
}
