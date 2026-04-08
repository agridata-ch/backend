package ch.agridata.agreement.controller;

import static ch.agridata.agreement.controller.ContractRevisionController.PATH;
import static ch.agridata.common.openapi.ApiSubsetConstants.WEB_APP;
import static ch.agridata.common.utils.AuthenticationUtil.ADMIN_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PROVIDER_ROLE;

import ch.agridata.agreement.dto.ContractRevisionDto;
import ch.agridata.agreement.dto.OtpChallengeDto;
import ch.agridata.agreement.dto.SignatureSlotCodeEnum;
import ch.agridata.agreement.dto.VerifyOtpRequestDto;
import ch.agridata.agreement.service.ContractRevisionOtpChallengeService;
import ch.agridata.agreement.service.ContractRevisionQueryService;
import ch.agridata.agreement.service.ContractRevisionSealService;
import ch.agridata.agreement.service.ContractRevisionSignatureService;
import ch.agridata.common.openapi.ApiSubset;
import ch.agridata.common.security.AgridataSecurityIdentity;
import io.smallrye.common.annotation.RunOnVirtualThread;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Provides an endpoint for retrieving contract revisions for consumers and providers.
 *
 * @CommentLastReviewed 2026-03-16
 */

@Slf4j
@Path(PATH)
@RequiredArgsConstructor
@Tag(
    name = "Contract Revisions",
    description = "Provides access to contract revisions for consumers and providers"
)
@RunOnVirtualThread
public class ContractRevisionController {
  public static final String PATH = "/api/agreement/v1/contract-revisions";

  private final ContractRevisionQueryService contractRevisionQueryService;
  private final ContractRevisionOtpChallengeService contractRevisionOtpChallengeService;
  private final ContractRevisionSignatureService contractRevisionSignatureService;
  private final ContractRevisionSealService contractRevisionSealService;
  private final AgridataSecurityIdentity agridataSecurityIdentity;

  @GET
  @ApiSubset({WEB_APP})
  @Path("/{id}")
  @Operation(
      operationId = "getContractRevision",
      description = "Retrieves a specific contract revision by its ID. Accessible by the consumer "
          + "or provider that owns the associated datarequest."
  )
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({CONSUMER_ROLE, PROVIDER_ROLE})
  public ContractRevisionDto getContractRevision(@PathParam("id") UUID id) {
    if (agridataSecurityIdentity.isProvider()) {
      return contractRevisionQueryService.getContractRevisionOfCurrentProvider(id);
    }
    return contractRevisionQueryService.getContractRevisionOfCurrentConsumer(id);
  }

  @POST
  @ApiSubset({WEB_APP})
  @Path("/{id}/signatures/{slotCode}/otp-challenges")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
      operationId = "initiateSignatureChallenge",
      description = "Initiates a challenge for a specific signature slot of a specific contract revision."
  )
  @RolesAllowed({CONSUMER_ROLE, PROVIDER_ROLE})
  public OtpChallengeDto initiateSignatureChallenge(
      @Parameter(
          description = "ID of the contract revision",
          required = true,
          example = "979ABA0B-8C00-4B4F-9621-77C086AD2333"
      )
      @PathParam("id") UUID id,
      @Parameter(
          description = "Code for identifying the slot for the signature",
          required = true,
          example = "DATA_CONSUMER_01"
      )
      @PathParam("slotCode") SignatureSlotCodeEnum slotCode
  ) {
    return contractRevisionOtpChallengeService.createOtpChallenge(
        id,
        slotCode
    );
  }

  @POST
  @ApiSubset({WEB_APP})
  @Path("/{id}/signatures/{slotCode}/otp-challenges/{challengeId}/verification")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
      operationId = "verifySignature",
      description = "Verifies the otp and adds a signature to a specific signature slot."
  )
  @RolesAllowed({CONSUMER_ROLE, PROVIDER_ROLE})
  public ContractRevisionDto verifySignature(
      @Parameter(
          description = "ID of the contract revision",
          required = true,
          example = "98EC39D2-E78C-4C48-A58B-E1B251A44A61"
      )
      @PathParam("id") UUID id,
      @Parameter(
          description = "Code for identifying the slot for the signature",
          required = true,
          example = "DATA_CONSUMER_01"
      )
      @PathParam("slotCode") SignatureSlotCodeEnum slotCode,
      @Parameter(
          description = "ID of the challenge that should be verified",
          required = true,
          example = "D74D4C75-4F08-4050-B98C-7259EBABEEC5"
      )
      @PathParam("challengeId") UUID challengeId,
      @Parameter(
          description = "Contains the otp code for the verification",
          required = true
      )
      @Valid VerifyOtpRequestDto request
  ) {
    return contractRevisionSignatureService.signContractRevision(
        id,
        slotCode,
        challengeId,
        request.otpCode()
    );
  }

  @POST
  @ApiSubset({WEB_APP})
  @Path("/{id}/seals")
  @Produces("application/pdf")
  @Operation(
      operationId = "sealContractRevision",
      description = "Seals a contract revision PDF via BIT Evidence Signing API."
  )
  @RolesAllowed({ADMIN_ROLE})
  public Response sealContractRevision(
      @PathParam("id") UUID id,
      // TODO: Remove this parameter once adminGlobalId is available in the agate token and can be read from the AgridataSecurityIdentity
      @QueryParam("adminGlobalId") String adminGlobalId
  ) {
    byte[] sealedPdf = contractRevisionSealService.seal(id, adminGlobalId);
    return Response.ok(sealedPdf)
        .header("Content-Disposition", String.format("attachment; filename=\"%s-sealed.pdf\"", id))
        .build();
  }
}
