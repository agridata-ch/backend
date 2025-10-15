package ch.agridata.datatransfer.controller;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.agridata.datatransfer.service.DataTransferService;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.UriInfo;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DataTransferControllerTest {

  private static final UUID PRODUCT_ID = UUID.randomUUID();

  @Mock
  DataTransferService dataTransferService;
  @Mock
  UriInfo uriInfo;
  @InjectMocks
  DataTransferController dataTransferController;

  @Test
  void givenUidButNoBurGiven_whenDataTransferRequested_thenNoException() {
    when(uriInfo.getQueryParameters(true)).thenReturn(new MultivaluedHashMap<>());

    dataTransferController.dataTransfer(PRODUCT_ID, "CHE123456789", null, null, uriInfo);
    verify(dataTransferService, times(1)).transferDataByUid(eq(PRODUCT_ID), eq("CHE123456789"), any());
  }

  @Test
  void givenNoUidButBurGiven_whenDataTransferRequested_thenNoException() {
    when(uriInfo.getQueryParameters(true)).thenReturn(new MultivaluedHashMap<>());

    dataTransferController.dataTransfer(PRODUCT_ID, null, "A1234", null, uriInfo);
    verify(dataTransferService, times(1)).transferDataByBur(eq(PRODUCT_ID), eq("A1234"), any());
  }

  @Test
  void givenNoUidAndNoBurGiven_whenDataTransferRequested_thenThrowException() {
    assertThatThrownBy(() -> dataTransferController.dataTransfer(
        PRODUCT_ID, null, null, null, uriInfo))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Exactly one of uid or bur must be provided");
  }

  @Test
  void givenUidAndBurGiven_whenDataTransferRequested_thenThrowException() {
    assertThatThrownBy(() -> dataTransferController.dataTransfer(
        PRODUCT_ID, "CHE123456789", "A1234", null, uriInfo))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Exactly one of uid or bur must be provided");
  }

}
