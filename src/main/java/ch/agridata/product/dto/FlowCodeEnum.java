package ch.agridata.product.dto;

/**
 * Enumeration representing various flow codes used to categorize and define specific validation or processing workflows.
 *
 * @CommentLastReviewed 2026-08-17
 */

public enum FlowCodeEnum {
  UID_BASED_PRE_VALIDATION,
  UID_BASED_POST_VALIDATION,
  BUR_BASED_PRE_VALIDATION,
  BUR_BASED_POST_VALIDATION,
  UNBOUND_UID_BASED_POST_VALIDATION,
  UNBOUND_BUR_BASED_POST_VALIDATION;

  /**
   * Indicates whether the producer of this flow is identified by a BUR instead of a UID.
   */
  public boolean isBurBased() {
    return this == BUR_BASED_PRE_VALIDATION
        || this == BUR_BASED_POST_VALIDATION
        || this == UNBOUND_BUR_BASED_POST_VALIDATION;
  }
}
