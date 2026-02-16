package ch.agridata.common.openapi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a JAX-RS endpoint as belonging to one or more API subsets. The subsets are written into the OpenAPI document as
 * an {@code x-api-subset} extension by the {@code ApiSubsetOpenApiFilter} and can be used to generate filtered OpenAPI views.
 *
 * <p>Usage:
 * <pre>
 * &#64;ApiSubset({MOBILE_APP, WEB_APP})
 * </pre>
 *
 * @CommentLastReviewed 2026-02-16
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiSubset {
  String[] value();
}
