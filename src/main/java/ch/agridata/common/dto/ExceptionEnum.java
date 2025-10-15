package ch.agridata.common.dto;

/**
 * Lists generic exception categories across the system. It standardizes error reporting for internal and client-facing responses.
 *
 * @CommentLastReviewed 2025-08-25
 */
public enum ExceptionEnum {
  GENERIC,
  UID_MISSING,
  EXTERNAL_SERVICE_ERROR,
}
