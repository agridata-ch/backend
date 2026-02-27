package ch.agridata.common.exceptions;

import java.util.Set;
import lombok.Getter;

/**
 * Indicates that a required consent has not been granted for a data transfer operation.
 * This exception is thrown when a consumer attempts to access data without valid consent
 * from the data producer or without an active data request.
 *
 * @CommentLastReviewed 2026-02-04
 */
@Getter
public class ConsentNotGrantedException extends RuntimeException {

  private final Set<String> producerIdentifiers;

  public ConsentNotGrantedException(Set<String> producerIdentifiers) {
    super("Consent not granted for the requested data producer(s): " + String.join(", ", producerIdentifiers));
    this.producerIdentifiers = producerIdentifiers;
  }

  public ConsentNotGrantedException(String message) {
    super(message);
    this.producerIdentifiers = Set.of();
  }
}
