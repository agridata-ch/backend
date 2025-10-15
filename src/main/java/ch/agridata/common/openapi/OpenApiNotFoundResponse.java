package ch.agridata.common.openapi;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.smallrye.openapi.OpenApiFilter;
import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.media.MediaType;
import org.eclipse.microprofile.openapi.models.responses.APIResponse;
import org.eclipse.microprofile.openapi.models.responses.APIResponses;

/**
 * Defines the OpenAPI representation of a "not found" response. It documents behavior for missing resources.
 *
 * @CommentLastReviewed 2025-08-25
 */

@OpenApiFilter(OpenApiFilter.RunStage.BUILD)
public class OpenApiNotFoundResponse implements OASFilter {

  @Override
  public Operation filterOperation(Operation op) {
    if (op == null) {
      return null;
    }

    // Don’t overwrite if the dev already documented not found
    APIResponses responses = op.getResponses();
    if (responses != null
        && responses.hasAPIResponse(String.valueOf(HttpResponseStatus.NOT_FOUND.code()))) {
      return op;
    }

    MediaType json = OASFactory.createMediaType()
        .schema(OASFactory.createSchema().ref("#/components/schemas/ExceptionDto"));

    APIResponse error = OASFactory.createAPIResponse()
        .description("The resource or element was not found")
        .content(OASFactory.createContent().addMediaType("application/json", json));

    if (responses == null) {
      responses = OASFactory.createAPIResponses();
      op.setResponses(responses);
    }
    responses.addAPIResponse(String.valueOf(HttpResponseStatus.NOT_FOUND.code()), error);
    return op;
  }
}
