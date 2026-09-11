package ch.agridata.common.openapi;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.junit.jupiter.api.Test;

class JsonNullableOpenApiFilterTest {

  private static final String STRING_WRAPPER = "JsonNullableString";
  private static final String STRING_WRAPPER_REF = "#/components/schemas/" + STRING_WRAPPER;

  private final JsonNullableOpenApiFilter filter = new JsonNullableOpenApiFilter();

  private Schema wrapperOf(Schema value) {
    return OASFactory.createSchema()
        .addType(Schema.SchemaType.OBJECT)
        .addProperty("value", value)
        .addProperty("isPresent", OASFactory.createSchema().addType(Schema.SchemaType.BOOLEAN));
  }

  private Schema refWithStrayType(String ref) {
    return OASFactory.createSchema().ref(ref).addType(Schema.SchemaType.OBJECT);
  }

  private OpenAPI openApiWith(String dtoProperty, Schema dtoPropertySchema) {
    Schema dto = OASFactory.createSchema()
        .addType(Schema.SchemaType.OBJECT)
        .addProperty(dtoProperty, dtoPropertySchema);
    return OASFactory.createOpenAPI().components(OASFactory.createComponents()
        .addSchema(STRING_WRAPPER, wrapperOf(OASFactory.createSchema().addType(Schema.SchemaType.STRING)))
        .addSchema("Dto", dto));
  }

  @Test
  void wrapper_component_is_replaced_by_its_inner_value_schema() {
    OpenAPI openApi = openApiWith("field", refWithStrayType(STRING_WRAPPER_REF));

    filter.filterOpenAPI(openApi);

    Schema replaced = openApi.getComponents().getSchemas().get(STRING_WRAPPER);
    assertThat(replaced.getType()).containsExactly(Schema.SchemaType.STRING);
    assertThat(replaced.getProperties()).isNullOrEmpty();
  }

  @Test
  void stray_type_is_stripped_from_property_referencing_a_wrapper() {
    OpenAPI openApi = openApiWith("field", refWithStrayType(STRING_WRAPPER_REF));

    filter.filterOpenAPI(openApi);

    Schema field = openApi.getComponents().getSchemas().get("Dto").getProperties().get("field");
    assertThat(field.getType()).isNull();
    assertThat(field.getRef()).isEqualTo(STRING_WRAPPER_REF);
  }

  @Test
  void stray_type_is_stripped_recursively_inside_array_items() {
    Schema arrayProperty = OASFactory.createSchema()
        .addType(Schema.SchemaType.ARRAY)
        .items(refWithStrayType(STRING_WRAPPER_REF));
    OpenAPI openApi = openApiWith("list", arrayProperty);

    filter.filterOpenAPI(openApi);

    Schema items = openApi.getComponents().getSchemas().get("Dto").getProperties().get("list").getItems();
    assertThat(items.getType()).isNull();
    assertThat(items.getRef()).isEqualTo(STRING_WRAPPER_REF);
  }

  @Test
  void property_referencing_a_non_wrapper_component_keeps_its_type() {
    Schema ref = OASFactory.createSchema()
        .ref("#/components/schemas/SomeDto")
        .addType(Schema.SchemaType.OBJECT);
    OpenAPI openApi = openApiWith("nested", ref);

    filter.filterOpenAPI(openApi);

    Schema nested = openApi.getComponents().getSchemas().get("Dto").getProperties().get("nested");
    assertThat(nested.getType()).containsExactly(Schema.SchemaType.OBJECT);
  }

  @Test
  void schema_with_only_value_property_is_not_treated_as_a_wrapper() {
    Schema notAWrapper = OASFactory.createSchema()
        .addType(Schema.SchemaType.OBJECT)
        .addProperty("value", OASFactory.createSchema().addType(Schema.SchemaType.STRING));
    OpenAPI openApi = OASFactory.createOpenAPI().components(OASFactory.createComponents()
        .addSchema("NotAWrapper", notAWrapper));

    filter.filterOpenAPI(openApi);

    Schema unchanged = openApi.getComponents().getSchemas().get("NotAWrapper");
    assertThat(unchanged.getType()).containsExactly(Schema.SchemaType.OBJECT);
    assertThat(unchanged.getProperties()).containsKey("value");
  }

  @Test
  void openapi_without_components_is_left_untouched() {
    OpenAPI openApi = OASFactory.createOpenAPI();

    filter.filterOpenAPI(openApi);

    assertThat(openApi.getComponents()).isNull();
  }
}
