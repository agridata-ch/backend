package ch.agridata.datatransferv2.service.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.agridata.datatransferv2.service.AgridataContext;
import ch.agridata.datatransferv2.service.FlowEnum;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class EnsureValidConsumerRequestTaskTest {

  private static final String VALID_UID = "CHE123456789";

  private EnsureValidConsumerRequestTask task;

  @BeforeEach
  void setUp() {
    task = new EnsureValidConsumerRequestTask();
  }

  @Test
  void givenValidUidParameter_whenApply_thenContextIsReturned() {
    var context = createContextWithParams(Map.of("uid", VALID_UID));

    var result = task.apply(context);

    assertThat(result).isSameAs(context);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  void givenMissingOrBlankUid_whenApply_thenIllegalArgumentExceptionIsThrown(String uidValue) {
    var params = uidValue == null ? Map.<String, String>of() : Map.of("uid", uidValue);
    var context = createContextWithParams(params);

    assertThatThrownBy(() -> task.apply(context))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Missing required request parameters")
        .hasMessageContaining("uid");
  }

  @Test
  void givenNullRequestParameters_whenApply_thenIllegalArgumentExceptionIsThrown() {
    var context = AgridataContext.builder()
        .productId(UUID.randomUUID())
        .flowEnum(FlowEnum.UID_BASED_PRE_VALIDATION)
        .requestParameters(null)
        .build();

    assertThatThrownBy(() -> task.apply(context))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Missing required request parameters");
  }

  @Test
  void givenAdditionalParameters_whenApply_thenOnlyRequiredParametersAreValidated() {
    var context = createContextWithParams(Map.of(
        "uid", VALID_UID,
        "year", "2024",
        "extra", "value"
    ));

    var result = task.apply(context);

    assertThat(result).isSameAs(context);
  }

  private AgridataContext createContextWithParams(Map<String, String> params) {
    return AgridataContext.builder()
        .productId(UUID.randomUUID())
        .flowEnum(FlowEnum.UID_BASED_PRE_VALIDATION)
        .requestParameters(params)
        .build();
  }
}
