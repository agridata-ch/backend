package ch.agridata.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.groups.Default;
import java.io.File;
import java.nio.file.Files;
import java.util.Set;

/**
 * Entry point for generating JSON Schemas from DTOs using Jakarta Validation annotations. Takes class names as arguments, generates their
 * JSON Schemas, and writes them to disk.
 *
 * @CommentLastReviewed 2025-08-25
 */
public class SchemaGeneratorMain {
  public static final String OUTPUT_PATH = "target/classes/schemas/agridata-schemas.json";

  public static void main(String[] args) throws Exception {
    if (args.length == 0) {
      System.err.println("Usage: java SchemaGeneratorMain <fully.qualified.ClassName> [...]");
      System.exit(1);
    }

    try {
      Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
      ObjectMapper mapper = new ObjectMapper();
      ValidationSchemaGenerator generator = new ValidationSchemaGenerator(validator, mapper);

      ObjectNode combinedSchema = generateSchemas(generator, mapper, args);
      writeSchemaToFile(combinedSchema, OUTPUT_PATH, mapper);

    } catch (Exception e) {
      System.err.println("Schema generation failed: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * Generates JSON schemas for a list of DTO class names.
   *
   * @param generator  the schema generator instance
   * @param mapper     Jackson object mapper
   * @param classNames array of fully qualified DTO class names
   * @return root ObjectNode containing individual schemas by simple class name
   */
  private static ObjectNode generateSchemas(ValidationSchemaGenerator generator,
                                            ObjectMapper mapper, String[] classNames) {
    ObjectNode root = mapper.createObjectNode();
    Set<Class<?>> groups = Set.of(Default.class, ValidationSchemaGenerator.Submit.class);

    for (String className : classNames) {
      try {
        Class<?> dtoClass = Class.forName(className);
        ObjectNode schema = generator.generateJsonSchema(dtoClass, groups);
        root.set(dtoClass.getSimpleName(), schema);
      } catch (ClassNotFoundException e) {
        System.err.println("Class not found: " + className);
      }
    }
    return root;
  }

  /**
   * Writes the given schema to a specified file path.
   *
   * @param schema     the root schema node to serialize
   * @param outputPath the destination file path
   * @param mapper     the Jackson object mapper used for writing
   * @throws Exception if writing fails
   */
  private static void writeSchemaToFile(ObjectNode schema, String outputPath, ObjectMapper mapper)
      throws Exception {

    String baseDir = System.getProperty("project.basedir");
    File output = new File(baseDir, outputPath);
    output.getParentFile().mkdirs();
    Files.writeString(output.toPath(),
        mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema));
    System.out.println("Schemas written to: " + output.getAbsolutePath());
  }
}
