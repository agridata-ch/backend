package ch.agridata.agreement.service;

import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.DECLINED;
import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.GRANTED;
import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.OPENED;
import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.agridata.agreement.persistence.ConsentRequestEntity;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConsentRequestStateServiceTest {

  @InjectMocks
  private ConsentRequestStateService consentRequestStateService;

  static Stream<TransitionTestCase> transitionCases() {
    return Stream.of(
        // From OPENED to GRANTED or DECLINED is always allowed
        new TransitionTestCase(OPENED, GRANTED, null, true),
        new TransitionTestCase(OPENED, DECLINED, null, true),

        // Staying in the same state is not allowed
        new TransitionTestCase(OPENED, OPENED, null, false),
        new TransitionTestCase(GRANTED, GRANTED, now().minusSeconds(10), false),
        new TransitionTestCase(DECLINED, DECLINED, now().minusSeconds(10), false),

        // Switching between GRANTED and DECLINED is always allowed
        new TransitionTestCase(GRANTED, DECLINED, now().minusSeconds(10), true),
        new TransitionTestCase(DECLINED, GRANTED, now().minusSeconds(40), true),

        // Reverting to OPENED is allowed within 30 seconds
        new TransitionTestCase(GRANTED, OPENED, now().minusSeconds(5), true),
        new TransitionTestCase(DECLINED, OPENED, now().minusSeconds(29), true),

        // Reverting to OPENED is not allowed if time has passed
        new TransitionTestCase(GRANTED, OPENED, now().minusSeconds(31), false),
        new TransitionTestCase(DECLINED, OPENED, now().minusDays(5), false),

        // Invalid transition: null target
        new TransitionTestCase(DECLINED, null, now().minusSeconds(5), false)
    );
  }

  @ParameterizedTest(name = "[{index}] {0}")
  @MethodSource("transitionCases")
  void testValidateTransition(TransitionTestCase testCase) {
    boolean validationResult = true;
    try {
      consentRequestStateService.verifyStatusTransition(testCase.from, testCase.to, testCase.lastStateChangeDate);
    } catch (Exception e) {
      validationResult = false;
    }
    assertEquals(testCase.expectedAllowed(), validationResult, () ->
        String.format("Expected state transition: %s  to be %s", testCase,
            testCase.expectedAllowed())
    );
  }

  record TransitionTestCase(
      ConsentRequestEntity.StateEnum from,
      ConsentRequestEntity.StateEnum to,
      LocalDateTime lastStateChangeDate,
      boolean expectedAllowed
  ) {
  }

}
