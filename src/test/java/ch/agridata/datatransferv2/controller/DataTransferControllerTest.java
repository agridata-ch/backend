package ch.agridata.datatransferv2.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.agridata.datatransferv2.service.DataTransferAuthorizationService;
import ch.agridata.datatransferv2.service.FlowProvider;
import ch.agridata.datatransferv2.service.FlowProvider.FlowWithProductProviderConfiguration;
import ch.agridata.datatransferv2.service.Flowable;
import ch.agridata.product.dto.DataProductProviderConfigurationDto;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link DataTransferController}.
 *
 * @CommentLastReviewed 2026-09-09
 */
@ExtendWith(MockitoExtension.class)
class DataTransferControllerTest {

  private static final UUID PRODUCT_ID = UUID.randomUUID();

  @Mock
  FlowProvider flowProvider;

  @Mock
  DataTransferAuthorizationService dataTransferAuthorizationService;

  @Mock
  Flowable flow;

  @Mock
  UriInfo uriInfo;

  @InjectMocks
  DataTransferController controller;

  @Test
  void givenProductWithRestClient_whenDataTransfer_thenTransferExecuted() {
    var configuration = configWithRestClient("AGIS_API");
    var expectedResponse = Response.ok().build();
    when(flowProvider.getFlowByProduct(PRODUCT_ID))
        .thenReturn(new FlowWithProductProviderConfiguration(flow, configuration));
    when(uriInfo.getQueryParameters(true)).thenReturn(new MultivaluedHashMap<>());
    when(flow.run(any(), any())).thenReturn(expectedResponse);

    var response = controller.dataTransfer(PRODUCT_ID, List.of(), uriInfo);

    assertThat(response).isSameAs(expectedResponse);
    verify(dataTransferAuthorizationService).enforceAuthorization(any());
    verify(flow).run(any(), any());
  }

  private DataProductProviderConfigurationDto configWithRestClient(String restClientIdentifierCode) {
    return DataProductProviderConfigurationDto.builder()
        .id(PRODUCT_ID)
        .restClientIdentifierCode(restClientIdentifierCode)
        .flowCode("UID_BASED_PRE_VALIDATION")
        .build();
  }
}
