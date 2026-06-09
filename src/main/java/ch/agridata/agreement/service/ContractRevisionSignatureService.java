package ch.agridata.agreement.service;

import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PROVIDER_ROLE;

import ch.agridata.agreement.dto.ContractRevisionDto;
import ch.agridata.agreement.dto.SignatureSlotCodeEnum;
import ch.agridata.agreement.mapper.ContractRevisionMapper;
import ch.agridata.agreement.persistence.ContractRevisionEntity;
import ch.agridata.agreement.persistence.ContractRevisionRepository;
import ch.agridata.agreement.persistence.SignatureTypeEnum;
import ch.agridata.common.security.AgridataSecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.NotFoundException;
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
  private final DataRequestStateService dataRequestStateService;
  private final ContractRevisionQueryService contractRevisionQueryService;
  private final ContractRevisionPdfService contractRevisionPdfService;
  private final AuditingService auditingService;

  @RolesAllowed({CONSUMER_ROLE})
  @Transactional
  public ContractRevisionDto signContractRevisionAsConsumer(
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
    verifySlotNotAlreadySigned(revisionToSign, signatureSlotCode);
    verifyUserHasNotSignedAlready(revisionToSign, agridataSecurityIdentity.getUserId());
    verifySignatureSlotOrder(revisionToSign, signatureSlotCode);

    otpChallengeService.verifyAndConsume(
        verificationId,
        agridataSecurityIdentity.getUserId(),
        contractRevisionId,
        signatureSlotCode,
        otpCode
    );

    ContractRevisionEntity newRevision =
        contractRevisionMapper.toNextRevisionEntity(revisionToSign);

    String userFullName = agridataSecurityIdentity.getUserInfoOrElseThrow().getString("given_name")
        + " " + agridataSecurityIdentity.getUserInfoOrElseThrow().getString("family_name");

    applyConsumerSignature(
        newRevision,
        signatureSlotCode,
        agridataSecurityIdentity.getUserId(),
        userFullName,
        LocalDateTime.now()
    );

    var dataRequest = revisionToSign.getDataRequest();

    if (signatureSlotCode == SignatureSlotCodeEnum.DATA_CONSUMER_01) {
      newRevision.setConsumerSignatureType(dataRequest.getConsumerSignatureType());
    }

    contractRevisionPdfService.generateAndUploadPdf(newRevision);
    contractRevisionRepository.persist(newRevision);

    auditingService.logContractRevisionSigned(contractRevisionId, signatureSlotCode);

    dataRequest.setCurrentContractRevisionId(newRevision.getId());

    if (hasAllRequiredConsumerSignatures(newRevision)) {
      dataRequestStateService.transitionToPendingReleaseByConsumer(dataRequest);
    }

    return contractRevisionMapper.toDto(newRevision);
  }

  @RolesAllowed({PROVIDER_ROLE})
  @Transactional
  public ContractRevisionDto signContractRevisionAsProvider(
      UUID contractRevisionId,
      SignatureSlotCodeEnum signatureSlotCode,
      UUID verificationId,
      String otpCode
  ) {
    ContractRevisionEntity revisionToSign = contractRevisionRepository.findByIdOptional(contractRevisionId)
        .orElseThrow(() -> new ValidationException("Contract revision not found"));

    verifyIsAssignedToCurrentProvider(revisionToSign);
    verifyContractRevisionIsCurrent(contractRevisionId, revisionToSign);
    verifySlotNotAlreadySigned(revisionToSign, signatureSlotCode);
    verifyUserHasNotSignedAlready(revisionToSign, agridataSecurityIdentity.getUserId());
    verifySignatureSlotOrder(revisionToSign, signatureSlotCode);

    otpChallengeService.verifyAndConsume(
        verificationId,
        agridataSecurityIdentity.getUserId(),
        contractRevisionId,
        signatureSlotCode,
        otpCode
    );

    ContractRevisionEntity newRevision =
        contractRevisionMapper.toNextRevisionEntity(revisionToSign);

    String userFullName = agridataSecurityIdentity.getUserInfoOrElseThrow().getString("given_name")
        + " " + agridataSecurityIdentity.getUserInfoOrElseThrow().getString("family_name");

    applyProviderSignature(
        newRevision,
        signatureSlotCode,
        agridataSecurityIdentity.getUserId(),
        userFullName,
        LocalDateTime.now()
    );

    var dataRequest = revisionToSign.getDataRequest();

    if (signatureSlotCode == SignatureSlotCodeEnum.DATA_PROVIDER_01) {
      newRevision.setProviderSignatureType(dataRequest.getProviderSignatureType());
    }

    contractRevisionPdfService.generateAndUploadPdf(newRevision);
    contractRevisionRepository.persist(newRevision);

    auditingService.logContractRevisionSigned(contractRevisionId, signatureSlotCode);

    dataRequest.setCurrentContractRevisionId(newRevision.getId());

    if (hasAllRequiredProviderSignatures(newRevision)) {
      dataRequestStateService.transitionToPendingReleaseByProvider(dataRequest);
    }

    return contractRevisionMapper.toDto(newRevision);
  }

  private void verifyIsAssignedToCurrentProvider(ContractRevisionEntity revisionToSign) {
    if (!contractRevisionQueryService.isAssignedToCurrentProvider(revisionToSign)) {
      throw new NotFoundException(revisionToSign.getId().toString());
    }
  }

  private static boolean hasAllRequiredConsumerSignatures(ContractRevisionEntity revision) {
    return (revision.getConsumerSignatureType() == SignatureTypeEnum.INDIVIDUAL_SIGNATURE && revision.getConsumerSignatureUserId1() != null)
        ||
        (revision.getConsumerSignatureType() == SignatureTypeEnum.COLLECTIVE_SIGNATURE && revision.getConsumerSignatureUserId1() != null
            && revision.getConsumerSignatureUserId2() != null);
  }

  private static boolean hasAllRequiredProviderSignatures(ContractRevisionEntity revision) {
    return (revision.getProviderSignatureType() == SignatureTypeEnum.INDIVIDUAL_SIGNATURE && revision.getProviderSignatureUserId1() != null)
        ||
        (revision.getProviderSignatureType() == SignatureTypeEnum.COLLECTIVE_SIGNATURE && revision.getProviderSignatureUserId1() != null
            && revision.getProviderSignatureUserId2() != null);
  }

  private static void verifyContractRevisionIsCurrent(UUID contractRevisionId, ContractRevisionEntity revisionToSign) {
    UUID currentActiveId = revisionToSign.getDataRequest().getCurrentContractRevisionId();
    if (!contractRevisionId.equals(currentActiveId)) {
      throw new ValidationException("This contract version is no longer current. Please refresh.");
    }
  }

  private void verifyUserHasNotSignedAlready(ContractRevisionEntity currentRevision, UUID userId) {
    if (Objects.equals(currentRevision.getConsumerSignatureUserId1(), userId)
        || Objects.equals(currentRevision.getConsumerSignatureUserId2(), userId)
        || Objects.equals(currentRevision.getProviderSignatureUserId1(), userId)
        || Objects.equals(currentRevision.getProviderSignatureUserId2(), userId)) {
      throw new ValidationException("User has already signed this contract");
    }
  }

  private void verifySignatureSlotOrder(ContractRevisionEntity revision, SignatureSlotCodeEnum signatureSlotCode) {
    boolean slotIsValid = switch (signatureSlotCode) {
      case SignatureSlotCodeEnum.DATA_CONSUMER_02 -> revision.getConsumerSignatureUserId1() != null;
      case SignatureSlotCodeEnum.DATA_PROVIDER_02 -> revision.getProviderSignatureUserId1() != null;
      default -> true;
    };

    if (!slotIsValid) {
      throw new ValidationException("Must sign first signature slot before signing the second one");
    }
  }

  private void verifySlotNotAlreadySigned(ContractRevisionEntity revision, SignatureSlotCodeEnum signatureSlotCode) {
    boolean alreadySigned = switch (signatureSlotCode) {
      case SignatureSlotCodeEnum.DATA_CONSUMER_01 -> revision.getConsumerSignatureTimestamp1() != null;
      case SignatureSlotCodeEnum.DATA_CONSUMER_02 -> revision.getConsumerSignatureTimestamp2() != null;
      case SignatureSlotCodeEnum.DATA_PROVIDER_01 -> revision.getProviderSignatureTimestamp1() != null;
      case SignatureSlotCodeEnum.DATA_PROVIDER_02 -> revision.getProviderSignatureTimestamp2() != null;
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
      default -> throw new ValidationException("Invalid consumer signature slot id");
    }
  }

  private void applyProviderSignature(
      ContractRevisionEntity revision,
      SignatureSlotCodeEnum signatureSlotCode,
      java.util.UUID userId,
      String name,
      LocalDateTime timestamp
  ) {
    switch (signatureSlotCode) {
      case SignatureSlotCodeEnum.DATA_PROVIDER_01 -> {
        revision.setProviderSignatureUserId1(userId);
        revision.setProviderSignatureName1(name);
        revision.setProviderSignatureTimestamp1(timestamp);
      }
      case SignatureSlotCodeEnum.DATA_PROVIDER_02 -> {
        revision.setProviderSignatureUserId2(userId);
        revision.setProviderSignatureName2(name);
        revision.setProviderSignatureTimestamp2(timestamp);
      }
      default -> throw new ValidationException("Invalid provider signature slot id");
    }
  }
}
