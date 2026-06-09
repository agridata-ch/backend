package ch.agridata.common.security.actingrole;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a JAX-RS resource method as role-dispatched: the endpoint's behavior depends on which role the authenticated
 * user is acting as. The central {@code ActingRoleResolutionFilter} resolves the effective {@link ActingRoleEnum} from
 * the optional {@code actingRole} query parameter (or auto-resolves it for single-role users) and exposes it via
 * {@code ActingRoleHolder}.
 *
 * @CommentLastReviewed 2026-05-20
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableActingRoleHolder {
}
