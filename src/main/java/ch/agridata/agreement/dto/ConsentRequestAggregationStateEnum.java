package ch.agridata.agreement.dto;

/**
 * Represents the aggregated state of a group of consent requests belonging to the same data request. The state is derived from the
 * individual consent request states and may reflect fully consistent or partially mixed outcomes.
 *
 * @CommentLastReviewed 2026-02-04
 */

public enum ConsentRequestAggregationStateEnum {
  GRANTED,
  OPENED,
  DECLINED,
  PARTIALLY_GRANTED,
  PARTIALLY_OPENED
}
