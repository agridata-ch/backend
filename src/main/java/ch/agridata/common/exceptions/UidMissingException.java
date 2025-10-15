package ch.agridata.common.exceptions;

/**
 * Indicates that a required UID is missing. It supports validation of user identity and access control.
 *
 * @CommentLastReviewed 2025-08-25
 */
public class UidMissingException extends RuntimeException {

  public UidMissingException(String message) {
    super(message);
  }

}
