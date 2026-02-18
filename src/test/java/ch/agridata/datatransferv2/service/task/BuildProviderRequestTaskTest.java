package ch.agridata.datatransferv2.service.task;

import static ch.agridata.datatransferv2.client.DataProviderRestClientProvider.RestClientIdentifier.AGIS_API;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ch.agridata.datatransferv2.client.DataProviderRestClient;
import ch.agridata.datatransferv2.client.DataProviderRestClientProvider;
import ch.agridata.datatransferv2.service.AgridataContext;
import ch.agridata.datatransferv2.service.FlowEnum;
import ch.agridata.product.api.DataProductApi;
import ch.agridata.product.dto.DataProductProviderConfigurationDto;
import jakarta.ws.rs.core.Response;
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
class BuildProviderRequestTaskTest {

  private static final UUID PRODUCT_ID = UUID.randomUUID();
  private static final String CONSUMER_UID = "CHE123456789";
  private static final String CONSUMER_AGATE_LOGIN_ID = "12345678";

  @Mock
  DataProviderRestClientProvider dataProviderRestClientProvider;

  @Mock
  DataProductApi dataProductApi;

  @Mock
  DataProviderRestClient dataProviderRestClient;

  @Captor
  ArgumentCaptor<DataProviderRestClient.Headers> headersCaptor;

  @InjectMocks
  BuildProviderRequestTask task;

  @Test
  void givenPostMethod_whenApply_thenProviderRequestIsBuiltForPost() {
    var context = createContext();
    var productConfig = createProductConfig("POST", "/api/data", "{\"uid\": \"{{uid}}\"}");

    when(dataProductApi.getProviderConfigurationById(PRODUCT_ID)).thenReturn(productConfig);
    when(dataProviderRestClientProvider.get(AGIS_API)).thenReturn(dataProviderRestClient);
    when(dataProviderRestClient.post(any(), any(), any())).thenReturn(mock(Response.class));

    var result = task.apply(context);

    assertThat(result.getProviderRequest()).isNotNull();

    // Execute the supplier to verify it's configured correctly
    result.getProviderRequest().get();

    verify(dataProviderRestClient).post(eq("/api/data"), headersCaptor.capture(), eq("{\"uid\": \"CHE987654321\"}"));
    assertThat(headersCaptor.getValue().agridataConsumerUid).isEqualTo(CONSUMER_UID);
    assertThat(headersCaptor.getValue().agridataConsumerAgateLoginId).isEqualTo(CONSUMER_AGATE_LOGIN_ID);
  }

  @Test
  void givenGetMethod_whenApply_thenProviderRequestIsBuiltForGet() {
    var context = createContext();
    var productConfig = createProductConfig("GET", "/api/data/{{uid}}", null);

    when(dataProductApi.getProviderConfigurationById(PRODUCT_ID)).thenReturn(productConfig);
    when(dataProviderRestClientProvider.get(AGIS_API)).thenReturn(dataProviderRestClient);
    when(dataProviderRestClient.get(any(), any())).thenReturn(mock(Response.class));

    var result = task.apply(context);

    assertThat(result.getProviderRequest()).isNotNull();

    // Execute the supplier
    result.getProviderRequest().get();

    verify(dataProviderRestClient).get(eq("/api/data/CHE987654321"), any());
  }

  @Test
  void givenUnsupportedMethod_whenApply_thenIllegalArgumentExceptionIsThrown() {
    var context = createContext();
    var productConfig = createProductConfig("DELETE", "/api/data", null);

    when(dataProductApi.getProviderConfigurationById(PRODUCT_ID)).thenReturn(productConfig);
    when(dataProviderRestClientProvider.get(AGIS_API)).thenReturn(dataProviderRestClient);

    // The exception is thrown when building the supplier (during apply), not when executing it
    assertThatThrownBy(() -> task.apply(context))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Unsupported rest client method: DELETE");
  }

  @Test
  void givenMissingPlaceholder_whenApply_thenIllegalArgumentExceptionIsThrown() {
    var context = createContext();
    var productConfig = createProductConfig("POST", "/api/data", "{\"missing\": \"{{notFound}}\"}");

    when(dataProductApi.getProviderConfigurationById(PRODUCT_ID)).thenReturn(productConfig);

    assertThatThrownBy(() -> task.apply(context))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Parameter 'notFound' not found");

    verifyNoInteractions(dataProviderRestClientProvider);
  }

  @Test
  void givenNullTemplate_whenApply_thenNullBodyIsUsed() {
    var context = createContext();
    var productConfig = createProductConfig("POST", "/api/data", null);

    when(dataProductApi.getProviderConfigurationById(PRODUCT_ID)).thenReturn(productConfig);
    when(dataProviderRestClientProvider.get(AGIS_API)).thenReturn(dataProviderRestClient);
    when(dataProviderRestClient.post(any(), any(), any())).thenReturn(mock(Response.class));

    var result = task.apply(context);
    result.getProviderRequest().get();

    verify(dataProviderRestClient).post(eq("/api/data"), any(), eq(null));
  }

  private AgridataContext createContext() {
    return AgridataContext.builder()
        .productId(PRODUCT_ID)
        .flowEnum(FlowEnum.UID_BASED_PRE_VALIDATION)
        .consumerUid(CONSUMER_UID)
        .consumerAgateLoginId(CONSUMER_AGATE_LOGIN_ID)
        .requestParameters(Map.of("uid", "CHE987654321"))
        .build();
  }

  private DataProductProviderConfigurationDto createProductConfig(String method, String path, String template) {
    return DataProductProviderConfigurationDto.builder()
        .id(PRODUCT_ID)
        .restClientIdentifierCode("AGIS_API")
        .restClientMethodCode(method)
        .restClientPath(path)
        .restClientRequestTemplate(template)
        .build();
  }
}
