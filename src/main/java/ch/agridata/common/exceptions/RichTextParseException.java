package ch.agridata.common.exceptions;

/**
 * Indicates that a stored rich-text value is not well-formed XHTML and therefore cannot be parsed for
 * PDF rendering. Purposes are expected to be rich text produced by the front-end editor; a value that
 * is not well-formed is a data defect, not a bad request, and therefore results in a 500 response.
 *
 * @CommentLastReviewed 2026-09-23
 */
public class RichTextParseException extends RuntimeException {

  public RichTextParseException(String message) {
    super(message);
  }

  public RichTextParseException(String message, Throwable cause) {
    super(message, cause);
  }

}
