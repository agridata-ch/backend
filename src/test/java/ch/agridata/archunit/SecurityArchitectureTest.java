package ch.agridata.archunit;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.equivalentTo;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import ch.agridata.common.security.AgridataSecurityIdentity;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.junit.jupiter.api.Test;

@AnalyzeClasses(packages = SecurityArchitectureTest.MAIN_PACKAGE, importOptions = {
    ImportOption.DoNotIncludeTests.class})
class SecurityArchitectureTest {

  static final String MAIN_PACKAGE = "ch.agridata";

  private static final String COMMON_SECURITY_PACKAGE = "ch.agridata.common.security..";

  /**
   * Ensures that no class outside of {@code common.security} directly depends on the Quarkus
   * {@link SecurityIdentity}. All other code must go through {@link AgridataSecurityIdentity},
   * which adds impersonation support and a unified API for accessing identity claims.
   */
  @Test
  void no_class_outside_common_security_may_use_raw_SecurityIdentity() {
    var classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages(MAIN_PACKAGE);

    noClasses()
        .that(resideInAnyPackage(MAIN_PACKAGE + ".."))
        .and().resideOutsideOfPackage(COMMON_SECURITY_PACKAGE)
        .should().dependOnClassesThat(equivalentTo(SecurityIdentity.class))
        .because("Use AgridataSecurityIdentity instead of the raw Quarkus SecurityIdentity "
            + "to ensure consistent identity resolution")
        .check(classes);
  }

  /**
   * Ensures that security annotations are never placed on classes, only on individual methods.
   * This enforces explicit, per-endpoint security declarations and prevents accidental broad access grants.
   */
  @Test
  void security_annotations_must_not_be_placed_on_classes() {
    var classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages(MAIN_PACKAGE);

    noClasses()
        .that(resideInAnyPackage(MAIN_PACKAGE + ".."))
        .should().beAnnotatedWith(RolesAllowed.class)
        .orShould().beAnnotatedWith(Authenticated.class)
        .orShould().beAnnotatedWith(PermitAll.class)
        .because("security annotations must be placed on individual methods, not on classes")
        .check(classes);
  }

  /**
   * Ensures that every JAX-RS endpoint method in a controller is explicitly secured at method level.
   *
   * <p>Each method must carry exactly one of {@link RolesAllowed}, {@link Authenticated}, or
   * {@link PermitAll} directly — class-level annotations do not count. This makes the security intent
   * visible and explicit on every endpoint without relying on inherited class-level defaults.
   */
  @Test
  void all_rest_endpoints_in_controllers_must_be_secured() {
    var classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages(MAIN_PACKAGE);

    DescribedPredicate<JavaMethod> hasHttpVerbAnnotation = DescribedPredicate.describe(
        "annotated with a JAX-RS HTTP method annotation",
        method -> method.isAnnotatedWith(GET.class)
            || method.isAnnotatedWith(POST.class)
            || method.isAnnotatedWith(PUT.class)
            || method.isAnnotatedWith(DELETE.class)
            || method.isAnnotatedWith(PATCH.class));

    ArchCondition<JavaMethod> isSecuredAtMethodLevel =
        new ArchCondition<>("carry @RolesAllowed, @Authenticated, or @PermitAll directly on the method") {
          @Override
          public void check(JavaMethod method, ConditionEvents events) {
            boolean secured = method.isAnnotatedWith(RolesAllowed.class)
                || method.isAnnotatedWith(Authenticated.class)
                || method.isAnnotatedWith(PermitAll.class);
            if (!secured) {
              events.add(SimpleConditionEvent.violated(method, String.format(
                  "Method %s.%s() is a REST endpoint but has no security annotation (@RolesAllowed, @Authenticated, or @PermitAll)",
                  method.getOwner().getSimpleName(), method.getName())));
            }
          }
        };

    methods()
        .that().areDeclaredInClassesThat(resideInAnyPackage(MAIN_PACKAGE + ".."))
        .and().areDeclaredInClassesThat().areNotAnnotatedWith(RegisterRestClient.class)
        .and(hasHttpVerbAnnotation)
        .should(isSecuredAtMethodLevel)
        .because("every REST endpoint must be explicitly secured at method level")
        .check(classes);
  }
}
