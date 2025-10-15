package ch.agridata.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Validator;
import jakarta.validation.groups.Default;
import jakarta.validation.metadata.BeanDescriptor;
import jakarta.validation.metadata.ConstraintDescriptor;
import jakarta.validation.metadata.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

/**
 * Generates JSON Schemas from annotated Java DTOs using Jakarta Validation metadata. It supports nested objects, collections, and common
 * validation constraints like {@code @NotNull}, {@code @NotEmpty}, {@code @Size}, {@code @Pattern}, {@code @Min}, and {@code @Max}.
 *
 * @CommentLastReviewed 2025-08-25
 */
@ApplicationScoped
@RequiredArgsConstructor
public class ValidationSchemaGenerator {
  private final Validator validator;
  private final ObjectMapper objectMapper;

  /**
   * Generates a JSON schema for a given root DTO class.
   *
   * @param rootClass The DTO class to process.
   * @param groups    The validation groups to apply.
   * @return The generated JSON Schema as an ObjectNode.
   */
  public ObjectNode generateJsonSchema(Class<?> rootClass, Set<Class<?>> groups) {
    ObjectNode rootSchema = objectMapper.createObjectNode();
    ObjectNode propertiesNode = objectMapper.createObjectNode();
    ArrayNode requiredFields = objectMapper.createArrayNode();
    Set<String> visitedPaths = new HashSet<>();

    processClass(rootClass, groups, propertiesNode, requiredFields, visitedPaths,
        rootClass.getSimpleName());

    rootSchema.put("type", "object");
    rootSchema.set("properties", propertiesNode);
    if (!requiredFields.isEmpty()) {
      rootSchema.set("required", requiredFields);
    }

    return rootSchema;
  }

  /**
   * Recursively processes a class and its fields to generate JSON Schema nodes.
   *
   * @param clazz          The current class to process.
   * @param groups         The validation groups to apply.
   * @param propertiesNode JSON object to populate with field schemas.
   * @param requiredFields JSON array of required field names.
   * @param visited        Tracks already visited paths to avoid infinite recursion.
   * @param path           Fully qualified field path for current processing depth.
   */
  private void processClass(Class<?> clazz, Set<Class<?>> groups, ObjectNode propertiesNode,
                            ArrayNode requiredFields, Set<String> visited, String path) {
    if (clazz == null || clazz.getName().startsWith("java.")) {
      return;
    }
    if (!visited.add(path)) {
      return;
    }

    BeanDescriptor beanDescriptor = validator.getConstraintsForClass(clazz);

    for (Field field : clazz.getDeclaredFields()) {
      ObjectNode fieldSchema = objectMapper.createObjectNode();
      String newPath = path + "." + field.getName();
      processField(clazz, field, fieldSchema, beanDescriptor, groups, requiredFields, visited,
          newPath);
      propertiesNode.set(field.getName(), fieldSchema);
    }
  }

  /**
   * Processes a single field and determines its schema representation.
   *
   * @param declaringClass Class that declares the field.
   * @param field          The field to process. May be null for anonymous nested items.
   * @param fieldSchema    Output JSON schema node for the field.
   * @param beanDescriptor Validation metadata for the declaring class.
   * @param groups         Active validation groups.
   * @param requiredFields List of required field names for the parent class.
   * @param visited        Visited field paths.
   * @param path           Fully qualified field path.
   */
  private void processField(Class<?> declaringClass, Field field, ObjectNode fieldSchema,
                            BeanDescriptor beanDescriptor, Set<Class<?>> groups,
                            ArrayNode requiredFields, Set<String> visited, String path) {
    if (field == null) {
      processAnonymousNestedObject(declaringClass, fieldSchema, groups, visited, path);
      return;
    }

    Class<?> fieldType = field.getType();
    String fieldName = field.getName();
    PropertyDescriptor descriptor = beanDescriptor.getConstraintsForProperty(fieldName);

    // ✅ Enum handling
    if (fieldType.isEnum()) {
      fieldSchema.put("type", "string");
      ArrayNode enumValues = objectMapper.createArrayNode();
      for (Object constant : fieldType.getEnumConstants()) {
        enumValues.add(constant.toString());
      }
      fieldSchema.set("enum", enumValues);

      if (descriptor != null) {
        applyConstraints(descriptor, fieldName, fieldSchema, groups, requiredFields);
      }
      return;
    }

    if (Collection.class.isAssignableFrom(fieldType)) {
      processCollectionField(field, fieldSchema, groups, requiredFields, visited, beanDescriptor,
          path);
    } else {
      processScalarOrNestedField(field, fieldSchema, beanDescriptor, groups, requiredFields,
          visited, path);
    }
  }

