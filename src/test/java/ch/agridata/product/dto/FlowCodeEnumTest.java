package ch.agridata.product.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class FlowCodeEnumTest {

  @ParameterizedTest
  @EnumSource(value = FlowCodeEnum.class, names = {
      "BUR_BASED_PRE_VALIDATION", "BUR_BASED_POST_VALIDATION", "UNBOUND_BUR_BASED_POST_VALIDATION"})
  void givenBurBasedFlowCode_whenIsBurBased_thenTrue(FlowCodeEnum flowCode) {
    assertThat(flowCode.isBurBased()).isTrue();
  }

  @ParameterizedTest
  @EnumSource(value = FlowCodeEnum.class, names = {
      "UID_BASED_PRE_VALIDATION", "UID_BASED_POST_VALIDATION", "UNBOUND_UID_BASED_POST_VALIDATION"})
  void givenUidBasedFlowCode_whenIsBurBased_thenFalse(FlowCodeEnum flowCode) {
    assertThat(flowCode.isBurBased()).isFalse();
  }
}
