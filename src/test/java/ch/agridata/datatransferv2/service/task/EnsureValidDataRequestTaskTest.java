package ch.agridata.datatransferv2.service.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import ch.agridata.agreement.api.DataRequestApi;
import ch.agridata.common.exceptions.ConsentNotGrantedException;
import ch.agridata.datatransferv2.service.AgridataContext;
import ch.agridata.datatransferv2.service.FlowEnum;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EnsureValidDataRequestTaskTest {

  private static final UUID PRODUCT_ID = UUID.randomUUID();
  private static final UUID DATA_REQUEST_ID_1 = UUID.randomUUID();
  private static final UUID DATA_REQUEST_ID_2 = UUID.randomUUID();
  private static final String CONSUMER_UID = "CHE123456789";

  @Mock
  DataRequestApi dataRequestApi;

  @InjectMocks
  EnsureValidDataRequestTask task;

  @Test
  void givenMatchingDataRequestIds_whenApply_thenIdsAreSet() {
    var context = createContext();

    when(dataRequestApi.getActiveDataRequestIdsForConsumerAndProduct(CONSUMER_UID, PRODUCT_ID))
        .thenReturn(List.of(DATA_REQUEST_ID_1, DATA_REQUEST_ID_2));

    var result = task.apply(context);

    assertThat(result.getValidDataRequestIds())
        .containsExactlyInAnyOrder(DATA_REQUEST_ID_1, DATA_REQUEST_ID_2);
  }

  @Test
  void givenNoMatchingDataRequests_whenApply_thenConsentNotGrantedExceptionIsThrown() {
    var context = createContext();

    when(dataRequestApi.getActiveDataRequestIdsForConsumerAndProduct(CONSUMER_UID, PRODUCT_ID))
        .thenReturn(List.of());

    assertThatThrownBy(() -> task.apply(context))
        .isInstanceOf(ConsentNotGrantedException.class)
        .hasMessageContaining("No active data request found");
  }

  private AgridataContext createContext() {
    return AgridataContext.builder()
        .productId(PRODUCT_ID)
        .flowEnum(FlowEnum.UID_BASED_PRE_VALIDATION)
        .consumerUid(CONSUMER_UID)
        .build();
  }
}
