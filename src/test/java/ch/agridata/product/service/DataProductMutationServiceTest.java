package ch.agridata.product.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ch.agridata.product.dto.DataProductUpdateDto;
import ch.agridata.product.mapper.DataProductMapperImpl;
import ch.agridata.product.persistence.DataProductEntity;
import ch.agridata.product.persistence.DataProductRepository;
import ch.agridata.product.persistence.DataProductStateEnum;
import ch.agridata.product.persistence.DataProviderEntity;
import ch.agridata.product.persistence.DataSourceSystemEntity;
import ch.agridata.product.persistence.DataSourceSystemRepository;
import ch.agridata.product.persistence.RestClientRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DataProductMutationServiceTest {
  @Mock
  private DataProductRepository dataProductRepository;
  @Mock
  private DataSourceSystemRepository dataSourceSystemRepository;
  @Mock
  private DataProductMapperImpl dataProductMapper;
  @Mock
  private RestClientRepository restClientRepository;
  @Mock
  private DataProductDocumentService dataProductDocumentService;
  @InjectMocks
  private DataProductMutationService dataProductMutationService;

  @Test
  void givenAdminAndDataProductDraftWithDataSourceSystem_whenUpdateDraft_thenUpdateUid() {
    UUID dataProductId = UUID.randomUUID();
    UUID dataSourceId = UUID.randomUUID();
    String providerUid = "CH000000001";
    DataProductUpdateDto updateDto = DataProductUpdateDto.builder()
        .dataSourceSystemId(dataSourceId)
        .build();
    DataProductEntity dataProductEntity = spy(DataProductEntity.builder().stateCode(DataProductStateEnum.DRAFT).build());
    DataProviderEntity dataProviderEntity = mock(DataProviderEntity.class);
    when(dataProviderEntity.getUid()).thenReturn(providerUid);
    DataSourceSystemEntity dataSourceSystemEntity = mock(DataSourceSystemEntity.class);
    when(dataSourceSystemEntity.getDataProvider()).thenReturn(dataProviderEntity);
    when(dataSourceSystemRepository.findByIdOptional(dataSourceId)).thenReturn(Optional.of(dataSourceSystemEntity));
    when(dataProductRepository.findByIdOptional(dataProductId)).thenReturn(Optional.of(dataProductEntity));

    dataProductMutationService.updateDataProductDraftAsAdmin(dataProductId, updateDto);

    verify(dataProductEntity).setDataProviderUid(providerUid);
  }

  @Test
  void givenAdminAndDataProductDraft_whenDeleteDraft_thenDocumentsAndProductDeleted() {
    UUID dataProductId = UUID.randomUUID();
    DataProductEntity dataProductEntity = DataProductEntity.builder()
        .id(dataProductId)
        .stateCode(DataProductStateEnum.DRAFT)
        .build();
    when(dataProductRepository.findByIdOptional(dataProductId)).thenReturn(Optional.of(dataProductEntity));

    dataProductMutationService.deleteDataProductDraftAsAdmin(dataProductId);

    InOrder inOrder = inOrder(dataProductDocumentService, dataProductRepository);
    inOrder.verify(dataProductDocumentService).deleteAllDataProductDocuments(dataProductId);
    inOrder.verify(dataProductRepository).delete(dataProductEntity);
  }

  @Test
  void givenAdminAndActiveDataProduct_whenDeleteDraft_thenIllegalStateExceptionAndNothingDeleted() {
    UUID dataProductId = UUID.randomUUID();
    DataProductEntity dataProductEntity = DataProductEntity.builder()
        .id(dataProductId)
        .stateCode(DataProductStateEnum.ACTIVE)
        .build();
    when(dataProductRepository.findByIdOptional(dataProductId)).thenReturn(Optional.of(dataProductEntity));

    assertThatThrownBy(() -> dataProductMutationService.deleteDataProductDraftAsAdmin(dataProductId))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("must be in state DRAFT");

    verifyNoInteractions(dataProductDocumentService);
    verify(dataProductRepository, never()).delete(dataProductEntity);
  }
}
