package ch.agridata.datatransferv2.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import ch.agridata.common.exceptions.DataProviderException;
import ch.agridata.datatransferv2.service.client.DataProviderExceptionMapper;
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
    when(response.getStatus()).thenReturn(400);
    when(response.readEntity(String.class)).thenReturn("error details");

    RuntimeException ex = mapper.toThrowable(response);

    assertThat(ex).isInstanceOf(DataProviderException.class);
    DataProviderException dpe = (DataProviderException) ex;
    assertThat(dpe.getDataProviderHttpStatus()).isEqualTo(400);
    assertThat(dpe.getDataProviderMessage()).isEqualTo("error details");
  }

  @Test
  void toThrowable_shouldReturnExceptionWithNoResponseBodyIfReadFails() {
    when(response.getStatus()).thenReturn(500);
    when(response.readEntity(String.class)).thenThrow(new RuntimeException("cannot read"));

    RuntimeException ex = mapper.toThrowable(response);

    assertThat(ex).isInstanceOf(DataProviderException.class);
    DataProviderException dpe = (DataProviderException) ex;
    assertThat(dpe.getDataProviderHttpStatus()).isEqualTo(500);
    assertThat(dpe.getDataProviderMessage()).isEqualTo("<no response body>");
  }
}
