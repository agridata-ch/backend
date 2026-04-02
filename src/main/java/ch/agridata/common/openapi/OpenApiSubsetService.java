package ch.agridata.common.openapi;

import static ch.agridata.common.openapi.ApiSubsetConstants.X_API_SUBSET;

import io.quarkus.smallrye.openapi.runtime.OpenApiDocumentService;
import io.smallrye.openapi.api.SmallRyeOpenAPI;
import io.smallrye.openapi.runtime.io.Format;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.Components;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.Paths;
import org.eclipse.microprofile.openapi.models.media.Schema;

/**
 * Produces filtered OpenAPI documents containing only operations annotated with a matching {@code x-api-subset} extension value.
 *
 * @CommentLastReviewed 2026-02-16
 */

@ApplicationScoped
@RequiredArgsConstructor
public class OpenApiSubsetService {

  private final OpenApiDocumentService openApiDocumentService;

  /**
   * Returns a filtered OpenAPI document serialized in the requested format.
   *
   * @param subset the subset name to match against the {@code x-api-subset} extension
   * @param format the output format (YAML or JSON)
   * @return the serialized OpenAPI document
   */
  public String getFilteredDocument(String subset, Format format) {
    byte[] yamlBytes = openApiDocumentService.getDocument("<default>", Format.YAML);
    OpenAPI original = SmallRyeOpenAPI.builder()
        .withCustomStaticFile(() -> new ByteArrayInputStream(yamlBytes))
        .enableModelReader(false)
        .enableAnnotationScan(false)
        .enableStandardStaticFiles(false)
        .enableStandardFilter(false)
        .build()
        .model();
    OpenAPI filtered = buildFilteredModel(original, subset);
    SmallRyeOpenAPI filteredDoc = SmallRyeOpenAPI.builder()
        .withInitialModel(filtered)
        .enableModelReader(false)
        .enableAnnotationScan(false)
        .enableStandardStaticFiles(false)
        .enableStandardFilter(false)
        .build();
    return format == Format.JSON ? filteredDoc.toJSON() : filteredDoc.toYAML();
  }

  private OpenAPI buildFilteredModel(OpenAPI original, String subset) {
    OpenAPI filtered = OASFactory.createOpenAPI();
    filtered.setOpenapi(original.getOpenapi());
    filtered.setInfo(original.getInfo());
    filtered.setServers(original.getServers());
    filtered.setSecurity(original.getSecurity());
    Paths filteredPaths = OASFactory.createPaths();
    Set<String> referencedTags = new HashSet<>();

    if (original.getPaths() != null) {
      for (Map.Entry<String, PathItem> pathEntry : original.getPaths().getPathItems().entrySet()) {
        PathItem filteredItem = filterPathItem(pathEntry.getValue(), subset, referencedTags);
        if (filteredItem != null) {
          filteredPaths.addPathItem(pathEntry.getKey(), filteredItem);
        }
      }
    }

    filtered.setPaths(filteredPaths);

    if (original.getComponents() != null) {
      filtered.setComponents(buildFilteredComponents(filteredPaths, original.getComponents()));
    }

    if (original.getTags() != null) {
      original.getTags().stream()
          .filter(tag -> referencedTags.contains(tag.getName()))
          .forEach(filtered::addTag);
    }

    return filtered;
  }

  private PathItem filterPathItem(PathItem item, String subset, Set<String> referencedTags) {
    PathItem filtered = OASFactory.createPathItem();
    boolean hasMatch = false;

    for (Map.Entry<PathItem.HttpMethod, Operation> entry : item.getOperations().entrySet()) {
      if (operationMatchesSubset(entry.getValue(), subset)) {
        setOperation(filtered, entry.getKey(), entry.getValue());
        if (entry.getValue().getTags() != null) {
          referencedTags.addAll(entry.getValue().getTags());
        }
        hasMatch = true;
      }
    }

    if (!hasMatch) {
      return null;
    }

    filtered.setParameters(item.getParameters());
    return filtered;
  }

