package ch.agridata.agreement.service;

import static ch.agridata.common.utils.AuthenticationUtil.CONSUMER_ROLE;
import static ch.agridata.common.utils.AuthenticationUtil.PROVIDER_ROLE;

import ch.agridata.agreement.dto.DataRequestDto;
import ch.agridata.agreement.dto.SignatureTypeEnum;
import ch.agridata.agreement.mapper.ContractRevisionMapper;
import ch.agridata.agreement.persistence.ContractRevisionEntity;
import ch.agridata.agreement.persistence.ContractRevisionRepository;
import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.agreement.persistence.DataRequestRepository;
import ch.agridata.common.security.AgridataSecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.NotFoundException;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;

/**
 * Service for changing the signature type of a data request for the currently
 * authenticated consumer or provider.
 *
 * <p>The signature type may only be changed while the data request is waiting
 * for the respective party to sign it. Once either signature slot of that party
 * has been signed, the signature type can no longer be changed.</p>
 *
 * <p>Consumers may only update data requests assigned to their own UID.
 * Providers may only update data requests assigned to them according to the
 * provider access rules in {@link DataRequestQueryService}.</p>
 *
 * @CommentLastReviewed 2026-04-29
 */

@ApplicationScoped
@RequiredArgsConstructor
public class DataRequestSignatureTypeMutationService {

  private final DataRequestRepository dataRequestRepository;
  private final ContractRevisionRepository contractRevisionRepository;
  private final DataRequestEnrichmentService dataRequestEnrichmentService;
  private final DataRequestQueryService dataRequestQueryService;
  private final AgridataSecurityIdentity agridataSecurityIdentity;
  private final ContractRevisionMapper contractRevisionMapper;
  private final AuditingService auditingService;

  @Transactional
  @RolesAllowed(CONSUMER_ROLE)
  public DataRequestDto updateSignatureTypeAsConsumer(UUID id, SignatureTypeEnum signatureType) {
    var dataRequest = dataRequestRepository.findByIdAndDataConsumerUid(id, agridataSecurityIdentity.getUidOrElseThrow())
        .orElseThrow(() -> new NotFoundException(id.toString()));

    return updateSignatureType(
        dataRequest,
        signatureType,
        DataRequestEntity.DataRequestStateEnum.TO_BE_SIGNED_BY_CONSUMER,
        ContractRevisionEntity::getConsumerSignatureUserId1,
        DataRequestEntity::setConsumerSignatureType
    );
  }

  @Transactional
  @RolesAllowed(PROVIDER_ROLE)
  public DataRequestDto updateSignatureTypeAsProvider(UUID id, SignatureTypeEnum signatureType) {
    var dataRequest = dataRequestRepository.findByIdOptional(id)
        .filter(dataRequestQueryService::isAssignedToCurrentProvider)
        .orElseThrow(() -> new NotFoundException(id.toString()));

    return updateSignatureType(
        dataRequest,
        signatureType,
        DataRequestEntity.DataRequestStateEnum.TO_BE_SIGNED_BY_PROVIDER,
        ContractRevisionEntity::getProviderSignatureUserId1,
        DataRequestEntity::setProviderSignatureType
    );
  }

  private DataRequestDto updateSignatureType(
      DataRequestEntity dataRequest,
      SignatureTypeEnum signatureType,
      DataRequestEntity.DataRequestStateEnum requiredState,
      Function<ContractRevisionEntity, UUID> signatureUserId1Getter,
      BiConsumer<DataRequestEntity, ch.agridata.agreement.persistence.SignatureTypeEnum> signatureTypeSetter
  ) {

    verifyDataRequestIsInRequiredState(dataRequest, requiredState);

    var contractRevision = contractRevisionRepository
        .findByIdOptional(dataRequest.getCurrentContractRevisionId())
        .orElseThrow(() -> new IllegalStateException("ContractRevision " + dataRequest.getCurrentContractRevisionId() + " does not exist"));

    verifyNoSignatureApplied(contractRevision, signatureUserId1Getter);

    signatureTypeSetter.accept(dataRequest, contractRevisionMapper.toEntitySignatureType(signatureType));

    auditingService.logSignatureTypeChosen(dataRequest.getId(), signatureType, requiredState);

    return dataRequestEnrichmentService.toEnrichedDto(dataRequest);
  }

  private void verifyDataRequestIsInRequiredState(
      DataRequestEntity dataRequestEntity,
      DataRequestEntity.DataRequestStateEnum requiredState
  ) {
    if (dataRequestEntity.getStateCode() != requiredState) {
      throw new ValidationException("Signature type can only be changed when data request is in state " + requiredState);
    }
  }

  private void verifyNoSignatureApplied(
      ContractRevisionEntity contractRevision,
      Function<ContractRevisionEntity, UUID> signatureUserId1Getter
  ) {
    if (signatureUserId1Getter.apply(contractRevision) != null) {
      throw new ValidationException("Signature type can only be changed before a signature has been applied");
    }
  }
}
