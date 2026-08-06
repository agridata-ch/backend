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
class ResolveProducerBursFromResponseHeaderTaskTest {

  private static final String BUR_HEADER = "AGRIDATA-RESPONSE-PRODUCER-BURS";
  private static final String BUR_1 = "99910002";
  private static final String BUR_2 = "99910003";

  @InjectMocks
  ResolveProducerBursFromResponseHeaderTask task;

  static Stream<Arguments> csvParseCases() {
    return Stream.of(
        Arguments.of("single value", BUR_1, List.of(BUR_1)),
        Arguments.of("multiple values", BUR_1 + "," + BUR_2, List.of(BUR_1, BUR_2)),
        Arguments.of("surrounding whitespace is trimmed", "  " + BUR_1 + " , " + BUR_2 + "  ", List.of(BUR_1, BUR_2)),
        Arguments.of("blank entries are ignored", BUR_1 + ",, ," + BUR_2, List.of(BUR_1, BUR_2))
    );
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("csvParseCases")
  void givenCsvHeaderValue_whenApply_thenParsedAccordingly(String description, String headerValue, List<String> expected) {
    var context = createContext(Map.of(BUR_HEADER, headerValue));

    var result = task.apply(context);

    assertThat(result.getProducerBurs()).containsExactlyElementsOf(expected);
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "AGRIDATA-RESPONSE-PRODUCER-BURS",
      "agridata-response-producer-burs",
      "Agridata-Response-Producer-Burs"
  })
  void givenHeaderNameInVariousCases_whenApply_thenResolved(String headerName) {
    var context = createContext(Map.of(headerName, BUR_1));

    var result = task.apply(context);

    assertThat(result.getProducerBurs()).containsExactly(BUR_1);
  }

  @Test
  void givenBursHeaderMissing_whenApply_thenExternalWebServiceExceptionThrown() {
    var context = createContext(Map.of());

    assertThatThrownBy(() -> task.apply(context))
        .isInstanceOf(ExternalWebServiceException.class)
        .hasMessageContaining(BUR_HEADER);
  }

  @Test
  void givenBursHeaderPresentButEmpty_whenApply_thenProducerBursEmpty() {
    var context = createContext(Map.of(BUR_HEADER, ""));

    var result = task.apply(context);

    assertThat(result.getProducerBurs()).isEmpty();
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "999/10002",
      "999-10002",
      "999 10002",
      "[\"99910002\"]",
      "\"99910002\"",
      "99910002;99910003",
      "99910002,999-10003"
  })
  void givenNonAlphanumericValueInBursHeader_whenApply_thenExternalWebServiceExceptionThrown(String invalidValue) {
    var context = createContext(Map.of(BUR_HEADER, invalidValue));

    assertThatThrownBy(() -> task.apply(context))
        .isInstanceOf(ExternalWebServiceException.class)
        .hasMessageContaining(BUR_HEADER)
        .hasMessageContaining("alphanumeric");
  }

  private AgridataContext createContext(Map<String, String> responseHeaders) {
    return AgridataContext.builder()
        .productId(UUID.randomUUID())
        .flowEnum(FlowEnum.UNBOUND_BUR_BASED_POST_VALIDATION)
        .responseHeaders(responseHeaders)
        .build();
  }
}
