package ch.agridata.agreement.service;

import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PROVIDER_ROLE;

import ch.agridata.agreement.dto.OtpChallengeDto;
import ch.agridata.agreement.dto.SignatureSlotCodeEnum;
import ch.agridata.agreement.persistence.ContractRevisionRepository;
import ch.agridata.common.security.AgridataSecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

/**
 * Handles OTP challenges for contract revision signatures. It generates challenges and manages their expiration.
 *
 * @CommentLastReviewed: 2026-06-09
 */

@ApplicationScoped
@RequiredArgsConstructor
public class ContractRevisionOtpChallengeService {
  private final OtpChallengeService otpChallengeService;
  private final AgridataSecurityIdentity agridataSecurityIdentity;
  private final ContractRevisionRepository contractRevisionRepository;
  private final ContractRevisionQueryService contractRevisionQueryService;

  @RolesAllowed({CONSUMER_ROLE})
  public OtpChallengeDto createOtpChallengeAsConsumer(
      UUID contractRevisionId,
      SignatureSlotCodeEnum signatureSlotCode
  ) {
    verifyConsumerOwnership(contractRevisionId);
    verifyConsumerSlot(signatureSlotCode);
    return buildOtpChallenge(contractRevisionId, signatureSlotCode);
  }

  @RolesAllowed({PROVIDER_ROLE})
  public OtpChallengeDto createOtpChallengeAsProvider(
      UUID contractRevisionId,
      SignatureSlotCodeEnum signatureSlotCode
  ) {
    verifyIsAssignedToCurrentProvider(contractRevisionId);
    verifyProviderSlot(signatureSlotCode);
    return buildOtpChallenge(contractRevisionId, signatureSlotCode);
  }

  private OtpChallengeDto buildOtpChallenge(UUID contractRevisionId, SignatureSlotCodeEnum signatureSlotCode) {
    var challenge = otpChallengeService.createChallenge(
        agridataSecurityIdentity.getUserId(),
        contractRevisionId,
        signatureSlotCode,
        agridataSecurityIdentity.getMobileNumberOrElseThrow()
    );

    return new OtpChallengeDto(
        challenge.getId(),
        challenge.getExpiresAt(),
        maskPhoneNumber(challenge.getPhoneNumberSnapshot()),
        otpChallengeService.getResendCooldown().toSeconds()
    );
  }

  private void verifyIsAssignedToCurrentProvider(UUID contractRevisionId) {
    if (!contractRevisionQueryService.isAssignedToCurrentProvider(contractRevisionId)) {
      throw new NotFoundException(contractRevisionId.toString());
    }
  }

  private static void verifyConsumerSlot(SignatureSlotCodeEnum signatureSlotCode) {
    if (signatureSlotCode != SignatureSlotCodeEnum.DATA_CONSUMER_01
        && signatureSlotCode != SignatureSlotCodeEnum.DATA_CONSUMER_02) {
      throw new IllegalArgumentException("Invalid consumer signature slot id=" + signatureSlotCode);
    }
  }

  private void verifyConsumerOwnership(UUID contractRevisionId) {
    contractRevisionRepository
        .findByIdAndDataConsumerUid(
            contractRevisionId,
            agridataSecurityIdentity.getUidOrElseThrow()
        )
        .orElseThrow(() -> new NotFoundException(contractRevisionId.toString()));
  }

  private static void verifyProviderSlot(SignatureSlotCodeEnum signatureSlotCode) {
    if (signatureSlotCode != SignatureSlotCodeEnum.DATA_PROVIDER_01
        && signatureSlotCode != SignatureSlotCodeEnum.DATA_PROVIDER_02) {
      throw new IllegalArgumentException("Invalid provider signature slot id=" + signatureSlotCode);
    }
  }

  private String maskPhoneNumber(String phoneNumber) {
    if (phoneNumber.length() < 10) {
      return "****";
    }
    String phoneNumberWithoutWhitespaces = phoneNumber.replaceAll("\\s+", "");
    return phoneNumberWithoutWhitespaces.substring(0, 3) + " "
        + "*".repeat(Math.max(0, phoneNumberWithoutWhitespaces.length() - 5))
        + phoneNumberWithoutWhitespaces.substring(phoneNumberWithoutWhitespaces.length() - 2);
  }
}