  /**
   * Handles fields that are collections and generates array schemas.
   *
   * @param field          Field representing the collection.
   * @param fieldSchema    Output schema for the field.
   * @param groups         Validation groups.
   * @param requiredFields List of required field names.
   * @param visited        Visited paths.
   * @param beanDescriptor Descriptor for constraints.
   * @param path           Fully qualified path to the field.
   */
  private void processCollectionField(Field field, ObjectNode fieldSchema,
                                      Set<Class<?>> groups, ArrayNode requiredFields,
                                      Set<String> visited, BeanDescriptor beanDescriptor,
                                      String path) {
    String fieldName = field.getName();
    fieldSchema.put("type", "array");

    Type genericType = field.getGenericType();
    if (genericType instanceof ParameterizedType parameterizedType) {
      Type actualType = parameterizedType.getActualTypeArguments()[0];
      if (actualType instanceof Class<?> itemClass) {
        ObjectNode itemSchema = objectMapper.createObjectNode();

        if (UUID.class.isAssignableFrom(itemClass)) {
          itemSchema.put("type", "string");
          itemSchema.put("format", "uuid");
        } else if (isPrimitiveOrSimpleType(itemClass)) {
          itemSchema.put("type", mapTypeToJsonType(itemClass));
        } else {
          processField(itemClass, null, itemSchema,
              validator.getConstraintsForClass(itemClass), groups,
              objectMapper.createArrayNode(), visited, path + "[]");
        }
        fieldSchema.set("items", itemSchema);
      }
    }

    PropertyDescriptor descriptor = beanDescriptor.getConstraintsForProperty(fieldName);
    if (descriptor != null) {
      applyConstraints(descriptor, fieldName, fieldSchema, groups, requiredFields);
    }
  }

  /**
   * Processes primitive or nested POJO fields.
   */
  private void processScalarOrNestedField(Field field, ObjectNode fieldSchema,
                                          BeanDescriptor beanDescriptor, Set<Class<?>> groups,
                                          ArrayNode requiredFields, Set<String> visited,
                                          String path) {
    String fieldName = field.getName();
    Class<?> fieldType = field.getType();

    fieldSchema.put("type", mapTypeToJsonType(fieldType));

    PropertyDescriptor descriptor = beanDescriptor.getConstraintsForProperty(fieldName);
    if (descriptor != null) {
      applyConstraints(descriptor, fieldName, fieldSchema, groups, requiredFields);
    }

    if (hasConstraints(fieldType) || isCustomPojo(fieldType)) {
      handleNestedPojo(fieldType, fieldSchema, groups, visited, path);
    }
  }

  /**
   * Recursively processes a nested POJO and embeds its schema.
   */
  private void handleNestedPojo(Class<?> fieldType, ObjectNode fieldSchema,
                                Set<Class<?>> groups, Set<String> visited, String path) {
    ObjectNode nestedProps = objectMapper.createObjectNode();
    ArrayNode nestedRequired = objectMapper.createArrayNode();

    processClass(fieldType, groups, nestedProps, nestedRequired, visited, path);

    fieldSchema.put("type", "object");
    fieldSchema.set("properties", nestedProps);
    if (!nestedRequired.isEmpty()) {
      fieldSchema.set("required", nestedRequired);
    }
  }

