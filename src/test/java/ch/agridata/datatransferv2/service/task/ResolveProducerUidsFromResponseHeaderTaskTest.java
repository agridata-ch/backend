package ch.agridata.datatransferv2.service.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.agridata.common.exceptions.ExternalWebServiceException;
import ch.agridata.datatransferv2.service.AgridataContext;
import ch.agridata.datatransferv2.service.FlowEnum;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResolveProducerUidsFromResponseHeaderTaskTest {

  private static final String UID_HEADER = "AGRIDATA-RESPONSE-PRODUCER-UIDS";
  private static final String UID_1 = "CHE101000001";
  private static final String UID_2 = "CHE103000001";

  @InjectMocks
  ResolveProducerUidsFromResponseHeaderTask task;

  static Stream<Arguments> csvParseCases() {
    return Stream.of(
        Arguments.of("single value", UID_1, List.of(UID_1)),
        Arguments.of("multiple values", UID_1 + "," + UID_2, List.of(UID_1, UID_2)),
        Arguments.of("surrounding whitespace is trimmed", "  " + UID_1 + " , " + UID_2 + "  ", List.of(UID_1, UID_2)),
        Arguments.of("blank entries are ignored", UID_1 + ",, ," + UID_2, List.of(UID_1, UID_2))
    );
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("csvParseCases")
  void givenCsvHeaderValue_whenApply_thenParsedAccordingly(String description, String headerValue, List<String> expected) {
    var context = createContext(Map.of(UID_HEADER, headerValue));

    var result = task.apply(context);

    assertThat(result.getProducerUids()).containsExactlyElementsOf(expected);
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "AGRIDATA-RESPONSE-PRODUCER-UIDS",
      "agridata-response-producer-uids",
      "Agridata-Response-Producer-Uids"
  })
  void givenHeaderNameInVariousCases_whenApply_thenResolved(String headerName) {
    var context = createContext(Map.of(headerName, UID_1));

    var result = task.apply(context);

    assertThat(result.getProducerUids()).containsExactly(UID_1);
  }

  @Test
  void givenUidsHeaderMissing_whenApply_thenExternalWebServiceExceptionThrown() {
    var context = createContext(Map.of());

    assertThatThrownBy(() -> task.apply(context))
        .isInstanceOf(ExternalWebServiceException.class)
        .hasMessageContaining(UID_HEADER);
  }

  @Test
  void givenUidsHeaderPresentButEmpty_whenApply_thenProducerUidsEmpty() {
    var context = createContext(Map.of(UID_HEADER, ""));

    var result = task.apply(context);

    assertThat(result.getProducerUids()).isEmpty();
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "CHE/101000001",
      "CHE-101000001",
      "CHE 101000001",
      "[\"CHE101000001\"]",
      "\"CHE101000001\"",
      "CHE101000001;CHE103000001",
      "CHE101000001,CHE-103000001"
  })
  void givenNonAlphanumericValueInUidsHeader_whenApply_thenExternalWebServiceExceptionThrown(String invalidValue) {
    var context = createContext(Map.of(UID_HEADER, invalidValue));

    assertThatThrownBy(() -> task.apply(context))
        .isInstanceOf(ExternalWebServiceException.class)
        .hasMessageContaining(UID_HEADER)
        .hasMessageContaining("alphanumeric");
  }

  private AgridataContext createContext(Map<String, String> responseHeaders) {
    return AgridataContext.builder()
        .productId(UUID.randomUUID())
        .flowEnum(FlowEnum.UNBOUND_UID_BASED_POST_VALIDATION)
        .responseHeaders(responseHeaders)
        .build();
  }
}
