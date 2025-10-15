package ch.agridata.datatransfer.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import ch.agridata.common.exceptions.DataTransferFailedException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DataProviderExceptionMapperTest {

  @Mock
  Response response;
  @InjectMocks
  DataProviderExceptionMapper mapper;

  @Test
  void toThrowable_shouldReturnExceptionWithBodyAndStatus() {
    // given
    when(response.getStatus()).thenReturn(400);
    when(response.readEntity(String.class)).thenReturn("error details");

    // when
    RuntimeException ex = mapper.toThrowable(response);

    // then
    assertThat(ex)
        .isInstanceOf(DataTransferFailedException.class)
        .hasMessageContaining("error details");
    assertThat(((DataTransferFailedException) ex).getStatus()).isEqualTo(400);
  }

  @Test
  void toThrowable_shouldReturnExceptionWithNoResponseBodyIfReadFails() {
    // given
    when(response.getStatus()).thenReturn(500);
    when(response.readEntity(String.class)).thenThrow(new RuntimeException("cannot read"));

    // when
    RuntimeException ex = mapper.toThrowable(response);

    // then
    assertThat(ex)
        .isInstanceOf(DataTransferFailedException.class)
        .hasMessageContaining("<no response body>");
    assertThat(((DataTransferFailedException) ex).getStatus()).isEqualTo(500);
  }
}
