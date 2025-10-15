package ch.agridata.agreement.service;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates pseudo-random integers for human-friendly, non-security-critical id generation. The use of ThreadLocalRandom is intentional and
 * safe in this context.
 *
 * @CommentLastReviewed 2025-08-25
 */
@ApplicationScoped
@SuppressWarnings("java:S2245") // see javadoc comment
public class HumanFriendlyIdRandomGenerator {
  public int nextInt(int bound) {
    return ThreadLocalRandom.current().nextInt(bound);
  }
}