  /**
   * Processes nested object types passed without a field (e.g., array items).
   */
  private void processAnonymousNestedObject(Class<?> clazz, ObjectNode fieldSchema,
                                            Set<Class<?>> groups, Set<String> visited,
                                            String path) {
    fieldSchema.put("type", "object");

    ObjectNode nestedProps = objectMapper.createObjectNode();
    ArrayNode nestedRequired = objectMapper.createArrayNode();

    processClass(clazz, groups, nestedProps, nestedRequired, visited, path);

    fieldSchema.set("properties", nestedProps);
    if (!nestedRequired.isEmpty()) {
      fieldSchema.set("required", nestedRequired);
    }
  }

  /**
   * Applies relevant validation constraints from the descriptor to the JSON Schema, filtering based on active validation groups.
   *
   * @param descriptor     the validation descriptor for a property
   * @param fieldName      the name of the field
   * @param fieldSchema    the JSON schema node representing the field
   * @param groups         active validation groups to filter constraints
   * @param requiredFields output list to which required fields will be added
   */
  private void applyConstraints(PropertyDescriptor descriptor, String fieldName,
                                ObjectNode fieldSchema, Set<Class<?>> groups,
                                ArrayNode requiredFields) {

    List<ConstraintDescriptor<?>> constraints = descriptor.findConstraints()
        .unorderedAndMatchingGroups(groups.toArray(new Class<?>[0]))
        .getConstraintDescriptors()
        .stream()
        .filter(cd -> isGroupApplicable(cd.getGroups(), groups))
        .toList();

    ConstraintInfo merged = mergeConstraints(constraints);

    if (merged.isRequired()) {
      requiredFields.add(fieldName);
    }

    if (merged.isNotEmpty()) {
      fieldSchema.put("minItems", 1);
    }

    if (merged.getMinLength() != null) {
      fieldSchema.put("minLength", merged.getMinLength());
    }

    if (merged.getMaxLength() != null) {
      fieldSchema.put("maxLength", merged.getMaxLength());
    }

    if (merged.getMinimum() != null) {
      fieldSchema.put("minimum", merged.getMinimum());
    }

    if (merged.getMaximum() != null) {
      fieldSchema.put("maximum", merged.getMaximum());
    }

    if (merged.getPattern() != null) {
      fieldSchema.put("pattern", merged.getPattern());
    }
  }

  /**
   * Determines whether a constraint's groups match the currently active validation groups.
   *
   * @param constraintGroups the groups on the constraint annotation
   * @param activeGroups     the set of active groups used for validation
   * @return true if the constraint should be applied, false otherwise
   */
  private boolean isGroupApplicable(Set<Class<?>> constraintGroups, Set<Class<?>> activeGroups) {
    if (activeGroups.isEmpty()) {
      return constraintGroups.isEmpty() || constraintGroups.contains(Default.class);
    }
    return constraintGroups.stream().anyMatch(activeGroups::contains);
  }

  /**
   * Merges multiple Jakarta Bean Validation constraints into the most restrictive possible representation. Handles constraints across
   * multiple validation groups.
   *
   * @param constraints the list of constraint descriptors to merge
   * @return a {@link ConstraintInfo} object representing the merged constraint rules
   */
  private ConstraintInfo mergeConstraints(List<ConstraintDescriptor<?>> constraints) {
    ConstraintInfo info = new ConstraintInfo();

    for (ConstraintDescriptor<?> cd : constraints) {
      Map<String, Object> attrs = cd.getAttributes();
      String annotation = cd.getAnnotation().annotationType().getSimpleName();

      switch (annotation) {
        case "NotNull" -> info.markRequired();

        case "NotEmpty" -> {
          info.markRequired();
          info.markNotEmpty();
        }

        case "Size" -> {
          if (attrs.containsKey("min")) {
            int min = (Integer) attrs.get("min");
            info.updateMinLength(min);
          }
          if (attrs.containsKey("max")) {
            int max = (Integer) attrs.get("max");
            info.updateMaxLength(max);
          }
        }

        case "Min" -> {
          long min = Long.parseLong(String.valueOf(attrs.get("value")));
          info.updateMinimum(min);
        }

        case "Max" -> {
          long max = Long.parseLong(String.valueOf(attrs.get("value")));
          info.updateMaximum(max);
        }

        case "Pattern" -> {
          // Multiple patterns are not merged; only first is kept
          info.setPattern(String.valueOf(attrs.get("regexp")));
        }
      }
    }

    return info;
  }

