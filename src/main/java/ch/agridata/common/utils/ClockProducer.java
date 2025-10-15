package ch.agridata.common.utils;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import java.time.Clock;

/**
 * Supplies a central clock instance. It enables consistent and testable time-based logic across services.
 *
 * @CommentLastReviewed 2025-08-25
 */

@ApplicationScoped
public class ClockProducer {

  @Produces
  public Clock produceClock() {
    return Clock.systemDefaultZone();
  }
}