  private boolean operationMatchesSubset(Operation operation, String subset) {
    Map<String, Object> extensions = operation.getExtensions();
    if (extensions == null) {
      return false;
    }
    Object value = extensions.get(X_API_SUBSET);
    if (value instanceof String s) {
      return subset.equals(s);
    }
    if (value instanceof List<?> list) {
      return list.contains(subset);
    }
    return false;
  }

  private void setOperation(PathItem item, PathItem.HttpMethod method, Operation operation) {
    switch (method) {
      case GET -> item.setGET(operation);
      case PUT -> item.setPUT(operation);
      case POST -> item.setPOST(operation);
      case DELETE -> item.setDELETE(operation);
      case PATCH -> item.setPATCH(operation);
      case HEAD -> item.setHEAD(operation);
      case OPTIONS -> item.setOPTIONS(operation);
      case TRACE -> item.setTRACE(operation);
    }
  }

  private Components buildFilteredComponents(Paths filteredPaths, Components original) {
    Set<String> usedSchemaNames = new HashSet<>();
    collectSchemaRefsFromPaths(filteredPaths, original, usedSchemaNames);

    Components filtered = OASFactory.createComponents();

    if (original.getSchemas() != null) {
      usedSchemaNames.stream()
          .filter(name -> original.getSchemas().containsKey(name))
          .forEach(name -> filtered.addSchema(name, original.getSchemas().get(name)));
    }

    // Security schemes are referenced by name (not $ref), always include them all
    if (original.getSecuritySchemes() != null) {
      original.getSecuritySchemes().forEach(filtered::addSecurityScheme);
    }

    return filtered;
  }

  private void collectSchemaRefsFromPaths(Paths paths, Components components, Set<String> collected) {
    if (paths == null || paths.getPathItems() == null) {
      return;
    }
    for (PathItem pathItem : paths.getPathItems().values()) {
      if (pathItem.getParameters() != null) {
        pathItem.getParameters().forEach(p -> collectSchemaRefs(p.getSchema(), components, collected));
      }
      for (Operation op : pathItem.getOperations().values()) {
        if (op.getParameters() != null) {
          op.getParameters().forEach(p -> collectSchemaRefs(p.getSchema(), components, collected));
        }
        if (op.getRequestBody() != null && op.getRequestBody().getContent() != null) {
          op.getRequestBody().getContent().getMediaTypes().values()
              .forEach(mt -> collectSchemaRefs(mt.getSchema(), components, collected));
        }
        if (op.getResponses() != null && op.getResponses().getAPIResponses() != null) {
          op.getResponses().getAPIResponses().values().forEach(response -> {
            if (response.getContent() != null) {
              response.getContent().getMediaTypes().values()
                  .forEach(mt -> collectSchemaRefs(mt.getSchema(), components, collected));
            }
          });
        }
      }
    }
  }

  private void collectSchemaRefs(Schema schema, Components components, Set<String> collected) {
    if (schema == null) {
      return;
    }
    String ref = schema.getRef();
    if (ref != null) {
      String name = schemaNameFromRef(ref);
      if (name != null && collected.add(name) && components.getSchemas() != null) {
        collectSchemaRefs(components.getSchemas().get(name), components, collected);
      }
    }
    if (schema.getProperties() != null) {
      schema.getProperties().values().forEach(s -> collectSchemaRefs(s, components, collected));
    }
    if (schema.getAllOf() != null) {
      schema.getAllOf().forEach(s -> collectSchemaRefs(s, components, collected));
    }
    if (schema.getAnyOf() != null) {
      schema.getAnyOf().forEach(s -> collectSchemaRefs(s, components, collected));
    }
    if (schema.getOneOf() != null) {
      schema.getOneOf().forEach(s -> collectSchemaRefs(s, components, collected));
    }
    if (schema.getItems() != null) {
      collectSchemaRefs(schema.getItems(), components, collected);
    }
    if (schema.getAdditionalPropertiesSchema() != null) {
      collectSchemaRefs(schema.getAdditionalPropertiesSchema(), components, collected);
    }
  }

  private String schemaNameFromRef(String ref) {
    String prefix = "#/components/schemas/";
    return ref != null && ref.startsWith(prefix) ? ref.substring(prefix.length()) : null;
  }
}
