package ch.agridata.common.openapi;

import io.quarkus.smallrye.openapi.OpenApiFilter;
import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.media.MediaType;
import org.eclipse.microprofile.openapi.models.responses.APIResponse;
import org.eclipse.microprofile.openapi.models.responses.APIResponses;

/**
 * Represents a standardized error response for OpenAPI. It ensures consistency in API specifications.
 *
 * @CommentLastReviewed 2025-08-25
 */

@OpenApiFilter(OpenApiFilter.RunStage.BUILD)
public class OpenApiExceptionResponse implements OASFilter {

  @Override
  public Operation filterOperation(Operation op) {
    if (op == null) {
      return null;
    }

    // Don’t overwrite if the dev already documented 500
    APIResponses responses = op.getResponses();
    if (responses != null && responses.hasAPIResponse("500")) {
      return op;
    }

    MediaType json = OASFactory.createMediaType()
        .schema(OASFactory.createSchema().ref("#/components/schemas/ExceptionDto"));

    APIResponse error500 = OASFactory.createAPIResponse()
        .description("Internal server error: unexpected error")
        .content(OASFactory.createContent().addMediaType("application/json", json));

    if (responses == null) {
      responses = OASFactory.createAPIResponses();
      op.setResponses(responses);
    }
    responses.addAPIResponse("500", error500);
    return op;
  }
}