  /**
   * Checks if a class has Jakarta validation constraints.
   */

  private boolean hasConstraints(Class<?> clazz) {
    return clazz != null
        && !clazz.getName().startsWith("java.")
        && !clazz.isPrimitive()
        && validator.getConstraintsForClass(clazz).isBeanConstrained();
  }

  /**
   * Checks whether the class is a custom POJO (non-primitive, non-collection, non-map).
   */

  private boolean isCustomPojo(Class<?> clazz) {
    return !clazz.isPrimitive()
        && !clazz.getName().startsWith("java.")
        && !clazz.isEnum()
        && !Collection.class.isAssignableFrom(clazz)
        && !Map.class.isAssignableFrom(clazz);
  }

  /**
   * Maps a Java type to its corresponding JSON Schema type.
   *
   * @param type Java class type.
   * @return JSON Schema type string (e.g., "string", "integer").
   */

  private String mapTypeToJsonType(Class<?> type) {
    if (UUID.class.isAssignableFrom(type) || String.class.isAssignableFrom(type)) {
      return "string";
    }
    if (Integer.class.isAssignableFrom(type) || int.class.isAssignableFrom(type)
        || Long.class.isAssignableFrom(type) || long.class.isAssignableFrom(type)) {
      return "integer";
    }
    if (Double.class.isAssignableFrom(type) || double.class.isAssignableFrom(type)
        || Float.class.isAssignableFrom(type) || float.class.isAssignableFrom(type)
        || BigDecimal.class.isAssignableFrom(type)) {
      return "number";
    }
    if (Boolean.class.isAssignableFrom(type) || boolean.class.isAssignableFrom(type)) {
      return "boolean";
    }
    return "object";
  }

  private boolean isPrimitiveOrSimpleType(Class<?> type) {
    return type.isPrimitive()
        || String.class.isAssignableFrom(type)
        || Number.class.isAssignableFrom(type)
        || Boolean.class.isAssignableFrom(type)
        || UUID.class.isAssignableFrom(type);
  }

  /**
   * Used as a validation group marker interface. It allows distinguishing between different validation scenarios (e.g., draft vs.
   * submission)
   *
   * @CommentLastReviewed 2025-08-25
   */
  public interface Submit extends Default {
  }

  /**
   * Holds the merged constraint information for a field.
   *
   * @CommentLastReviewed 2025-08-25
   */
  private static class ConstraintInfo {
    private boolean required = false;
    private boolean notEmpty = false;
    private Integer minLength;
    private Integer maxLength;
    private Long minimum;
    private Long maximum;
    private String pattern;

    public boolean isRequired() {
      return required;
    }

    public void markRequired() {
      this.required = true;
    }

    public boolean isNotEmpty() {
      return notEmpty;
    }

    public void markNotEmpty() {
      this.notEmpty = true;
    }

    public Integer getMinLength() {
      return minLength;
    }

    public void updateMinLength(int min) {
      this.minLength = (minLength == null) ? min : Math.max(minLength, min);
    }

    public Integer getMaxLength() {
      return maxLength;
    }

    public void updateMaxLength(int max) {
      this.maxLength = (maxLength == null) ? max : Math.min(maxLength, max);
    }

    public Long getMinimum() {
      return minimum;
    }

    public void updateMinimum(long min) {
      this.minimum = (minimum == null) ? min : Math.max(minimum, min);
    }

    public Long getMaximum() {
      return maximum;
    }

    public void updateMaximum(long max) {
      this.maximum = (maximum == null) ? max : Math.min(maximum, max);
    }

    public String getPattern() {
      return pattern;
    }

    public void setPattern(String pattern) {
      if (this.pattern == null) {
        this.pattern = pattern;
      }
    }
  }

}
