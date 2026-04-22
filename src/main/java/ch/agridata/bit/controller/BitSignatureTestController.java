package ch.agridata.bit.controller;

import static ch.agridata.common.openapi.ApiSubsetConstants.WEB_APP;
import static ch.agridata.common.utils.AuthenticationUtil.ADMIN_ROLE;

import ch.agridata.bit.service.BitSignatureApiImpl;
import ch.agridata.common.openapi.ApiSubset;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

/**
 * Test endpoint for verifying BIT Evidence Signing API integration. Only available on non-production profiles.
 *
 * @CommentLastReviewed 2026-04-14
 */
@Slf4j
@Path("/api/bit/v1/test")
@Tag(name = "BIT Signature Test")
@RequiredArgsConstructor
@RunOnVirtualThread
public class BitSignatureTestController {

  private static final List<String> ENABLED_PROFILES = List.of("local", "test", "develop", "testing");

  private final BitSignatureApiImpl bitSignatureApi;

  @ConfigProperty(name = "quarkus.profile")
  String activeProfile;

  @POST
  @ApiSubset({WEB_APP})
  @Path("/sign")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces("application/pdf")
  @RolesAllowed({ADMIN_ROLE})
  @Operation(
      operationId = "testBitSign",
      description = "Signs an uploaded PDF via the BIT Evidence Signing API. Only available on non-production profiles."
  )
  public Response testSign(
      @RestForm("file") FileUpload file,
      @QueryParam("adminGlobalId") String adminGlobalId
  ) throws IOException {
    if (!ENABLED_PROFILES.contains(activeProfile)) {
      throw new NotFoundException(activeProfile);
    }
    byte[] signedPdf = bitSignatureApi.sign(Files.readAllBytes(file.filePath()), adminGlobalId);
    return Response.ok(signedPdf)
        .header("Content-Disposition", "attachment; filename=\"" + file.fileName() + "-signed.pdf\"")
        .build();
  }
}
