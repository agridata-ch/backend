package ch.agridata.common.utils;

import static ch.agridata.common.utils.SchemaGeneratorMain.OUTPUT_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.nio.file.Files;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SchemaGeneratorMainTest {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  @BeforeAll
  static void setup() throws Exception {
    SchemaGeneratorMain.main(new String[] {
        "ch.agridata.common.utils.TestDto"
    });
  }

  @Test
  void outputFileShouldExist() {
    File outputFile = new File(OUTPUT_PATH);
    assertTrue(outputFile.exists(), "Output schema file should exist");
  }

  @Test
  void schemaShouldContainExpectedDto() throws Exception {
    File outputFile = new File(OUTPUT_PATH);
    JsonNode root = MAPPER.readTree(outputFile);

    assertTrue(root.has("TestDto"), "Schema should include TestDto");

    JsonNode dtoSchema = root.get("TestDto");
    assertEquals("object", dtoSchema.get("type").asText(), "Top-level schema should be an object");
    assertTrue(dtoSchema.has("properties"), "Schema should contain properties node");
  }

  @AfterAll
  static void cleanup() throws Exception {
    Files.deleteIfExists(new File(OUTPUT_PATH).toPath());
  }
}