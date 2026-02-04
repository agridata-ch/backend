package ch.agridata.datatransferv2.service.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import ch.agridata.agreement.api.DataRequestApi;
import ch.agridata.agreement.dto.DataRequestDto;
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
  private static final UUID OTHER_PRODUCT_ID = UUID.randomUUID();
  private static final UUID DATA_REQUEST_ID_1 = UUID.randomUUID();
  private static final UUID DATA_REQUEST_ID_2 = UUID.randomUUID();
  private static final String CONSUMER_UID = "CHE123456789";

  @Mock
  DataRequestApi dataRequestApi;

  @InjectMocks
  EnsureValidDataRequestTask task;

  @Test
  void givenActiveDataRequestWithProduct_whenApply_thenDataRequestIdIsSet() {
    var context = createContext();
    var dataRequest = createDataRequest(DATA_REQUEST_ID_1, List.of(PRODUCT_ID));

    when(dataRequestApi.getActiveDataRequestsOfConsumer(CONSUMER_UID))
        .thenReturn(List.of(dataRequest));

    var result = task.apply(context);

    assertThat(result.getValidDataRequestIds()).containsExactly(DATA_REQUEST_ID_1);
  }

  @Test
  void givenMultipleMatchingDataRequests_whenApply_thenAllIdsAreReturned() {
    var context = createContext();
    var dataRequest1 = createDataRequest(DATA_REQUEST_ID_1, List.of(PRODUCT_ID));
    var dataRequest2 = createDataRequest(DATA_REQUEST_ID_2, List.of(PRODUCT_ID, OTHER_PRODUCT_ID));

    when(dataRequestApi.getActiveDataRequestsOfConsumer(CONSUMER_UID))
        .thenReturn(List.of(dataRequest1, dataRequest2));

    var result = task.apply(context);

    assertThat(result.getValidDataRequestIds())
        .containsExactlyInAnyOrder(DATA_REQUEST_ID_1, DATA_REQUEST_ID_2);
  }

  @Test
  void givenNoDataRequests_whenApply_thenConsentNotGrantedExceptionIsThrown() {
    var context = createContext();

    when(dataRequestApi.getActiveDataRequestsOfConsumer(CONSUMER_UID))
        .thenReturn(List.of());

    assertThatThrownBy(() -> task.apply(context))
        .isInstanceOf(ConsentNotGrantedException.class)
        .hasMessageContaining("No active data request found");
  }

  @Test
  void givenDataRequestWithoutMatchingProduct_whenApply_thenConsentNotGrantedExceptionIsThrown() {
    var context = createContext();
    var dataRequest = createDataRequest(DATA_REQUEST_ID_1, List.of(OTHER_PRODUCT_ID));

    when(dataRequestApi.getActiveDataRequestsOfConsumer(CONSUMER_UID))
        .thenReturn(List.of(dataRequest));

    assertThatThrownBy(() -> task.apply(context))
        .isInstanceOf(ConsentNotGrantedException.class)
        .hasMessageContaining("No active data request found");
  }

  @Test
  void givenDataRequestWithNullProducts_whenApply_thenDataRequestIsFiltered() {
    var context = createContext();
    var dataRequestWithNullProducts = createDataRequest(DATA_REQUEST_ID_1, null);
    var dataRequestWithProduct = createDataRequest(DATA_REQUEST_ID_2, List.of(PRODUCT_ID));

    when(dataRequestApi.getActiveDataRequestsOfConsumer(CONSUMER_UID))
        .thenReturn(List.of(dataRequestWithNullProducts, dataRequestWithProduct));

    var result = task.apply(context);

    assertThat(result.getValidDataRequestIds()).containsExactly(DATA_REQUEST_ID_2);
  }

  private AgridataContext createContext() {
    return AgridataContext.builder()
        .productId(PRODUCT_ID)
        .flowEnum(FlowEnum.UID_DIRECT)
        .consumerUids(List.of(CONSUMER_UID))
        .build();
  }

  private DataRequestDto createDataRequest(UUID id, List<UUID> products) {
    return DataRequestDto.builder()
        .id(id)
        .products(products)
        .build();
  }
}
