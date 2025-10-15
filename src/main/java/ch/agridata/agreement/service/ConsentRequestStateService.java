package ch.agridata.agreement.service;

import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.DECLINED;
import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.GRANTED;
import static ch.agridata.agreement.persistence.ConsentRequestEntity.StateEnum.OPENED;

import ch.agridata.agreement.persistence.ConsentRequestEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

/**
 * Manages state transitions of consent requests and enforces transition rules. Handles validation and submission logic related to changing
 * request states.
 *
 * @CommentLastReviewed 2025-09-26
 */

@ApplicationScoped
@RequiredArgsConstructor
public class ConsentRequestStateService {

  private static final Map<Transition, Rule> ALLOWED_TRANSITIONS = Map.of(
      new Transition(null, OPENED), Rule.allow(),
      new Transition(null, GRANTED), Rule.allow(),
      new Transition(null, DECLINED), Rule.allow(),
      new Transition(OPENED, GRANTED), Rule.allow(),
      new Transition(OPENED, DECLINED), Rule.allow(),
      new Transition(GRANTED, DECLINED), Rule.allow(),
      new Transition(GRANTED, OPENED), Rule.allowWithinSeconds(30),
      new Transition(DECLINED, GRANTED), Rule.allow(),
      new Transition(DECLINED, OPENED), Rule.allowWithinSeconds(30)
  );

  public void verifyStatusTransition(ConsentRequestEntity.StateEnum from,
                                     ConsentRequestEntity.StateEnum to,
                                     LocalDateTime lastStateChangeDate) {
    Rule rule = Optional.ofNullable(ALLOWED_TRANSITIONS.get(new Transition(from, to)))
        .orElseThrow(() -> new ValidationException("invalid transition from " + from + " to " + to));

    if (rule.maxAgeSeconds == null || lastStateChangeDate == null) {
      return;
    }
    var lastAcceptableChangeDate = LocalDateTime.now().minusSeconds(rule.maxAgeSeconds);
    if (lastStateChangeDate.isBefore(lastAcceptableChangeDate)) {
      throw new ValidationException(
          String.format("unable to transition from %s to %s. LastStateChangeDate '%s' was too long ago. LastAcceptableChangeDate was '%s'",
              from, to, lastStateChangeDate, lastAcceptableChangeDate));
    }
  }

  private record Transition(ConsentRequestEntity.StateEnum from, ConsentRequestEntity.StateEnum to) {
  }

  private record Rule(Integer maxAgeSeconds) {
    static Rule allow() {
      return new Rule(null);
    }

    // enables revert functionality
    static Rule allowWithinSeconds(int seconds) {
      return new Rule(seconds);
    }
  }
}
