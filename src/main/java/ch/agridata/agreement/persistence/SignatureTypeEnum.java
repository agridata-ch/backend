package ch.agridata.agreement.persistence;

/**
 * Lists the possible signature types that will be applied to the contract revision. Every party of a contract needs to either
 * have the contract be signed by one or two people.
 *
 * @CommentLastReviewed: 2026-04-23
 */
public enum SignatureTypeEnum {
  INDIVIDUAL_SIGNATURE,
  COLLECTIVE_SIGNATURE
}
