package ch.agridata.agreement.service;

import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;

import ch.agridata.agreement.dto.OtpChallengeDto;
import ch.agridata.agreement.dto.SignatureSlotCodeEnum;
import ch.agridata.agreement.persistence.ContractRevisionRepository;
import ch.agridata.common.security.AgridataSecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.ValidationException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

/**
 * Handles OTP challenges for contract revision signatures. It generates challenges and manages their expiration.
 *
 * @CommentLastReviewed: 2026-03-19
 */

@ApplicationScoped
@RequiredArgsConstructor
public class ContractRevisionOtpChallengeService {
  private final OtpChallengeService otpChallengeService;
  private final AgridataSecurityIdentity agridataSecurityIdentity;
  private final ContractRevisionRepository contractRevisionRepository;

  @RolesAllowed({CONSUMER_ROLE})
  public OtpChallengeDto createOtpChallenge(
      UUID contractRevisionId,
      SignatureSlotCodeEnum signatureSlotCode
  ) {
    contractRevisionRepository
        .findByIdAndDataConsumerUid(
            contractRevisionId,
            agridataSecurityIdentity.getUidOrElseThrow()
        )
        .orElseThrow(() -> new ValidationException("Contract revision not found"));

    if (signatureSlotCode != SignatureSlotCodeEnum.DATA_CONSUMER_01 && signatureSlotCode != SignatureSlotCodeEnum.DATA_CONSUMER_02) {
      throw new IllegalArgumentException("Invalid signature slot id");
    }

    var challenge = otpChallengeService.createChallenge(
        agridataSecurityIdentity.getUserId(),
        contractRevisionId,
        signatureSlotCode,
        agridataSecurityIdentity.getPhoneNumberOrElseThrow()
    );

    return new OtpChallengeDto(
        challenge.getId(),
        challenge.getExpiresAt(),
        maskPhoneNumber(challenge.getPhoneNumberSnapshot()),
        otpChallengeService.getResendCooldown().toSeconds()
    );
  }

  private String maskPhoneNumber(String phoneNumber) {
    if (phoneNumber == null || phoneNumber.length() < 4) {
      return "****";
    }
    return "*".repeat(Math.max(0, phoneNumber.length() - 2)) + phoneNumber.substring(phoneNumber.length() - 2);
  }
}
