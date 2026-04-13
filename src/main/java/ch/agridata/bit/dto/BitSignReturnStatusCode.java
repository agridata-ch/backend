package ch.agridata.bit.dto;

/**
 * Status codes returned by the BIT evidence Signing API for all operations.
 *
 * @CommentLastReviewed 2026-04-09
 */
public enum BitSignReturnStatusCode {
  OK,
  ERROR,
  KEY_BEARER_NOT_EXISTS,
  PROFILE_NOT_FOUND,
  SIGN_PROCESS_NOT_EXISTS,
  PDF_INVALID,
  ALREADY_STARTED,
  DOES_NOT_MATCH_TO_PROFILE,
  INVALID_STATE,
  NO_METATAG,
  INVALID_METATAG,
  MAX_LIMIT_OF_DOCUMENTS,
  ADMIN_GLOBAL_ID_NOT_SET,
  CALLBACKURL_NOTALLOWED
}
