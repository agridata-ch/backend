package ch.agridata.common.openapi;

import ch.agridata.common.security.actingrole.ActingRoleEnum;
import ch.agridata.common.security.actingrole.EnableActingRoleHolder;
import io.quarkus.smallrye.openapi.OpenApiFilter;
import jakarta.annotation.security.RolesAllowed;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;

/**
 * OAS filter that injects the {@code actingRole} query parameter into every operation whose Java method carries
 * {@link EnableActingRoleHolder}. The injected parameter is optional, and has an enum schema derived from the method's (or its
 * enclosing class's) {@code @RolesAllowed} via {@link ActingRoleEnum#fromKeycloakRole(String)}
 *
 * <p>Operations whose method does not declare {@code @EnableActingRoleHolder} are not touched.
 *
 * @CommentLastReviewed 2026-05-20
 */
@OpenApiFilter(stages = {OpenApiFilter.RunStage.BUILD})
public class ActingRoleOpenApiFilter implements OASFilter {

  static final String BASE_PACKAGE = "ch.agridata";
  static final String PARAM_NAME = "actingRole";
  static final String PARAM_DESCRIPTION =
      "Selects the role in which the authenticated user acts for this request. Optional: if the authenticated user "
          + "holds exactly one of the allowed roles, the value is auto-resolved. Returns 400 if the value is unknown, "
          + "not allowed for this endpoint, or omitted while the user holds multiple matching roles. Returns 403 if "
          + "the user does not hold the role specified in the parameter.";

  private final Map<String, EnumSet<ActingRoleEnum>> rolesByOperationId;

  public ActingRoleOpenApiFilter() {
    this.rolesByOperationId = new HashMap<>();
    scanPackageRecursively(BASE_PACKAGE);
  }

  @Override
  public Operation filterOperation(Operation operation) {
    if (operation == null || operation.getOperationId() == null) {
      return operation;
    }
    EnumSet<ActingRoleEnum> roles = rolesByOperationId.get(operation.getOperationId());
    if (roles == null || roles.isEmpty()) {
      return operation;
    }

    Schema schema = OASFactory.createSchema()
        .addType(Schema.SchemaType.STRING)
        .enumeration(roles.stream().map(Enum::name).map(s -> (Object) s).toList());

    Parameter param = OASFactory.createParameter()
        .name(PARAM_NAME)
        .in(Parameter.In.QUERY)
        .required(false)
        .description(PARAM_DESCRIPTION)
        .schema(schema);

    List<Parameter> existing = operation.getParameters();
    if (existing != null && existing.stream()
        .anyMatch(p -> PARAM_NAME.equals(p.getName()) && p.getIn() == Parameter.In.QUERY)) {
      return operation;
    }
    List<Parameter> parameters = new ArrayList<>();
    if (existing != null) {
      parameters.addAll(existing);
    }
    parameters.add(param);
    operation.setParameters(parameters);
    return operation;
  }

  private void scanPackageRecursively(String packageName) {
    String path = packageName.replace('.', '/');
    try {
      Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(path);
      while (resources.hasMoreElements()) {
        URL resource = resources.nextElement();
        if ("file".equals(resource.getProtocol())) {
          scanDirectory(new File(resource.toURI()), packageName);
        }
      }
    } catch (Exception ignored) {
      // Package not found or inaccessible — skip
    }
  }

  private void scanDirectory(File directory, String packageName) {
    File[] files = directory.listFiles();
    if (files == null) {
      return;
    }
    for (File file : files) {
      if (file.isDirectory()) {
        scanDirectory(file, packageName + "." + file.getName());
      } else if (file.getName().endsWith(".class")) {
        String className = packageName + "." + file.getName().replace(".class", "");
        try {
          Class<?> clazz = Class.forName(className);
          scanClass(clazz);
        } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
          // Skip classes that can't be loaded
        }
      }
    }
  }

  private void scanClass(Class<?> clazz) {
    for (Method method : clazz.getDeclaredMethods()) {
      if (method.getAnnotation(EnableActingRoleHolder.class) == null) {
        continue;
      }
      var op = method.getAnnotation(org.eclipse.microprofile.openapi.annotations.Operation.class);
      if (op == null || op.operationId().isEmpty()) {
        continue;
      }
      EnumSet<ActingRoleEnum> roles = derivedRoles(method);
      if (!roles.isEmpty()) {
        rolesByOperationId.put(op.operationId(), roles);
      }
    }
  }

  private EnumSet<ActingRoleEnum> derivedRoles(Method method) {
    String[] keycloakRoles = readRolesAllowed(method);
    EnumSet<ActingRoleEnum> result = EnumSet.noneOf(ActingRoleEnum.class);
    for (String keycloakRole : keycloakRoles) {
      ActingRoleEnum.fromKeycloakRole(keycloakRole).ifPresent(result::add);
    }
    return result;
  }

  private String[] readRolesAllowed(Method method) {
    RolesAllowed methodLevel = method.getAnnotation(RolesAllowed.class);
    if (methodLevel != null) {
      return methodLevel.value();
    }
    RolesAllowed classLevel = method.getDeclaringClass().getAnnotation(RolesAllowed.class);
    if (classLevel != null) {
      return classLevel.value();
    }
    return new String[0];
  }
}
