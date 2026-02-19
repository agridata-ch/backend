package ch.agridata.common.openapi;

import static ch.agridata.common.openapi.ApiSubsetConstants.X_API_SUBSET;

import io.quarkus.smallrye.openapi.runtime.OpenApiDocumentService;
import io.smallrye.openapi.runtime.io.Format;
import io.smallrye.openapi.runtime.io.OpenApiParser;
import io.smallrye.openapi.runtime.io.OpenApiSerializer;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.Paths;

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
  public String getFilteredDocument(String subset, Format format) throws IOException {
    byte[] yamlBytes = openApiDocumentService.getDocument("<default>", Format.YAML);
    OpenAPI original = OpenApiParser.parse(new ByteArrayInputStream(yamlBytes), Format.YAML, null);
    OpenAPI filtered = buildFilteredModel(original, subset);
    return OpenApiSerializer.serialize(filtered, format);
  }

  private OpenAPI buildFilteredModel(OpenAPI original, String subset) {
    OpenAPI filtered = OASFactory.createOpenAPI();
    filtered.setOpenapi(original.getOpenapi());
    filtered.setInfo(original.getInfo());
    filtered.setServers(original.getServers());
    filtered.setSecurity(original.getSecurity());
    filtered.setComponents(original.getComponents());

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
}
