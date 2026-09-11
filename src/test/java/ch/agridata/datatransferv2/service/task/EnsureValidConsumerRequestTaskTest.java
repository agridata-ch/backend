package ch.agridata.datatransferv2.service.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.agridata.datatransferv2.service.AgridataContext;
import ch.agridata.datatransferv2.service.FlowEnum;
import ch.agridata.product.dto.DataProductProviderConfigurationDto;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class EnsureValidConsumerRequestTaskTest {

  private static final String VALID_UID = "CHE123456789";
  private static final String DATA_REQUEST_ID = "3fa85f64-5717-4562-b3fc-2c963f66afb7";

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
    var context = createContext(null, true);

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

  @Test
  void givenConsentFreeProductWithoutDataRequestId_whenApply_thenIllegalArgumentExceptionIsThrown() {
    var context = createContext(Map.of("uid", VALID_UID), false);

    assertThatThrownBy(() -> task.apply(context))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Missing required request parameters")
        .hasMessageContaining("dataRequestId");
  }

  @Test
  void givenConsentFreeProductWithDataRequestId_whenApply_thenContextIsReturned() {
    var context = createContext(Map.of("uid", VALID_UID, "dataRequestId", DATA_REQUEST_ID), false);

    var result = task.apply(context);

    assertThat(result).isSameAs(context);
  }

  @Test
  void givenConsentRequiredProductWithoutDataRequestId_whenApply_thenContextIsReturned() {
    var context = createContext(Map.of("uid", VALID_UID), true);

    var result = task.apply(context);

    assertThat(result).isSameAs(context);
  }

  private AgridataContext createContextWithParams(Map<String, String> params) {
    return createContext(params, true);
  }

  private AgridataContext createContext(Map<String, String> params, boolean consentRequired) {
    return AgridataContext.builder()
        .productId(UUID.randomUUID())
        .flowEnum(FlowEnum.UID_BASED_PRE_VALIDATION)
        .productProviderConfiguration(DataProductProviderConfigurationDto.builder()
            .id(UUID.randomUUID())
            .consentRequired(consentRequired)
            .build())
        .requestParameters(params)
        .build();
  }
}
