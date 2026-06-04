package ch.agridata.product.dto;

/**
 * Enumeration representing various flow codes used to categorize and define specific validation or processing workflows.
 *
 * @CommentLastReviewed 2026-06-11
 */

public enum FlowCodeEnum {
  UID_BASED_PRE_VALIDATION,
  UID_BASED_POST_VALIDATION,
  BUR_BASED_PRE_VALIDATION,
  BUR_BASED_POST_VALIDATION,
  UNBOUND_POST_VALIDATION
}
