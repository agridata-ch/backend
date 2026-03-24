package ch.agridata.agreement.service;

import ch.agridata.agreement.dto.ContractRevisionDto;
import ch.agridata.agreement.dto.SignatureSlotCodeEnum;
import ch.agridata.agreement.mapper.ContractRevisionMapper;
import ch.agridata.agreement.persistence.ContractRevisionEntity;
import ch.agridata.agreement.persistence.ContractRevisionRepository;
import ch.agridata.common.security.AgridataSecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

/**
 * Provides business logic for contract revision signatures. It handles the signing process, verification, and persistence.
 *
 * @CommentLastReviewed: 2026-03-19
 */

@ApplicationScoped
@RequiredArgsConstructor
public class ContractRevisionSignatureService {

  private final ContractRevisionRepository contractRevisionRepository;
  private final ContractRevisionMapper contractRevisionMapper;
  private final OtpChallengeService otpChallengeService;
  private final AgridataSecurityIdentity agridataSecurityIdentity;

  @Transactional
  public ContractRevisionDto signContractRevision(
      UUID contractRevisionId,
      SignatureSlotCodeEnum signatureSlotCode,
      UUID verificationId,
      String otpCode
  ) {
    ContractRevisionEntity revisionToSign =
        contractRevisionRepository
            .findByIdAndDataConsumerUid(
                contractRevisionId,
                agridataSecurityIdentity.getUidOrElseThrow()
            )
            .orElseThrow(() -> new ValidationException("Contract revision not found"));

    verifyContractRevisionIsCurrent(contractRevisionId, revisionToSign);
    verifySignatureSlotCode(signatureSlotCode);
    verifySlotNotAlreadySigned(revisionToSign, signatureSlotCode);
    verifyUserHasNotSignedAlready(revisionToSign, agridataSecurityIdentity.getUserId());

    otpChallengeService.verifyAndConsume(
        verificationId,
        agridataSecurityIdentity.getUserId(),
        contractRevisionId,
        signatureSlotCode,
        otpCode
    );

    ContractRevisionEntity newRevision =
        contractRevisionMapper.toNextRevisionEntity(revisionToSign);

    String userFullName = agridataSecurityIdentity.getUserInfoOrElseThrow().getFirstName()
        + " " + agridataSecurityIdentity.getUserInfoOrElseThrow().getFamilyName();

    applyConsumerSignature(
        newRevision,
        signatureSlotCode,
        agridataSecurityIdentity.getUserId(),
        userFullName,
        LocalDateTime.now()
    );

    contractRevisionRepository.persist(newRevision);
    revisionToSign.getDataRequest().setCurrentContractRevisionId(newRevision.getId());

    return contractRevisionMapper.toDto(newRevision);
  }

  private static void verifyContractRevisionIsCurrent(UUID contractRevisionId, ContractRevisionEntity revisionToSign) {
    UUID currentActiveId = revisionToSign.getDataRequest().getCurrentContractRevisionId();
    if (!contractRevisionId.equals(currentActiveId)) {
      throw new ValidationException("This contract version is no longer current. Please refresh.");
    }
  }

  private void verifyUserHasNotSignedAlready(ContractRevisionEntity currentRevision, UUID userId) {
    boolean isFirstSigner = Objects.equals(currentRevision.getConsumerSignatureUserId1(), userId);
    boolean isSecondSigner = Objects.equals(currentRevision.getConsumerSignatureUserId2(), userId);

    if (isFirstSigner || isSecondSigner) {
      throw new ValidationException("User has already signed this contract revision");
    }
  }

  private void verifySignatureSlotCode(SignatureSlotCodeEnum signatureSlotCode) {
    if (signatureSlotCode == null
        || (!signatureSlotCode.equals(SignatureSlotCodeEnum.DATA_CONSUMER_01)
        && !signatureSlotCode.equals(SignatureSlotCodeEnum.DATA_CONSUMER_02))) {
      throw new ValidationException("Invalid signature slot id");
    }
  }

  private void verifySlotNotAlreadySigned(ContractRevisionEntity revision, SignatureSlotCodeEnum signatureSlotCode) {
    boolean alreadySigned = switch (signatureSlotCode) {
      case SignatureSlotCodeEnum.DATA_CONSUMER_01 -> revision.getConsumerSignatureTimestamp1() != null;
      case SignatureSlotCodeEnum.DATA_CONSUMER_02 -> revision.getConsumerSignatureTimestamp2() != null;
      default -> throw new ValidationException("Signature slot already signed:" + signatureSlotCode);
    };

    if (alreadySigned) {
      throw new ValidationException("Signature already exists for this slot");
    }
  }

  private void applyConsumerSignature(
      ContractRevisionEntity revision,
      SignatureSlotCodeEnum signatureSlotCode,
      java.util.UUID userId,
      String name,
      LocalDateTime timestamp
  ) {
    switch (signatureSlotCode) {
      case SignatureSlotCodeEnum.DATA_CONSUMER_01 -> {
        revision.setConsumerSignatureUserId1(userId);
        revision.setConsumerSignatureName1(name);
        revision.setConsumerSignatureTimestamp1(timestamp);
      }
      case SignatureSlotCodeEnum.DATA_CONSUMER_02 -> {
        revision.setConsumerSignatureUserId2(userId);
        revision.setConsumerSignatureName2(name);
        revision.setConsumerSignatureTimestamp2(timestamp);
      }
      default -> throw new ValidationException("Invalid signature slot id");
    }
  }


}
