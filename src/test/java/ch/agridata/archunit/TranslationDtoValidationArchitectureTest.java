package ch.agridata.archunit;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.common.persistence.TranslationPersistenceDto;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import jakarta.persistence.Entity;
import jakarta.validation.Valid;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Enforces that the size validation declared on {@link TranslationPersistenceDto} actually takes effect at persistence level.
 *
 * <p>Bean Validation only descends into a nested object when the cascade is requested explicitly. Without {@code @Valid} (respectively
 * {@code List<@Valid ...>} for collections) the {@code @Size} constraints on the record are never evaluated, and over-long values can be
 * persisted by any write path that bypasses the API DTO validation — service-internal calls, import jobs or direct repository use.
 *
 * <p>These rules keep that cascade consistent across all modules, so the inconsistency cannot silently return when a new multilingual
 * field is added.
 *
 * @CommentLastReviewed 2026-09-02
 */
@AnalyzeClasses(packages = "ch.agridata", importOptions = {ImportOption.DoNotIncludeTests.class})
class TranslationDtoValidationArchitectureTest {

  static final JavaClasses CLASSES = new ClassFileImporter()
      .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
      .importPackages("ch.agridata");

  /**
   * Every field directly typed {@link TranslationPersistenceDto} must cascade validation via {@code @Valid}.
   */
  @Test
  void translation_fields_must_cascade_validation() {
    ArchCondition<JavaField> beAnnotatedWithValid =
        new ArchCondition<>("be annotated with @Valid so the @Size constraints are evaluated on persist") {
          @Override
          public void check(JavaField field, ConditionEvents events) {
            if (!field.isAnnotatedWith(Valid.class)) {
              events.add(SimpleConditionEvent.violated(
                  field, String.format(
                      "Field %s.%s is a TranslationPersistenceDto but is not annotated with @Valid, "
                          + "so its @Size constraints are never validated",
                      field.getOwner().getSimpleName(), field.getName()
                  )
              ));
            }
          }
        };

    fields()
        .that().haveRawType(TranslationPersistenceDto.class)
        .and().areDeclaredInClassesThat().areAnnotatedWith(Entity.class)
        .should(beAnnotatedWithValid)
        .because("size validation of multilingual text must apply regardless of the entry point")
        .check(CLASSES);
  }

  /**
   * Every {@code List<TranslationPersistenceDto>} must cascade validation to its elements via {@code List<@Valid ...>}.
   *
   * <p>{@code @Valid} on a type argument is a {@code TYPE_USE} annotation and is not exposed by ArchUnit's field-annotation API, so this
   * rule is expressed with reflection over the imported classes instead of a fluent {@code ArchRule}.
   */
  @Test
  void translation_list_elements_must_cascade_validation() {
    var violations = CLASSES.stream()
        .flatMap(javaClass -> javaClass.getFields().stream())
        .filter(field -> field.getOwner().isAnnotatedWith(Entity.class))
        .filter(field -> field.getRawType().isAssignableTo(List.class))
        .filter(TranslationDtoValidationArchitectureTest::isUnvalidatedTranslationList)
        .map(field -> String.format(
            "Field %s.%s is a List<TranslationPersistenceDto> but declares no List<@Valid ...>, "
                + "so its elements are never validated",
            field.getOwner().getSimpleName(), field.getName()
        ))
        .toList();

    assertThat(violations)
        .as("size validation of multilingual text must apply regardless of the entry point")
        .isEmpty();
  }

  private static boolean isUnvalidatedTranslationList(JavaField field) {
    if (!(field.reflect().getAnnotatedType() instanceof AnnotatedParameterizedType parameterizedType)) {
      return false;
    }
    return java.util.Arrays.stream(parameterizedType.getAnnotatedActualTypeArguments())
        .filter(TranslationDtoValidationArchitectureTest::isTranslationType)
        .anyMatch(argument -> argument.getAnnotation(Valid.class) == null);
  }

  private static boolean isTranslationType(AnnotatedType annotatedType) {
    return TranslationPersistenceDto.class.equals(annotatedType.getType());
  }
}
