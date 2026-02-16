package ch.agridata.archunit;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import ch.agridata.common.openapi.ApiSubset;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.junit.jupiter.api.Test;

/**
 * Ensures all API endpoints are assigned to at least one API subset via {@link ApiSubset}.
 */

@AnalyzeClasses(packages = "ch.agridata", importOptions = {ImportOption.DoNotIncludeTests.class})
class ApiSubsetArchitectureTest {

  static final JavaClasses CLASSES = new ClassFileImporter()
      .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
      .importPackages("ch.agridata");

  @Test
  void all_endpoints_should_have_api_subset_annotation() {
    methods()
        .that().areDeclaredInClassesThat(resideInAnyPackage("..controller.."))
        .and().areAnnotatedWith(Operation.class)
        .should().beAnnotatedWith(ApiSubset.class)
        .because("all API endpoints must declare which API subset(s) they belong to via @ApiSubset")
        .check(CLASSES);
  }
}
