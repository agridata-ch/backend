package ch.agridata.common.validation;

import jakarta.inject.Singleton;
import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.UnwrapByDefault;
import jakarta.validation.valueextraction.ValueExtractor;
import org.openapitools.jackson.nullable.JsonNullable;

/**
 * Bean Validation {@link ValueExtractor} for {@link JsonNullable}. It makes constraints declared on a {@code JsonNullable} property apply
 * to the <em>wrapped</em> value: a present value (including an explicit {@code null}) is handed to the constraints, while an absent
 * ({@link JsonNullable#isPresent()} {@code == false}) value is not extracted at all, so every constraint &ndash; including
 * {@code @NotNull} and {@code @Null} &ndash; is skipped for an omitted field. Annotated {@link UnwrapByDefault} so field constraints
 * unwrap without an explicit {@code @Unwrapping}. Registered as a CDI bean, which is how Quarkus's Hibernate Validator integration
 * discovers value extractors; the library's own extractor ships only via {@code META-INF/services} (JDK {@code ServiceLoader}), which
 * Quarkus does not consult, hence this project-local copy. The same CDI bean also backs the injected {@link jakarta.validation.Validator}
 * used by the standalone schema generator.
 *
 * <p>See the comment on {@link Singleton} for why this bean uses a pseudo-scope rather than {@code @ApplicationScoped}.
 *
 * @CommentLastReviewed 2026-09-09
 */
// @Singleton (pseudo-scope), not @ApplicationScoped: Hibernate Validator reads @UnwrapByDefault off the concrete class, but a normal
// scope would hand it a client-proxy subclass that loses the non-@Inherited annotation, breaking unwrapping (HV000030). See class Javadoc.
@Singleton
@UnwrapByDefault
public class JsonNullableValueExtractor implements ValueExtractor<JsonNullable<@ExtractedValue ?>> {

  @Override
  public void extractValues(JsonNullable<?> originalValue, ValueReceiver receiver) {
    if (originalValue.isPresent()) {
      receiver.value(null, originalValue.get());
    }
  }
}
