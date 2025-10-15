package ch.agridata.datatransfer.service;

import static ch.agridata.datatransfer.client.DataProviderRestClientFactory.RestClientIdentifier.AGIS_STRUCTURE_V1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ch.agridata.datatransfer.client.AgisStructureApiRestClient;
import ch.agridata.datatransfer.client.DataProviderRestClientFactory;
import ch.agridata.product.api.DataProductApi;
import ch.agridata.product.dto.DataProductProviderConfigurationDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DataFetchingServiceTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final UUID PRODUCT_ID = UUID.randomUUID();
  private static final Object DATA = new Object();

  @Mock
  DataProviderRestClientFactory dataProviderRestClientFactory;
  @Mock
  AgisStructureApiRestClient agisStructureApiRestClient;
  @Mock
  DataProductApi dataProductApi;
  @Captor
  ArgumentCaptor<JsonNode> requestCaptor;
  @InjectMocks
  DataFetchingService dataFetchingService;

  @Test
  void givenProductConfigurationMatchesParams_whenDataIsFetched_thenDataIsReturned() throws JsonProcessingException {
    var params = Map.of(
        "year", "2024",
        "bur", "A1234");
    var productConfigurationDto = DataProductProviderConfigurationDto.builder()
        .id(PRODUCT_ID)
        .restClientIdentifierCode("AGIS_STRUCTURE_V1")
        .restClientMethodCode("POST")
        .restClientPath("random-path")
        .restClientRequestTemplate("""
            {
              "surveyYear": "{{year}}",
              "ids": {
                "ber": "{{bur}}"
              }
            }
            """)
        .build();

    when(dataProductApi.getProviderConfigurationById(PRODUCT_ID)).thenReturn(productConfigurationDto);
    when(dataProviderRestClientFactory.get(AGIS_STRUCTURE_V1)).thenReturn(agisStructureApiRestClient);
    when(agisStructureApiRestClient.post(any(), requestCaptor.capture())).thenReturn(DATA);

    var response = dataFetchingService.fetchData(PRODUCT_ID, params);

    assertThat(requestCaptor.getValue()).isEqualTo(MAPPER.readTree("""
        {
          "surveyYear": "2024",
          "ids": {
            "ber": "A1234"
          }
        }
        """)
    );
    assertThat(response).isEqualTo(DATA);
  }

  @Test
  void givenMissingParamForProductConfiguration_whenDataIsFetched_thenThrowException() {
    var params = Map.of(
        "bur", "A1234");
    var productConfigurationDto = DataProductProviderConfigurationDto.builder()
        .id(PRODUCT_ID)
        .restClientIdentifierCode("AGIS_STRUCTURE_V1")
        .restClientMethodCode("POST")
        .restClientPath("random-path")
        .restClientRequestTemplate("""
            {
              "surveyYear": "{{year}}",
              "ids": {
                "ber": "{{bur}}"
              }
            }
            """)
        .build();

    when(dataProductApi.getProviderConfigurationById(PRODUCT_ID)).thenReturn(productConfigurationDto);

    assertThatThrownBy(() -> dataFetchingService.fetchData(PRODUCT_ID, params))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Parameter 'year' not found");
    verifyNoInteractions(dataProviderRestClientFactory);
    verifyNoInteractions(agisStructureApiRestClient);
  }
}
