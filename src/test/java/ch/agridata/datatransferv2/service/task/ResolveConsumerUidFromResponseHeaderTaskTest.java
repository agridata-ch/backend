package ch.agridata.datatransferv2.service.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.agridata.common.exceptions.ExternalWebServiceException;
import ch.agridata.datatransferv2.service.AgridataContext;
import ch.agridata.datatransferv2.service.FlowEnum;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResolveConsumerUidFromResponseHeaderTaskTest {

  private static final String CONSUMER_UID = "CHE123456789";

  @InjectMocks
  ResolveConsumerUidFromResponseHeaderTask task;

  @Test
  void givenConsumerUidInResponseHeader_whenApply_thenConsumerUidIsResolved() {
    var context = createContext(Map.of("AGRIDATA-CONSUMER-UID", CONSUMER_UID));

    var result = task.apply(context);

    assertThat(result.getConsumerUid()).isEqualTo(CONSUMER_UID);
  }

  @Test
  void givenNoConsumerUidInResponseHeader_whenApply_thenExternalWebServiceExceptionIsThrown() {
    var context = createContext(Map.of());

    assertThatThrownBy(() -> task.apply(context))
        .isInstanceOf(ExternalWebServiceException.class)
        .hasMessageContaining("AGRIDATA-CONSUMER-UID")
        .hasMessageContaining("absent in provider response");
  }

  private AgridataContext createContext(Map<String, String> responseHeaders) {
    return AgridataContext.builder()
        .productId(UUID.randomUUID())
        .flowEnum(FlowEnum.UID_BASED_POST_VALIDATION)
        .responseHeaders(responseHeaders)
        .build();
  }
}