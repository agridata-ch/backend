package ch.agridata.archunit;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import io.quarkus.test.junit.QuarkusTest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Validates the architecture of the application by enforcing the allowed dependencies.
 *
 * @CommentLastReviewed 2026-03-25
 */
class ModuleArchitectureTest {

  static final String MAIN_PACKAGE = "ch.agridata";

  static final String API = "api";
  static final String CONTROLLER = "controller";
  static final String DTO = "dto";
  static final String MAPPER = "mapper";
  static final String SERVICE = "service";
  static final String JOB = "job";
  static final String PERSISTENCE = "persistence";

  /**
   * Intra-module: which layers may depend on which other layers within the same module.
   * All other intra-module dependencies are forbidden.
   */
  static final Map<String, List<String>> INTRA_MODULE_ALLOWED = Map.of(
      API, List.of(DTO),
      CONTROLLER, List.of(SERVICE, DTO),
      DTO, List.of(),
      MAPPER, List.of(PERSISTENCE, DTO),
      SERVICE, List.of(SERVICE, API, PERSISTENCE, MAPPER, DTO),
      JOB, List.of(SERVICE),
      PERSISTENCE, List.of()
  );

  /**
   * Cross-module: which consumer layers may access which layers in a provider module.
   * Layers not listed here may not access any other module.
   */
  static final Map<String, List<String>> CROSS_MODULE_ALLOWED = Map.of(
      SERVICE, List.of(API, DTO),
      MAPPER, List.of(DTO)
  );

  /**
   * Allowed sub-packages of the common module accessible from other modules.
   */
  static final List<String> ALLOWED_COMMON_PACKAGES = List.of(
      "ch.agridata.common.persistence",
      "ch.agridata.common.api",
      "ch.agridata.common.dto",
      "ch.agridata.common.utils",
      "ch.agridata.common.security",
      "ch.agridata.common.exceptions",
      "ch.agridata.common.filters",
      "ch.agridata.common.openapi"
  );

  static final JavaClasses CLASSES = new ClassFileImporter()
      .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
      .importPackages(MAIN_PACKAGE);
  static final List<String> MODULES = CLASSES.stream()
      .map(JavaClass::getPackageName)
      .filter(pkg -> pkg.startsWith(MAIN_PACKAGE + "."))
      .map(pkg -> pkg.substring(MAIN_PACKAGE.length() + 1))
      .map(pkg -> pkg.contains(".") ? pkg.substring(0, pkg.indexOf(".")) : pkg)
      .distinct()
      .sorted()
      .toList();

  @Test
  void dependency_rules_are_satisfied() {
    CLASSES.stream()
        .filter(c -> c.getPackageName().startsWith(MAIN_PACKAGE + "."))
        .map(c -> new ModuleLayer(moduleOf(c.getPackageName()), layerOf(c.getPackageName())))
        .filter(ml -> ml.layer() != null && INTRA_MODULE_ALLOWED.containsKey(ml.layer()))
        .distinct()
        .forEach(ml -> classes()
            .that(resideInAnyPackage(layer(ml.module(), ml.layer())))
            .should()
            .onlyDependOnClassesThat(allowedDependenciesFor(ml.module(), ml.layer()))
            .because(String.format("%s.%s: intra=%s, cross=%s",
                ml.module(), ml.layer(),
                INTRA_MODULE_ALLOWED.get(ml.layer()),
                CROSS_MODULE_ALLOWED.getOrDefault(ml.layer(), List.of())))
            .check(CLASSES));
  }

  @Test
  void all_classes_reside_in_known_layers() {
    var knownLayerPatterns = new ArrayList<String>();
    MODULES.stream()
        .filter(m -> !m.equals("common"))
        .forEach(module -> {
          knownLayerPatterns.add(MAIN_PACKAGE + "." + module);
          INTRA_MODULE_ALLOWED.keySet().forEach(l -> knownLayerPatterns.add(layer(module, l)));
        });

    classes()
        .that(resideInAnyPackage(MAIN_PACKAGE + ".."))
        .and(not(resideInAnyPackage("ch.agridata.common..")))
        .should().resideInAnyPackage(knownLayerPatterns.toArray(String[]::new))
        .because("all classes (except common) must reside in a known layer: " + INTRA_MODULE_ALLOWED.keySet())
        .check(CLASSES);
  }

  @Test
  void quarkusTests_should_only_reside_in_integration_package() {
    JavaClasses allClasses = new ClassFileImporter().importPackages("ch.agridata", "integration");
    classes()
        .that().areAnnotatedWith(QuarkusTest.class)
        .should().resideInAPackage("integration..")
        .because("@QuarkusTest classes should only reside in the 'integration' package")
        .check(allClasses);
  }

  private DescribedPredicate<JavaClass> allowedDependenciesFor(String module, String layer) {
    var allowedPatterns = new ArrayList<String>();

    // common module: only whitelisted packages
    ALLOWED_COMMON_PACKAGES.forEach(p -> allowedPatterns.add(p + ".."));

    // Own layer
    allowedPatterns.add(layer(module, layer));

    // Intra-module: explicitly allowed layers within the same module
    INTRA_MODULE_ALLOWED.get(layer).forEach(l -> allowedPatterns.add(layer(module, l)));

    // Cross-module: allowed provider layers for this layer type, across all other modules
    CROSS_MODULE_ALLOWED.getOrDefault(layer, List.of()).forEach(providerLayer ->
        MODULES.stream()
            .filter(m -> !m.equals(module) && !m.equals("common"))
            .forEach(otherModule -> allowedPatterns.add(layer(otherModule, providerLayer))));

    return not(resideInAnyPackage(MAIN_PACKAGE + ".."))
        .or(resideInAnyPackage(allowedPatterns.toArray(String[]::new)));
  }

  private String moduleOf(String pkg) {
    var rest = pkg.substring(MAIN_PACKAGE.length() + 1);
    var dot = rest.indexOf('.');
    return dot >= 0 ? rest.substring(0, dot) : rest;
  }

  private String layerOf(String pkg) {
    var rest = pkg.substring(MAIN_PACKAGE.length() + 1);
    var dot = rest.indexOf('.');
    if (dot < 0) {
      return null;
    }
    var afterModule = rest.substring(dot + 1);
    var dot2 = afterModule.indexOf('.');
    return dot2 >= 0 ? afterModule.substring(0, dot2) : afterModule;
  }

  private String layer(String module, String layer) {
    return String.format("%s.%s.%s..", MAIN_PACKAGE, module, layer);
  }

  private record ModuleLayer(String module, String layer) {
  }
}
