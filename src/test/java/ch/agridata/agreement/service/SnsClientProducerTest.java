package ch.agridata.agreement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.SnsClientBuilder;

@ExtendWith(MockitoExtension.class)
class SnsClientProducerTest {
  @Mock
  private SnsClient snsClient;

  @InjectMocks
  private SnsClientProducer producer;

  @BeforeEach
  void setUp() {
    producer.awsRegion = "eu-central-2";
  }

  @Test
  @SuppressWarnings("resource")
  void whenSnsClientProduced_thenNotNull() {
    try (MockedStatic<SnsClient> mockedSns = mockStatic(SnsClient.class)) {
      SnsClientBuilder builderMock = mock(SnsClientBuilder.class, Answers.RETURNS_SELF);

      mockedSns.when(SnsClient::builder).thenReturn(builderMock);
      when(builderMock.build()).thenReturn(snsClient);

      SnsClient result = producer.snsClient();

      assertThat(result).isNotNull();
      assertThat(result).isEqualTo(snsClient);
      mockedSns.verify(SnsClient::builder);

      verify(builderMock).region(Region.of("eu-central-2"));
      verify(builderMock).credentialsProvider(any());
      verify(builderMock).build();
    }
  }

  @Test
  void whenSnsClientDisposed_thenClientIsClosed() {
    producer.close(snsClient);
    verify(snsClient).close();
  }
}
