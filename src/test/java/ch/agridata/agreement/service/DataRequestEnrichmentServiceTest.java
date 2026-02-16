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
import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.product.api.DataProductApi;
import ch.agridata.product.dto.DataSourceSystemDto;
import jakarta.ws.rs.NotFoundException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DataRequestEnrichmentServiceTest {

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
    var entity = mock(DataRequestEntity.class);
    when(entity.getDataSourceSystemId()).thenReturn(null);

    var mapped = mock(DataRequestDto.class);
    when(mapper.toDto(entity, null)).thenReturn(mapped);

    var result = service.toEnrichedDto(entity);

    assertThat(result).isSameAs(mapped);

    verifyNoInteractions(dataProductApi);
    verify(entity).getDataSourceSystemId();
    verify(mapper).toDto(entity, null);
    verifyNoMoreInteractions(mapper);
  }

  @Test
  void givenEntityWithDataSourceSystemId_whenToEnrichedDto_thenFetchDataSourceSystem_andMapWithIt() {
    UUID dataSourceSystemId = UUID.randomUUID();
    var entity = mock(DataRequestEntity.class);
    when(entity.getDataSourceSystemId()).thenReturn(dataSourceSystemId);

    DataSourceSystemDto dataSourceSystem = mock(DataSourceSystemDto.class);
    when(dataProductApi.getDataSourceSystem(dataSourceSystemId)).thenReturn(dataSourceSystem);

    DataRequestDto mapped = mock(DataRequestDto.class);
    when(mapper.toDto(entity, dataSourceSystem)).thenReturn(mapped);

    DataRequestDto result = service.toEnrichedDto(entity);

    assertThat(result).isSameAs(mapped);

    verify(dataProductApi).getDataSourceSystem(dataSourceSystemId);
    verify(mapper).toDto(entity, dataSourceSystem);
    verifyNoMoreInteractions(mapper);
    verify(dataProductApi).getDataSourceSystem(dataSourceSystemId);
    verifyNoMoreInteractions(dataProductApi, mapper);
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
