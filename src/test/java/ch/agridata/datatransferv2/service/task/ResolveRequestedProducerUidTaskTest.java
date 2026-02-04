package ch.agridata.datatransferv2.service.task;

import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.datatransferv2.service.AgridataContext;
import ch.agridata.datatransferv2.service.FlowEnum;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ResolveRequestedProducerUidTaskTest {

  private static final String PRODUCER_UID = "CHE987654321";

  private ResolveRequestedProducerUidTask task;

  @BeforeEach
  void setUp() {
    task = new ResolveRequestedProducerUidTask();
  }

  @Test
  void givenUidInRequestParameters_whenApply_thenProducerUidIsSet() {
    var context = createContextWithUid(PRODUCER_UID);

    var result = task.apply(context);

    assertThat(result.getProducerUidsInPayload()).containsExactly(PRODUCER_UID);
  }

  @Test
  void givenDifferentUid_whenApply_thenCorrectUidIsExtracted() {
    var differentUid = "CHE111222333";
    var context = createContextWithUid(differentUid);

    var result = task.apply(context);

    assertThat(result.getProducerUidsInPayload()).containsExactly(differentUid);
  }

  private AgridataContext createContextWithUid(String uid) {
    return AgridataContext.builder()
        .productId(UUID.randomUUID())
        .flowEnum(FlowEnum.UID_DIRECT)
        .requestParameters(Map.of("uid", uid))
        .build();
  }
}
