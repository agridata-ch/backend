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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResolveConsumerUidFromResponseHeaderTaskTest {

  private static final String CONSUMER_UID_HEADER = "AGRIDATA-CONSUMER-UID";
  private static final String CONSUMER_UID = "CHE123456789";

  @InjectMocks
  ResolveConsumerUidFromResponseHeaderTask task;

  @ParameterizedTest
  @ValueSource(strings = {
      "AGRIDATA-CONSUMER-UID",
      "agridata-consumer-uid",
      "Agridata-Consumer-Uid"
  })
  void givenHeaderNameInVariousCases_whenApply_thenConsumerUidIsResolved(String headerName) {
    var context = createContext(Map.of(headerName, CONSUMER_UID));

    var result = task.apply(context);

    assertThat(result.getConsumerUid()).isEqualTo(CONSUMER_UID);
  }

  @Test
  void givenValueWithSurroundingWhitespace_whenApply_thenTrimmedConsumerUidIsResolved() {
    var context = createContext(Map.of(CONSUMER_UID_HEADER, "  " + CONSUMER_UID + "  "));

    var result = task.apply(context);

    assertThat(result.getConsumerUid()).isEqualTo(CONSUMER_UID);
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "   "})
  void givenAbsentOrBlankHeader_whenApply_thenExternalWebServiceExceptionThrown(String headerValue) {
    var context = createContext(Map.of(CONSUMER_UID_HEADER, headerValue));

    assertThatThrownBy(() -> task.apply(context))
        .isInstanceOf(ExternalWebServiceException.class)
        .hasMessageContaining(CONSUMER_UID_HEADER)
        .hasMessageContaining("absent in provider response");
  }

  @Test
  void givenNoConsumerUidInResponseHeader_whenApply_thenExternalWebServiceExceptionIsThrown() {
    var context = createContext(Map.of());

    assertThatThrownBy(() -> task.apply(context))
        .isInstanceOf(ExternalWebServiceException.class)
        .hasMessageContaining(CONSUMER_UID_HEADER)
        .hasMessageContaining("absent in provider response");
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "CHE-123456789",
      "CHE 123456789",
      "\"CHE123456789\"",
      "CHE123456789,CHE987654321",
      "CHE123456789;extra"
  })
  void givenNonAlphanumericConsumerUid_whenApply_thenExternalWebServiceExceptionThrown(String invalidValue) {
    var context = createContext(Map.of(CONSUMER_UID_HEADER, invalidValue));

    assertThatThrownBy(() -> task.apply(context))
        .isInstanceOf(ExternalWebServiceException.class)
        .hasMessageContaining(CONSUMER_UID_HEADER)
        .hasMessageContaining("alphanumeric");
  }

  private AgridataContext createContext(Map<String, String> responseHeaders) {
    return AgridataContext.builder()
        .productId(UUID.randomUUID())
        .flowEnum(FlowEnum.UID_BASED_POST_VALIDATION)
        .responseHeaders(responseHeaders)
        .build();
  }
}
