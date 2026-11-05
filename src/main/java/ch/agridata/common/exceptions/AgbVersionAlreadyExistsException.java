package ch.agridata.common.exceptions;

/**
 * Indicates that an AGB revision with the given version number already exists.
 *
 * @CommentLastReviewed 2026-10-27
 */
public class AgbVersionAlreadyExistsException extends RuntimeException {

  public AgbVersionAlreadyExistsException(String message) {
    super(message);
  }
}
