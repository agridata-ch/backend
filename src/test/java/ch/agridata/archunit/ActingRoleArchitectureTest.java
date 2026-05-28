package ch.agridata.archunit;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.common.security.actingrole.ActingRoleHolder;
import ch.agridata.common.security.actingrole.EnableActingRoleHolder;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import jakarta.annotation.security.RolesAllowed;
import org.junit.jupiter.api.Test;

/**
 * Enforces consistency for the {@code @EnableActingRoleHolder} annotation:
 * <ul>
 *   <li>Only methods inside {@code ..controller..} packages may carry {@code @EnableActingRoleHolder}.</li>
 *   <li>Every method annotated with {@code @EnableActingRoleHolder} must have {@code @RolesAllowed} in scope
 *       (method-level or class-level). Without it, the resolver has no input set.</li>
 *   <li>{@link ActingRoleHolder} may only be referenced from {@code ..controller..} packages (and its own package).
 *       The role switch must happen in the controller, not deeper down in a service.</li>
 * </ul>
 */
@AnalyzeClasses(packages = "ch.agridata", importOptions = {ImportOption.DoNotIncludeTests.class})
class ActingRoleArchitectureTest {

  static final JavaClasses CLASSES = new ClassFileImporter()
      .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
      .importPackages("ch.agridata");

  @Test
  void enableRoleSwitch_only_on_methods_in_controller_packages() {
    methods()
        .that().areAnnotatedWith(EnableActingRoleHolder.class)
        .should().beDeclaredInClassesThat(resideInAnyPackage("..controller.."))
        .because("@EnableActingRoleHolder is only meaningful on JAX-RS resource methods inside controller packages")
        .check(CLASSES);
  }

  @Test
  void enableRoleSwitch_requires_rolesAllowed_in_scope() {
    var violations = CLASSES.stream()
        .flatMap(c -> c.getMethods().stream())
        .filter(m -> m.isAnnotatedWith(EnableActingRoleHolder.class))
        .filter(m -> !hasRolesAllowedInScope(m))
        .map(m -> m.getFullName() + " has @EnableActingRoleHolder but no @RolesAllowed in scope")
        .toList();

    assertThat(violations)
        .as("@EnableActingRoleHolder requires @RolesAllowed on the method or its declaring class")
        .isEmpty();
  }

  @Test
  void actingRoleHolder_only_used_in_controllers() {
    noClasses()
        .that().resideOutsideOfPackages("..controller..", "ch.agridata.common.security.actingrole..")
        .should().dependOnClassesThat().areAssignableTo(ActingRoleHolder.class)
        .because("the role switch must happen in the controller; services should receive the resolved role as a parameter")
        .check(CLASSES);
  }

  private boolean hasRolesAllowedInScope(JavaMethod method) {
    if (method.isAnnotatedWith(RolesAllowed.class)) {
      return true;
    }
    return method.getOwner().isAnnotatedWith(RolesAllowed.class);
  }
}
