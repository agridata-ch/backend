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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link FlowProvider}.
 *
 * @CommentLastReviewed 2026-09-09
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

  @Test
  void givenProductWithoutRestClient_whenGetFlowByProduct_thenIllegalStateExceptionThrown() {
    when(dataProductApi.getProviderConfigurationById(PRODUCT_ID)).thenReturn(config(null));

    assertThatThrownBy(() -> flowProvider.getFlowByProduct(PRODUCT_ID))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining(PRODUCT_ID.toString())
        .hasMessageContaining("no rest client");
  }

  @Test
  void givenProductWithRestClient_whenGetFlowByProduct_thenFlowResolvedFromFlowCode() {
    when(dataProductApi.getProviderConfigurationById(PRODUCT_ID)).thenReturn(config("AGIS_API"));

    var result = flowProvider.getFlowByProduct(PRODUCT_ID);

    assertThat(result.flow()).isSameAs(uidBasedPreValidationFlow);
    assertThat(result.productProviderConfiguration().restClientIdentifierCode()).isEqualTo("AGIS_API");
  }

  private DataProductProviderConfigurationDto config(String restClientIdentifierCode) {
    return DataProductProviderConfigurationDto.builder()
        .id(PRODUCT_ID)
        .restClientIdentifierCode(restClientIdentifierCode)
        .flowCode("UID_BASED_PRE_VALIDATION")
        .build();
  }
}