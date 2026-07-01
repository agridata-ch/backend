package ch.agridata.aws.api;

/**
 * This Enum lists all possible GuardDutyScanResult-Tags plus a fallback option for newly added Tags
 *
 * @CommentLastReviewed 2026-07-09
 */

public enum GuardDutyScanResultEnum {
  NO_THREATS_FOUND,
  THREATS_FOUND,
  UNSUPPORTED,
  ACCESS_DENIED,
  FAILED,
  UNKNOWN
}
