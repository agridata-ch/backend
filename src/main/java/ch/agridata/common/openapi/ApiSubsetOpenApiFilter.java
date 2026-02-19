package ch.agridata.common.openapi;

import static ch.agridata.common.openapi.ApiSubsetConstants.X_API_SUBSET;

import io.quarkus.smallrye.openapi.OpenApiFilter;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.annotations.Operation;

/**
 * OAS filter that translates {@link ApiSubset} annotations into {@code x-api-subset} extensions on OpenAPI operations.
 * Automatically scans all classes under {@code ch.agridata} for methods annotated with both {@link Operation} and
 * {@link ApiSubset}, then injects the corresponding {@code x-api-subset} extension into the OpenAPI model.
 *
 * @CommentLastReviewed 2026-02-16
 */

@OpenApiFilter(OpenApiFilter.RunStage.BUILD)
public class ApiSubsetOpenApiFilter implements OASFilter {

  private static final String BASE_PACKAGE = "ch.agridata";

  private final Map<String, String[]> subsetsByOperationId;

  /**
   * Creates the filter and builds a lookup map of operationId to subset values by scanning all classes under
   * {@code ch.agridata}.
   */
  public ApiSubsetOpenApiFilter() {
    subsetsByOperationId = new HashMap<>();
    scanPackageRecursively(BASE_PACKAGE);
  }

  @Override
  public org.eclipse.microprofile.openapi.models.Operation filterOperation(
      org.eclipse.microprofile.openapi.models.Operation operation) {
    if (operation.getOperationId() == null) {
      return operation;
    }

    String[] subsets = subsetsByOperationId.get(operation.getOperationId());
    if (subsets == null) {
      return operation;
    }

    if (subsets.length == 1) {
      operation.addExtension(X_API_SUBSET, subsets[0]);
    } else {
      operation.addExtension(X_API_SUBSET, List.of(subsets));
    }
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
    } catch (Exception e) {
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
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
          // Skip classes that can't be loaded
        }
      }
    }
  }

  private void scanClass(Class<?> clazz) {
    for (Method method : clazz.getDeclaredMethods()) {
      ApiSubset subset = method.getAnnotation(ApiSubset.class);
      Operation op = method.getAnnotation(Operation.class);
      if (subset != null && op != null && !op.operationId().isEmpty()) {
        subsetsByOperationId.put(op.operationId(), subset.value());
      }
    }
  }
}
