package ch.agridata.archunit;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import ch.agridata.common.dto.PageResponseDto;
import ch.agridata.common.dto.ResourceQueryDto;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import io.quarkus.test.junit.QuarkusTest;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * This test validates the architecture of the modules in the application.
 * It ensures that modules only access each other through specific layers
 * and that technical layers have correct dependencies.
 */

@AnalyzeClasses(packages = ModuleArchitectureTest.MAIN_PACKAGE, importOptions = {
    ImportOption.DoNotIncludeTests.class})
class ModuleArchitectureTest {

  static final String MAIN_PACKAGE = "ch.agridata";
  static final List<String> MODULES =
      List.of("agreement", "user", "auditing", "agis", "common", "product", "uidregister", "datatransfer");
  static final List<String> ALLOWED_COMMON_DEPENDENCIES =
      List.of("ch.agridata.common.persistence", "ch.agridata.common.api", "ch.agridata.common.dto",
          "ch.agridata.common.utils", "ch.agridata.common.security", "ch.agridata.common.exceptions",
          "ch.agridata.common.persistence", "ch.agridata.common.filters");
  static final JavaClasses CLASSES = new ClassFileImporter()
      .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
      .importPackages(MAIN_PACKAGE);

  private static final String API = "api";
  private static final String CONTROLLER = "controller";
  private static final String DTO = "dto";
  private static final String MAPPER = "mapper";
  private static final String SERVICE = "service";
  private static final String PERSISTENCE = "persistence";

  public static String[] convertToSpreadArray(List<String> list) {
    return list.toArray(String[]::new);
  }

  @Test
  void technical_layers_have_correct_dependencies() {
    layeredArchitecture().consideringOnlyDependenciesInLayers()
        .layer(API).definedBy(".." + API + "..")
        .layer(CONTROLLER).definedBy(".." + CONTROLLER + "..")
        .layer(DTO).definedBy(".." + DTO + "..")
        .layer(MAPPER).definedBy(".." + MAPPER + "..")
        .layer(PERSISTENCE).definedBy(".." + PERSISTENCE + "..")
        .layer(SERVICE).definedBy(".." + SERVICE + "..")

        .whereLayer(API).mayOnlyBeAccessedByLayers(SERVICE)
        .whereLayer(CONTROLLER).mayNotBeAccessedByAnyLayer()
        .whereLayer(DTO).mayOnlyBeAccessedByLayers(API, CONTROLLER, MAPPER, SERVICE)
        .whereLayer(MAPPER).mayOnlyBeAccessedByLayers(SERVICE)
        .whereLayer(PERSISTENCE).mayOnlyBeAccessedByLayers(MAPPER, SERVICE)
        .whereLayer(SERVICE).mayOnlyBeAccessedByLayers(API, CONTROLLER)

        .ignoreDependency(
            resideInAnyPackage(".." + PERSISTENCE + ".."),
            JavaClass.Predicates.equivalentTo(ResourceQueryDto.class)
        )
        .ignoreDependency(
            resideInAnyPackage(".." + PERSISTENCE + ".."),
            JavaClass.Predicates.equivalentTo(PageResponseDto.class)
        )

        .check(CLASSES);
  }

  /**
   * Ensures modules can only access each other through service layers.
   * Also ensures that only service layers can access API and DTOs of other modules.
   */

  @Test
  void modules_correctly_accessing_other_modules() {
    for (String consumerModule : MODULES) {
      for (String providerModule : MODULES) {
        if (consumerModule.equals(providerModule)) {
          continue;
        }

        validateRestrictedAccessToCommonModule(consumerModule, providerModule);
        validateModuleAccessOnlyThroughService(consumerModule, providerModule);
        validateOnlyServiceAccessApi(providerModule);
        validateOnlySpecificLayersAccessDto(providerModule);
      }
    }
  }

  /**
   * Ensures that QuarkusTests are only resided in the integration package
   */

