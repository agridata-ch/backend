package ch.agridata.common.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.enterprise.context.ApplicationScoped;
import org.openapitools.jackson.nullable.JsonNullableModule;

/**
 * Registers the {@link JsonNullableModule} with the application {@code ObjectMapper}. Quarkus applies every CDI bean of type
 * {@link ObjectMapperCustomizer} to the {@code ObjectMapper} it produces, which teaches Jackson to (de)serialize
 * {@link org.openapitools.jackson.nullable.JsonNullable} properties &ndash; an absent JSON field deserializes to
 * {@code JsonNullable.undefined()} and, together with {@code @JsonInclude(NON_ABSENT)}, undefined values are omitted on serialization.
 *
 * <p>Registering the module through an {@link ObjectMapperCustomizer} rather than by producing a {@code Module} bean is deliberate:
 * Quarkus only auto-registers {@code Module} implementations discovered in the Jandex index, which does not cover the third-party
 * {@link JsonNullableModule} class nor a producer-method return type.
 *
 * @CommentLastReviewed 2026-09-09
 */
@ApplicationScoped
public class JsonNullableJacksonConfig implements ObjectMapperCustomizer {

  @Override
  public void customize(ObjectMapper objectMapper) {
    objectMapper.registerModule(new JsonNullableModule());
  }
}
