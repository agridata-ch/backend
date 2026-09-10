package ch.agridata.agreement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import ch.agridata.agreement.dto.DataRequestDto;
import ch.agridata.agreement.mapper.DataRequestMapper;
import ch.agridata.agreement.persistence.DataRequestDataProductEntity;
import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.product.api.DataProductApi;
import ch.agridata.product.dto.DataProductDto;
import ch.agridata.product.dto.DataSourceSystemDto;
import ch.agridata.product.dto.FlowCodeEnum;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DataRequestEnrichmentServiceTest {

  private static final UUID PRODUCT_ID = UUID.randomUUID();

  @InjectMocks
  private DataRequestEnrichmentService service;

  @Mock
  private DataProductApi dataProductApi;
  @Mock
  private DataRequestMapper mapper;

  @Test
  void givenNullEntity_whenToEnrichedDto_thenReturnNull() {
    var result = service.toEnrichedDto(null);

    assertThat(result).isNull();
    verifyNoInteractions(dataProductApi);
  }

  @Test
  void givenEntityWithoutDataSourceSystemId_whenToEnrichedDto_thenMapWithNullDataSourceSystem() {
    var entity = entityWithProduct(PRODUCT_ID);
    when(entity.getDataSourceSystemId()).thenReturn(null);
    when(dataProductApi.getActiveProductsByIds(List.of(PRODUCT_ID))).thenReturn(List.of());

    var mapped = mock(DataRequestDto.class);
    when(mapper.toDto(entity, null, false)).thenReturn(mapped);

    var result = service.toEnrichedDto(entity);

    assertThat(result).isSameAs(mapped);

    verify(entity).getDataSourceSystemId();
    verify(mapper).toDto(entity, null, false);
    verifyNoMoreInteractions(mapper);
  }

  @Test
  void givenEntityWithDataSourceSystemId_whenToEnrichedDto_thenFetchDataSourceSystem_andMapWithIt() {
    UUID dataSourceSystemId = UUID.randomUUID();
    var entity = entityWithProduct(PRODUCT_ID);
    when(entity.getDataSourceSystemId()).thenReturn(dataSourceSystemId);
    when(dataProductApi.getActiveProductsByIds(List.of(PRODUCT_ID))).thenReturn(List.of());

    DataSourceSystemDto dataSourceSystem = mock(DataSourceSystemDto.class);
    when(dataProductApi.getDataSourceSystem(dataSourceSystemId)).thenReturn(dataSourceSystem);

    DataRequestDto mapped = mock(DataRequestDto.class);
    when(mapper.toDto(entity, dataSourceSystem, false)).thenReturn(mapped);

    DataRequestDto result = service.toEnrichedDto(entity);

    assertThat(result).isSameAs(mapped);

    verify(dataProductApi).getDataSourceSystem(dataSourceSystemId);
    verify(mapper).toDto(entity, dataSourceSystem, false);
    verifyNoMoreInteractions(mapper);
  }

  @Test
  void givenOnlyUidBasedProducts_whenToEnrichedDto_thenBurPresentIsFalse() {
    var entity = entityWithProduct(PRODUCT_ID);
    when(dataProductApi.getActiveProductsByIds(List.of(PRODUCT_ID)))
        .thenReturn(List.of(
            productWithFlowCode(FlowCodeEnum.UID_BASED_PRE_VALIDATION),
            productWithFlowCode(FlowCodeEnum.UID_BASED_POST_VALIDATION),
            productWithFlowCode(FlowCodeEnum.UNBOUND_UID_BASED_POST_VALIDATION)
        ));

    service.toEnrichedDto(entity);

    verify(mapper).toDto(entity, null, false);
  }

  @Test
  void givenOneBurBasedProduct_whenToEnrichedDto_thenBurPresentIsTrue() {
    var entity = entityWithProduct(PRODUCT_ID);
    when(dataProductApi.getActiveProductsByIds(List.of(PRODUCT_ID)))
        .thenReturn(List.of(
            productWithFlowCode(FlowCodeEnum.UID_BASED_PRE_VALIDATION),
            productWithFlowCode(FlowCodeEnum.BUR_BASED_POST_VALIDATION)
        ));

    service.toEnrichedDto(entity);

    verify(mapper).toDto(entity, null, true);
  }

  private DataRequestEntity entityWithProduct(UUID productId) {
    var entity = mock(DataRequestEntity.class);
    var product = new DataRequestDataProductEntity(entity, productId);
    when(entity.getDataProducts()).thenReturn(List.of(product));
    return entity;
  }

  private DataProductDto productWithFlowCode(FlowCodeEnum flowCode) {
    return DataProductDto.builder().flowCode(flowCode).build();
  }

  @Test
  void givenDataSourceSystemNotFound_whenToEnrichedDto_thenPropagateNotFoundException_andDoNotMap() {
    UUID dataSourceSystemId = UUID.randomUUID();

    DataRequestEntity entity = mock(DataRequestEntity.class);
    when(entity.getDataSourceSystemId()).thenReturn(dataSourceSystemId);
    when(dataProductApi.getDataSourceSystem(dataSourceSystemId)).thenThrow(new NotFoundException("not found"));

    assertThatThrownBy(() -> service.toEnrichedDto(entity))
        .isInstanceOf(NotFoundException.class);

    verify(entity).getDataSourceSystemId();
    verify(dataProductApi).getDataSourceSystem(dataSourceSystemId);
    verifyNoMoreInteractions(dataProductApi);
  }
}
