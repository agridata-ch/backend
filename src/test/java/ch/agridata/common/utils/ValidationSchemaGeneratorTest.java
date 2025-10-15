package ch.agridata.common.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ValidationSchemaGeneratorTest {

  private ValidationSchemaGenerator generator;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    objectMapper = new ObjectMapper();
    generator = new ValidationSchemaGenerator(validator, objectMapper);
  }

  @Test
  void testSchemaWithoutGroups() {
    JsonNode schema = generator.generateJsonSchema(TestDto.class, Set.of());
    JsonNode description = schema.at("/properties/description");
    assertEquals("string", description.get("type").asText());
    assertEquals(0, description.get("minLength").asInt());
    assertEquals(100, description.get("maxLength").asInt());
    assertThat(schema.at("/required").toString()).doesNotContain("description");

    JsonNode code = schema.at("/properties/code");
    assertEquals("string", code.get("type").asText());
    assertEquals("\\d{4}", code.get("pattern").asText());

    JsonNode items = schema.at("/properties/items/items/properties/id");
    assertEquals("string", items.get("type").asText());

    JsonNode metadata = schema.at("/properties/metadata/properties/name");
    assertEquals(2, metadata.get("minLength").asInt());
    assertEquals(10, metadata.get("maxLength").asInt());

    JsonNode stock = schema.at("/properties/stock");
    assertEquals(50, stock.get("maximum").asInt());

    JsonNode quantity = schema.at("/properties/quantity");
    assertEquals(10, quantity.get("minimum").asInt());

  }

  @Test
  void testSchemaWithSubmitGroup() {
    JsonNode schema = generator.generateJsonSchema(TestDto.class, Set.of(TestDto.OnSubmit.class));
    JsonNode description = schema.at("/properties/description");
    assertEquals(10, description.get("minLength").asInt());
    assertEquals(90, description.get("maxLength").asInt());

    JsonNode status = schema.at("/properties/status");
    assertEquals("string", status.get("type").asText());
    assertTrue(schema.get("required").toString().contains("status"));

    JsonNode active = schema.at("/properties/active");
    assertEquals("boolean", active.get("type").asText());
    assertTrue(schema.get("required").toString().contains("active"));

  }

  @Test
  void testSchemaWithCreateGroup() {
    JsonNode schema = generator.generateJsonSchema(TestDto.class, Set.of(TestDto.OnCreate.class));

    assertTrue(schema.get("required").toString().contains("description"));
  }
}