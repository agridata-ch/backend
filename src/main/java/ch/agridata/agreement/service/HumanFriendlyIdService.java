package ch.agridata.agreement.service;

import ch.agridata.agreement.persistence.DataRequestRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

/**
 * Service for generating short, human-friendly IDs.
 *
 * <p>The format of the generated ID is: <strong>2 uppercase letters + 2 digits</strong>,
 * excluding visually confusing characters. Specifically:
 * <ul>
 *   <li>Allowed letters: A, B, C, D, E, F, G, H, J, K, M, N, P, Q, R, S, T, V, W, X (20 total)</li>
 *   <li>Allowed digits: 2–9 (8 total)</li>
 * </ul>
 * This results in a total of 25,600 unique possible codes (20 × 20 × 8 × 8).
 *
 * <p>To ensure uniqueness, the system attempts to generate a new code and checks
 * its existence against the database. If a collision is detected, it retries.
 * The number of retry attempts is capped at 100.
 *
 * <p><strong>Collision probability:</strong>
 * Due to the birthday paradox, the probability of a collision grows with the number
 * of generated codes. A 50% chance of a collision already occurs at around 188
 * generated codes. However, because up to 100 retries are allowed, the probability
 * of successfully finding a free code remains very high until the number of assigned
 * codes approaches the total capacity of 25,600.
 *
 * <p>For example:
 * <ul>
 *   <li>With 10,000 existing codes, the chance of all 100 retries failing is effectively zero.</li>
 *   <li>With 20,000 existing codes, the chance of all 100 retries failing is effectively zero.</li>
 *   <li>With 25,500 existing codes, there's only a ~33% chance of success in 100 attempts.</li>
 * </ul>
 *
 * <p>Therefore, the retry mechanism is robust for most use cases,
 * but near the capacity limit, a fallback strategy or code format extension may be required.
 *
 * @CommentLastReviewed 2025-08-25
 */
@ApplicationScoped
@RequiredArgsConstructor
public class HumanFriendlyIdService {

  private static final char[] ALLOWED_LETTERS = {
      'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
      'J', 'K', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'X'
  };
  private static final char[] ALLOWED_DIGITS = {
      '2', '3', '4', '5', '6', '7', '8', '9'
  };
  private static final int MAX_ATTEMPTS = 100;

  private final DataRequestRepository dataRequestRepository;
  private final HumanFriendlyIdRandomGenerator randomGenerator;

  public String getHumanFriendlyIdForDataRequest() {
    String candidate;

    for (int i = 0; i < MAX_ATTEMPTS; i++) {
      candidate = generateRandomCode();
      if (!dataRequestRepository.existsByHumanFriendlyId(candidate)) {
        return candidate;
      }
    }

    throw new IllegalStateException("Unable to generate unique human-friendly id for data request after " + MAX_ATTEMPTS + " attempts");
  }

  private String generateRandomCode() {
    char firstLetter = ALLOWED_LETTERS[randomGenerator.nextInt(ALLOWED_LETTERS.length)];
    char secondLetter = ALLOWED_LETTERS[randomGenerator.nextInt(ALLOWED_LETTERS.length)];
    char firstDigit = ALLOWED_DIGITS[randomGenerator.nextInt(ALLOWED_DIGITS.length)];
    char secondDigit = ALLOWED_DIGITS[randomGenerator.nextInt(ALLOWED_DIGITS.length)];

    return "" + firstLetter + secondLetter + firstDigit + secondDigit;
  }

}
