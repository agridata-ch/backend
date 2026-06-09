package ch.agridata.common.security.actingrole;

import jakarta.enterprise.context.RequestScoped;
import lombok.Getter;
import lombok.Setter;

/**
 * Request-scoped holder for the acting role resolved by {@link ActingRoleResolutionFilter}. The value is set
 * once per request by the filter and read by controllers
 *
 * @CommentLastReviewed 2026-05-20
 */
@RequestScoped
@Getter
@Setter
public class ActingRoleHolder {
  private ActingRoleEnum role;
}
