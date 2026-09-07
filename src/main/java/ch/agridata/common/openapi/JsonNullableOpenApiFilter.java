package ch.agridata.common.openapi;

import io.quarkus.smallrye.openapi.OpenApiFilter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.media.Schema;

/**
 * OAS filter that unwraps the {@code JsonNullable<T>} wrapper schemas that SmallRye generates for
 * {@code org.openapitools.jackson.nullable.JsonNullable} fields.
 *
 * <p>SmallRye scans {@link org.openapitools.jackson.nullable.JsonNullable}'s internal fields and therefore emits, for every
 * {@code JsonNullable<T>} property, a component schema of the shape {@code {"value": <T>, "isPresent": boolean}}. That object
 * shape leaks the Java wrapper into the API and produces misleading request examples such as
 * {@code {"value": …, "isPresent": true}}, whereas on the wire {@code JsonNullable} is transparent: a present value serializes
 * as the bare value and an absent one is omitted entirely.
 *
 * <p>This filter replaces each such wrapper component with its inner {@code value} schema. Every property already references the
 * wrapper via {@code $ref}, so those references stay valid and now resolve to the unwrapped type.
 *
 * <p>Scanning a {@code JsonNullable<T>} property as an object also makes SmallRye stamp a stray {@code type: object} onto the
 * <em>property</em>, alongside its {@code $ref}. For any non-object inner type that sibling contradicts the unwrapped type and
 * (under OpenAPI 3.1 {@code $ref} sibling semantics) wins, so Swagger renders the field as an empty object {@code {}} instead of
 * the real string/array/enum. The filter therefore also strips that stray {@code type} from every property whose {@code $ref}
 * points at a former wrapper component.
 *
 * <p><strong>One case this filter cannot cover:</strong> a scalar {@code JsonNullable<T>} field whose {@code @Schema} example is
 * JSON-shaped (starts with <code>{</code> or <code>[</code>). Because the property is scanned as an object, the generator parses
 * that example string into an object/array during scanning &ndash; before this BUILD-stage filter runs &ndash; and the filter
 * cannot re-coerce an already-parsed example back to a string. Such a field must additionally pin
 * {@code type = SchemaType.STRING} on its {@code @Schema} so the example is scanned as a string in the first place (see
 * {@code DataProductUpdateDto#restClientRequestTemplate}). Fields with plain, non-JSON examples need no override.
 *
 * @CommentLastReviewed 2026-09-07
 */
@OpenApiFilter(stages = {OpenApiFilter.RunStage.BUILD})
public class JsonNullableOpenApiFilter implements OASFilter {

  private static final String VALUE_PROPERTY = "value";
  private static final String IS_PRESENT_PROPERTY = "isPresent";
  private static final String SCHEMA_REF_PREFIX = "#/components/schemas/";

  @Override
  public void filterOpenAPI(OpenAPI openApi) {
    if (openApi.getComponents() == null) {
      return;
    }
    Map<String, Schema> schemas = openApi.getComponents().getSchemas();
    if (schemas == null) {
      return;
    }
    Set<String> wrapperRefs = new HashSet<>();
    Map<String, Schema> unwrappedSchemas = new LinkedHashMap<>(schemas.size());
    for (Map.Entry<String, Schema> entry : schemas.entrySet()) {
      Schema unwrapped = unwrapValue(entry.getValue());
      if (unwrapped != null) {
        wrapperRefs.add(SCHEMA_REF_PREFIX + entry.getKey());
        unwrappedSchemas.put(entry.getKey(), unwrapped);
      } else {
        unwrappedSchemas.put(entry.getKey(), entry.getValue());
      }
    }
    unwrappedSchemas.values().forEach(schema -> stripStrayWrapperTypes(schema, wrapperRefs));
    openApi.getComponents().setSchemas(unwrappedSchemas);
  }

  /**
   * Returns the inner {@code value} schema if the given schema is a {@code JsonNullable} wrapper (an object with exactly a
   * {@code value} and an {@code isPresent} property), otherwise {@code null}.
   */
  private Schema unwrapValue(Schema schema) {
    Map<String, Schema> properties = schema.getProperties();
    if (properties == null || !properties.keySet().equals(Set.of(VALUE_PROPERTY, IS_PRESENT_PROPERTY))) {
      return null;
    }
    return properties.get(VALUE_PROPERTY);
  }

  /**
   * Recursively clears the stray inline {@code type} on every subschema that references a former wrapper component, so the
   * {@code $ref} to the unwrapped type is the only thing describing the property. References are not followed (each referenced
   * component is visited as a top-level schema), which also bounds the recursion.
   */
  private void stripStrayWrapperTypes(Schema schema, Set<String> wrapperRefs) {
    if (schema == null) {
      return;
    }
    if (schema.getRef() != null && wrapperRefs.contains(schema.getRef())) {
      schema.setType(null);
    }
    if (schema.getProperties() != null) {
      schema.getProperties().values().forEach(child -> stripStrayWrapperTypes(child, wrapperRefs));
    }
    stripStrayWrapperTypes(schema.getItems(), wrapperRefs);
    if (schema.getAdditionalPropertiesSchema() != null) {
      stripStrayWrapperTypes(schema.getAdditionalPropertiesSchema(), wrapperRefs);
    }
    stripStrayWrapperTypesInList(schema.getAllOf(), wrapperRefs);
    stripStrayWrapperTypesInList(schema.getAnyOf(), wrapperRefs);
    stripStrayWrapperTypesInList(schema.getOneOf(), wrapperRefs);
  }

  private void stripStrayWrapperTypesInList(List<Schema> schemas, Set<String> wrapperRefs) {
    if (schemas == null) {
      return;
    }
    new ArrayList<>(schemas).forEach(child -> stripStrayWrapperTypes(child, wrapperRefs));
  }
}
