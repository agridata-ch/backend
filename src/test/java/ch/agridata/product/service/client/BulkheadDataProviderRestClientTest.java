package ch.agridata.product.service.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ch.agridata.common.exceptions.DataProviderUnavailableException;
import ch.agridata.product.api.DataProviderRestClient;
import ch.agridata.product.dto.RestClientIdentifier;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Response;
import java.net.ConnectException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;

class BulkheadDataProviderRestClientTest {

  private static final DataProviderRestClient.Headers HEADERS = DataProviderRestClient.Headers.builder().build();

  private final DataProviderRestClient delegate = mock(DataProviderRestClient.class);

  @Test
  void get_shouldRejectImmediatelyWhenNoPermitIsAvailable() {
    var client = new BulkheadDataProviderRestClient(RestClientIdentifier.AGIS_API, delegate, new Semaphore(0));

    assertThatThrownBy(() -> client.get("path", HEADERS))
        .isInstanceOf(DataProviderUnavailableException.class)
        .hasMessage("Maximum number of concurrent connections to data provider AGIS_API is exhausted");
    verifyNoInteractions(delegate);
  }

  @Test
  void get_shouldReleasePermitAfterResponse() {
    var permits = new Semaphore(1);
    var client = new BulkheadDataProviderRestClient(RestClientIdentifier.AGIS_API, delegate, permits);
    var response = mock(Response.class);
    when(delegate.get("path", HEADERS)).thenReturn(response);

    assertThat(client.get("path", HEADERS)).isSameAs(response);
    assertThat(client.get("path", HEADERS)).isSameAs(response);
    assertThat(permits.availablePermits()).isEqualTo(1);
  }

  @Test
  void post_shouldMapReadTimeoutAndReleasePermit() {
    var permits = new Semaphore(1);
    var client = new BulkheadDataProviderRestClient(RestClientIdentifier.TVD_ZO_API, delegate, permits);
    when(delegate.post("path", HEADERS, "body")).thenThrow(new ProcessingException(new TimeoutException("read timeout")));

    assertThatThrownBy(() -> client.post("path", HEADERS, "body"))
        .isInstanceOf(DataProviderUnavailableException.class)
        .hasMessage("Data provider TVD_ZO_API did not respond in time");
    assertThat(permits.availablePermits()).isEqualTo(1);
  }

  @Test
  void get_shouldMapConnectionFailure() {
    var client = new BulkheadDataProviderRestClient(RestClientIdentifier.AGIS_API, delegate, new Semaphore(1));
    when(delegate.get("path", HEADERS)).thenThrow(new ProcessingException(new ConnectException("Connection refused")));

    assertThatThrownBy(() -> client.get("path", HEADERS))
        .isInstanceOf(DataProviderUnavailableException.class)
        .hasMessage("Data provider AGIS_API is not reachable");
  }

  @Test
  void get_shouldRethrowOtherProcessingExceptions() {
    var client = new BulkheadDataProviderRestClient(RestClientIdentifier.AGIS_API, delegate, new Semaphore(1));
    when(delegate.get("path", HEADERS)).thenThrow(new ProcessingException("unexpected"));

    assertThatThrownBy(() -> client.get("path", HEADERS)).isExactlyInstanceOf(ProcessingException.class);
  }
}
