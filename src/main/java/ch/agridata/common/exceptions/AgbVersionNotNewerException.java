package ch.agridata.common.exceptions;

/**
 * Indicates that the version of a new AGB revision is not newer than the version of the current AGB revision.
 *
 * @CommentLastReviewed 2026-10-27
 */
public class AgbVersionNotNewerException extends RuntimeException {

  public AgbVersionNotNewerException(String message) {
    super(message);
  }
}
