package ch.agridata.agreement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import ch.agridata.agreement.persistence.DataRequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HumanFriendlyIdServiceTest {

  @Mock
  private DataRequestRepository dataRequestRepository;
  @Mock
  private HumanFriendlyIdRandomGenerator randomGenerator;
  @InjectMocks
  private HumanFriendlyIdService humanFriendlyIdService;

  @Test
  void givenIdAlreadyTaken_whenGeneratingId_thenTryAgain() {
    // Arrange: Simulate two generated IDs – the first one is taken, the second one is available
    when(randomGenerator.nextInt(anyInt()))
        .thenReturn(0, 1, 0, 1) // results in ID 'AB23'
        .thenReturn(5, 2, 7, 7); // results in ID 'FC99'
    when(dataRequestRepository.existsByHumanFriendlyId("AB23")).thenReturn(true);
    when(dataRequestRepository.existsByHumanFriendlyId("FC99")).thenReturn(false);

    // Act
    String result = humanFriendlyIdService.getHumanFriendlyIdForDataRequest();

    // Assert
    assertThat(result).isEqualTo("FC99");
  }

}
