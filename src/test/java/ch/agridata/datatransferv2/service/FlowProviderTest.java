package ch.agridata.datatransferv2.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import ch.agridata.datatransferv2.service.flows.UidBasedPreValidationFlow;
import ch.agridata.product.api.DataProductApi;
import ch.agridata.product.dto.DataProductProviderConfigurationDto;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link FlowProvider}.
 *
 * @CommentLastReviewed 2026-09-11
 */
@ExtendWith(MockitoExtension.class)
class FlowProviderTest {

  private static final UUID PRODUCT_ID = UUID.randomUUID();

  @Mock
  DataProductApi dataProductApi;

  @Mock
  UidBasedPreValidationFlow uidBasedPreValidationFlow;

  @InjectMocks
  FlowProvider flowProvider;

  @ParameterizedTest
  @ValueSource(strings = {"restClient", "flowCode", "restClientMethodCode", "restClientPathTemplate"})
  void givenProductWithMissingTransferField_whenGetFlowByProduct_thenIllegalStateExceptionNamingTheField(String missingField) {
    when(dataProductApi.getProviderConfigurationById(PRODUCT_ID)).thenReturn(configWithout(missingField));

    assertThatThrownBy(() -> flowProvider.getFlowByProduct(PRODUCT_ID))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining(PRODUCT_ID.toString())
        .hasMessageContaining(missingField);
  }

  @Test
  void givenFullyConfiguredProduct_whenGetFlowByProduct_thenFlowResolvedFromFlowCode() {
    when(dataProductApi.getProviderConfigurationById(PRODUCT_ID)).thenReturn(fullConfig().build());

    var result = flowProvider.getFlowByProduct(PRODUCT_ID);

    assertThat(result.flow()).isSameAs(uidBasedPreValidationFlow);
    assertThat(result.productProviderConfiguration().restClientIdentifierCode()).isEqualTo("AGIS_API");
  }

  @Test
  void givenProductWithoutRequestTemplate_whenGetFlowByProduct_thenFlowResolved() {
    // The request template is optional: GET based products have no body.
    when(dataProductApi.getProviderConfigurationById(PRODUCT_ID))
        .thenReturn(fullConfig().restClientRequestTemplate(null).build());

    var result = flowProvider.getFlowByProduct(PRODUCT_ID);

    assertThat(result.flow()).isSameAs(uidBasedPreValidationFlow);
  }

  private DataProductProviderConfigurationDto configWithout(String missingField) {
    var config = fullConfig();
    switch (missingField) {
      case "restClient" -> config.restClientIdentifierCode(null);
      case "flowCode" -> config.flowCode(null);
      case "restClientMethodCode" -> config.restClientMethodCode(null);
      case "restClientPathTemplate" -> config.restClientPathTemplate(null);
      default -> throw new IllegalArgumentException("Unknown field: " + missingField);
    }
    return config.build();
  }

  private DataProductProviderConfigurationDto.DataProductProviderConfigurationDtoBuilder fullConfig() {
    return DataProductProviderConfigurationDto.builder()
        .id(PRODUCT_ID)
        .restClientIdentifierCode("AGIS_API")
        .flowCode("UID_BASED_PRE_VALIDATION")
        .restClientMethodCode("GET")
        .restClientPathTemplate("v1/animal/{{uid}}")
        .restClientRequestTemplate("{\"search\":{\"uid\":\"{{uid}}\"}}");
  }
}
