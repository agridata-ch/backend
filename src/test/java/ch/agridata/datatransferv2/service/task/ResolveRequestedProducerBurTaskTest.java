package ch.agridata.datatransferv2.service.task;

import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.datatransferv2.service.AgridataContext;
import ch.agridata.datatransferv2.service.FlowEnum;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ResolveRequestedProducerBurTaskTest {

  private static final String PRODUCER_BUR = "A123456";

  private ResolveRequestedProducerBurTask task;

  @BeforeEach
  void setUp() {
    task = new ResolveRequestedProducerBurTask();
  }

  @Test
  void givenBurInRequestParameters_whenApply_thenProducerBurIsSet() {
    var context = createContextWithBur(PRODUCER_BUR);

    var result = task.apply(context);

    assertThat(result.getProducerBurs()).containsExactly(PRODUCER_BUR);
  }

  @Test
  void givenDifferentBur_whenApply_thenCorrectBurIsExtracted() {
    var differentBur = "B987654";
    var context = createContextWithBur(differentBur);

    var result = task.apply(context);

    assertThat(result.getProducerBurs()).containsExactly(differentBur);
  }

  private AgridataContext createContextWithBur(String bur) {
    return AgridataContext.builder()
        .productId(UUID.randomUUID())
        .flowEnum(FlowEnum.BUR_BASED_POST_VALIDATION)
        .requestParameters(Map.of("bur", bur))
        .build();
  }
}