  @Test
  void quarkusTests_should_only_reside_in_integration_package() {
    JavaClasses classes = new ClassFileImporter().importPackages("ch.agridata", "integration");

    classes()
        .that().areAnnotatedWith(QuarkusTest.class)
        .should().resideInAPackage("integration..")
        .because("@QuarkusTest classes should only reside in the 'integration' package")
        .check(classes);
  }

  /**
   * Ensures module may only access whitelisted parts of `common`.
   */

  private void validateRestrictedAccessToCommonModule(String consumerModule,
                                                      String providerModule) {
    if (!providerModule.equals("common")) {
      return;
    }
    noClasses()
        .that(resideInAnyPackage(moduleRoot(consumerModule)))
        .should()
        .dependOnClassesThat(
            resideInAnyPackage(moduleRoot(providerModule))
                .and(not(resideInAnyPackage(convertToSpreadArray(ALLOWED_COMMON_DEPENDENCIES)))))
        .because(String.format(
            "Module %s tries to access %s directly, which is not allowed. Only %s are allowed.",
            consumerModule, providerModule, ALLOWED_COMMON_DEPENDENCIES))
        .check(CLASSES);
  }

  /**
   * Validates only `service` can access other modules with the exception of DTOs.
   */

  private void validateModuleAccessOnlyThroughService(String consumer, String provider) {
    if (provider.equals("common")) {
      return;
    }

    noClasses()
        .that(resideInAnyPackage(moduleRoot(consumer))
            .and(not(resideInAnyPackage(layer(consumer, SERVICE)))))
        .should()
        .dependOnClassesThat(
            resideInAnyPackage(moduleRoot(provider))
                .and(not(resideInAnyPackage(layer(provider, DTO)))))  // Skip DTOs
        .because(String.format(
            "Only %s.service is allowed to depend on %s (except for its DTOs)",
            consumer, provider))
        .check(CLASSES);
  }

  /**
   * Ensures only `service` can access another module's API.
   */

  private void validateOnlyServiceAccessApi(String provider) {
    if (provider.equals("common")) {
      return;
    }
    // Allow service layers from *any* module to access the provider API
    String[] allServiceLayers = MODULES.stream()
        .flatMap(m -> Stream.of(layer(m, SERVICE)))
        .toArray(String[]::new);

    noClasses()
        .that(resideInAnyPackage(MAIN_PACKAGE + "..")
            .and(not(resideInAnyPackage(allServiceLayers)))
            .and(not(resideInAnyPackage(moduleRoot(provider)))))
        .should()
        .dependOnClassesThat(resideInAnyPackage(layer(provider, API)))
        .because(String.format(
            "Only service layers of any module may access %s.api", provider))
        .check(CLASSES);
  }

  /**
   * Restricts access to `provider.dto` to specific consumer layers.
   */

  private void validateOnlySpecificLayersAccessDto(String provider) {
    if (provider.equals("common")) {
      return;
    }

    // Allow all module DTOs to access each other's DTOs
    String[] allAllowedDtoAccessors = MODULES.stream()
        .flatMap(m -> Stream.of(
            layer(m, SERVICE),
            layer(m, DTO),
            layer(m, CONTROLLER),
            layer(m, MAPPER)
        ))
        .toArray(String[]::new);

    noClasses()
        .that(resideInAnyPackage(MAIN_PACKAGE + "..")
            .and(not(resideInAnyPackage(allAllowedDtoAccessors)))
            .and(not(resideInAnyPackage(moduleRoot(provider)))))
        .should()
        .dependOnClassesThat(resideInAnyPackage(layer(provider, DTO)))
        .because(String.format(
            "Only service, dto, controller, mapper layers of any module may access %s.dto",
            provider))
        .check(CLASSES);
  }

  /**
   * Returns full package path to a module's sublayer, e.g. `ch.agridata.agreement.service..`
   */

  private String layer(String module, String layer) {
    return String.format("%s.%s.%s..", MAIN_PACKAGE, module, layer);
  }

  /**
   * Returns base package for the module, e.g. `ch.agridata.product..`
   */

  private String moduleRoot(String module) {
    return String.format("%s.%s..", MAIN_PACKAGE, module);
  }
}
