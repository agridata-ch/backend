package integration.agis;

import static org.mockito.Mockito.verify;

import ch.agridata.agis.dto.AgisRegisterDataRequest;
import ch.agridata.agis.service.AgisRegisterApiRestClient;
import ch.agridata.common.jsonfieldrewrite.JsonFieldRewriteInboundFilter;
import ch.agridata.common.jsonfieldrewrite.JsonFieldRewriteOutboundFilter;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
@RequiredArgsConstructor
class AgisJsonFieldRewriteFilterTest {

  @RestClient
  AgisRegisterApiRestClient restClient;
  @InjectMock
  JsonFieldRewriteInboundFilter jsonFieldRewriteInboundFilter;
  @InjectMock
  JsonFieldRewriteOutboundFilter jsonFieldRewriteOutboundFilter;

  @Test
  void testJsonFieldRewriteFilterIsInvoked() throws IOException {
    AgisRegisterDataRequest request = new AgisRegisterDataRequest();

    try {
      restClient.register(request);
    } catch (Exception ignored) {
      // Exception is ignored – the goal is only to verify that the filter is invoked
    }

    verify(jsonFieldRewriteInboundFilter, Mockito.times(1)).filter(Mockito.any(), Mockito.any());
    verify(jsonFieldRewriteOutboundFilter, Mockito.times(1)).filter(Mockito.any());
  }
}
