package ch.agridata.common.exceptions;

/**
 * Indicates a failure to connect to the database. This exception is thrown when the application cannot establish a connection
 * to the database, which may be due to network issues, misconfiguration, or database server problems.
 *
 * @CommentLastReviewed 2025-08-25
 */
public class DatabaseConnectionException extends RuntimeException {

  public DatabaseConnectionException(String message, Throwable cause) {
    super(message, cause);
  }
}
