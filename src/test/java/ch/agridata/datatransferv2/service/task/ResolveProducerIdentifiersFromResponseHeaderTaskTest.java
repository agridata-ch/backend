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
class ResolveProducerIdentifiersFromResponseHeaderTaskTest {

  private static final String UID_HEADER = "AGRIDATA-RESPONSE-PRODUCER-UIDS";
  private static final String BUR_HEADER = "AGRIDATA-RESPONSE-PRODUCER-BURS";
  private static final String UID_1 = "CHE101000001";
  private static final String UID_2 = "CHE103000001";
  private static final String BUR_1 = "99910002";
  private static final String BUR_2 = "99910003";

  @InjectMocks
  ResolveProducerIdentifiersFromResponseHeaderTask task;

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
    assertThat(result.getProducerBurs()).isEmpty();
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

  static Stream<Arguments> missingOrEmptyHeaderCases() {
    return Stream.of(
        Arguments.of("no headers at all", Map.<String, String>of()),
        Arguments.of("both headers empty", Map.of(UID_HEADER, "", BUR_HEADER, "")),
        Arguments.of("both headers blank/whitespace only", Map.of(UID_HEADER, " , , ", BUR_HEADER, "  "))
    );
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("missingOrEmptyHeaderCases")
  void givenMissingOrEmptyHeaders_whenApply_thenExternalWebServiceExceptionThrown(String description, Map<String, String> headers) {
    var context = createContext(headers);

    assertThatThrownBy(() -> task.apply(context))
        .isInstanceOf(ExternalWebServiceException.class)
        .hasMessageContaining(UID_HEADER)
        .hasMessageContaining(BUR_HEADER);
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

  @Test
  void givenNonAlphanumericValueInBursHeader_whenApply_thenExternalWebServiceExceptionThrown() {
    var context = createContext(Map.of(BUR_HEADER, "999-10002"));

    assertThatThrownBy(() -> task.apply(context))
        .isInstanceOf(ExternalWebServiceException.class)
        .hasMessageContaining(BUR_HEADER)
        .hasMessageContaining("alphanumeric");
  }

  @Test
  void givenBothHeadersPopulated_whenApply_thenBothProducerIdentifiersResolved() {
    var context = createContext(Map.of(
        UID_HEADER, UID_1 + "," + UID_2,
        BUR_HEADER, BUR_1 + "," + BUR_2));

    var result = task.apply(context);

    assertThat(result.getProducerUids()).containsExactly(UID_1, UID_2);
    assertThat(result.getProducerBurs()).containsExactly(BUR_1, BUR_2);
  }

  @Test
  void givenEmptyUidsHeaderWithBursPresent_whenApply_thenBursResolved() {
    var context = createContext(Map.of(UID_HEADER, "", BUR_HEADER, BUR_1));

    var result = task.apply(context);

    assertThat(result.getProducerUids()).isEmpty();
    assertThat(result.getProducerBurs()).containsExactly(BUR_1);
  }

  private AgridataContext createContext(Map<String, String> responseHeaders) {
    return AgridataContext.builder()
        .productId(UUID.randomUUID())
        .flowEnum(FlowEnum.UNBOUND_POST_VALIDATION)
        .responseHeaders(responseHeaders)
        .build();
  }
}
