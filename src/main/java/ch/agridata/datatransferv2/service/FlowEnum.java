package ch.agridata.datatransferv2.service;

import java.util.Set;
import lombok.Getter;

/**
 * Enumerates the available data transfer flow types and their required request parameters.
 *
 * @CommentLastReviewed 2026-06-01
 */
@Getter
public enum FlowEnum {
  UID_BASED_PRE_VALIDATION(Set.of("uid")),
  UID_BASED_POST_VALIDATION(Set.of("uid")),
  BUR_BASED_PRE_VALIDATION(Set.of("bur")),
  BUR_BASED_POST_VALIDATION(Set.of("bur")),
  UNBOUND_POST_VALIDATION(Set.of());

  private final Set<String> requiredRequestParameters;

  FlowEnum(Set<String> requiredRequestParameters) {
    this.requiredRequestParameters = requiredRequestParameters;
  }
}
