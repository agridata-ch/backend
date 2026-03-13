package ch.agridata.datatransferv2.service.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.agridata.common.exceptions.ExternalWebServiceException;
import ch.agridata.datatransferv2.service.AgridataContext;
import ch.agridata.datatransferv2.service.FlowEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResolveProducerIdentifiersFromResponseHeaderTaskTest {

  private static final String UID_HEADER = "AGRIDATA-RESPONSE-PRODUCER-UIDS";
  private static final String BUR_HEADER = "AGRIDATA-RESPONSE-PRODUCER-BURS";
  private static final String UID_1 = "CHE101000001";
  private static final String UID_2 = "CHE103000001";
  private static final String BUR_1 = "99910002";
  private static final String BUR_2 = "99910003";

  @Spy
  ObjectMapper objectMapper;

  @InjectMocks
  ResolveProducerIdentifiersFromResponseHeaderTask task;

  @Test
  void givenOnlyUidsHeader_whenApply_thenProducerUidsResolvedAndBursEmpty() {
    var context = createContext(Map.of(UID_HEADER, "[\"" + UID_1 + "\"]"));

    var result = task.apply(context);

    assertThat(result.getProducerUids()).containsExactly(UID_1);
    assertThat(result.getProducerBurs()).isEmpty();
  }

  @Test
  void givenOnlyBursHeader_whenApply_thenProducerBursResolvedAndUidsEmpty() {
    var context = createContext(Map.of(BUR_HEADER, "[\"" + BUR_1 + "\"]"));

    var result = task.apply(context);

    assertThat(result.getProducerUids()).isEmpty();
    assertThat(result.getProducerBurs()).containsExactly(BUR_1);
  }

  @Test
  void givenBothHeaders_whenApply_thenBothProducerIdentifiersResolved() {
    var context = createContext(Map.of(
        UID_HEADER, "[\"" + UID_1 + "\"]",
        BUR_HEADER, "[\"" + BUR_1 + "\"]"));

    var result = task.apply(context);

    assertThat(result.getProducerUids()).containsExactly(UID_1);
    assertThat(result.getProducerBurs()).containsExactly(BUR_1);
  }

  @Test
  void givenMultipleUidsAndBurs_whenApply_thenAllIdentifiersResolved() {
    var context = createContext(Map.of(
        UID_HEADER, "[\"" + UID_1 + "\", \"" + UID_2 + "\"]",
        BUR_HEADER, "[\"" + BUR_1 + "\", \"" + BUR_2 + "\"]"));

    var result = task.apply(context);

    assertThat(result.getProducerUids()).containsExactly(UID_1, UID_2);
    assertThat(result.getProducerBurs()).containsExactly(BUR_1, BUR_2);
  }

  @Test
  void givenNoHeaders_whenApply_thenExternalWebServiceExceptionThrown() {
    var context = createContext(Map.of());

    assertThatThrownBy(() -> task.apply(context))
        .isInstanceOf(ExternalWebServiceException.class)
        .hasMessageContaining(UID_HEADER)
        .hasMessageContaining(BUR_HEADER);
  }

  @Test
  void givenBothHeadersWithEmptyArrays_whenApply_thenExternalWebServiceExceptionThrown() {
    var context = createContext(Map.of(UID_HEADER, "[]", BUR_HEADER, "[]"));

    assertThatThrownBy(() -> task.apply(context))
        .isInstanceOf(ExternalWebServiceException.class)
        .hasMessageContaining(UID_HEADER)
        .hasMessageContaining(BUR_HEADER);
  }

  @Test
  void givenEmptyUidsHeaderWithBursPresent_whenApply_thenBursResolved() {
    var context = createContext(Map.of(UID_HEADER, "[]", BUR_HEADER, "[\"" + BUR_1 + "\"]"));

    var result = task.apply(context);

    assertThat(result.getProducerUids()).isEmpty();
    assertThat(result.getProducerBurs()).containsExactly(BUR_1);
  }

  @Test
  void givenInvalidJsonInUidsHeader_whenApply_thenExternalWebServiceExceptionThrown() {
    var context = createContext(Map.of(UID_HEADER, "not-valid-json"));

    assertThatThrownBy(() -> task.apply(context))
        .isInstanceOf(ExternalWebServiceException.class)
        .hasMessageContaining("producer UIDs");
  }

  @Test
  void givenInvalidJsonInBursHeader_whenApply_thenExternalWebServiceExceptionThrown() {
    var context = createContext(Map.of(BUR_HEADER, "not-valid-json"));

    assertThatThrownBy(() -> task.apply(context))
        .isInstanceOf(ExternalWebServiceException.class)
        .hasMessageContaining("producer BURs");
  }

  private AgridataContext createContext(Map<String, String> responseHeaders) {
    return AgridataContext.builder()
        .productId(UUID.randomUUID())
        .flowEnum(FlowEnum.UNBOUND_POST_VALIDATION)
        .responseHeaders(responseHeaders)
        .build();
  }
}