package ch.agridata.agreement.mapper;

import ch.agridata.agreement.dto.ContractRevisionDto;
import ch.agridata.agreement.dto.ContractRevisionSignatureDto;
import ch.agridata.agreement.dto.DataRequestContextDto;
import ch.agridata.agreement.dto.SealAttemptStateEnum;
import ch.agridata.agreement.dto.SignatureSlotCodeEnum;
import ch.agridata.agreement.persistence.ContractRevisionEntity;
import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.uidregister.dto.UidRegisterOrganisationDto;
import java.util.ArrayList;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Maps contract revision entities and DTOs and creates initial contract revisions from data requests.
 *
 * @CommentLastReviewed: 2026-03-16
 */

@Mapper(componentModel = "jakarta", uses = DataRequestMapper.class)
public interface ContractRevisionMapper {
  @Mapping(target = "dataRequestId", source = "dataRequest.id")
  @Mapping(target = "dataRequestContext", source = "dataRequest")
  @Mapping(target = "consumerSignatures", expression = "java(mapConsumerSignatures(entity))")
  @Mapping(target = "providerSignatures", expression = "java(mapProviderSignatures(entity))")
  ContractRevisionDto toDto(ContractRevisionEntity entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "dataRequest", source = "dataRequest")
  @Mapping(target = "dataConsumerName", source = "dataRequest.dataConsumerLegalName")
  @Mapping(target = "dataConsumerStreet", source = "dataRequest.dataConsumerStreet")
  @Mapping(target = "dataConsumerZip", source = "dataRequest.dataConsumerZip")
  @Mapping(target = "dataConsumerCity", source = "dataRequest.dataConsumerCity")
  @Mapping(target = "dataProviderName", source = "dataProvider.legalName")
  @Mapping(target = "dataProviderZip", source = "dataProvider.address.zip")
  @Mapping(target = "dataProviderStreet", source = "dataProvider.address.street")
  @Mapping(target = "dataProviderCity", source = "dataProvider.address.city")
  @Mapping(target = "consumerSignatureUserId1", ignore = true)
  @Mapping(target = "consumerSignatureName1", ignore = true)
  @Mapping(target = "consumerSignatureTimestamp1", ignore = true)
  @Mapping(target = "consumerSignatureUserId2", ignore = true)
  @Mapping(target = "consumerSignatureName2", ignore = true)
  @Mapping(target = "consumerSignatureTimestamp2", ignore = true)
  @Mapping(target = "providerSignatureUserId1", ignore = true)
  @Mapping(target = "providerSignatureName1", ignore = true)
  @Mapping(target = "providerSignatureTimestamp1", ignore = true)
  @Mapping(target = "providerSignatureUserId2", ignore = true)
  @Mapping(target = "providerSignatureName2", ignore = true)
  @Mapping(target = "providerSignatureTimestamp2", ignore = true)
  @Mapping(target = "sealStartedAt", ignore = true)
  @Mapping(target = "sealState", ignore = true)
  ContractRevisionEntity toInitialEntity(DataRequestEntity dataRequest, UidRegisterOrganisationDto dataProvider);

  @Mapping(target = "dataConsumerLogoBase64", source = "entity", qualifiedByName = "convertLogoToBase64")
  DataRequestContextDto toDataRequestContextDto(DataRequestEntity entity);

  @Mapping(target = "id", ignore = true)
  ContractRevisionEntity toNextRevisionEntity(ContractRevisionEntity previousRevision);

  SealAttemptStateEnum toSealAttemptStateEnum(ContractRevisionEntity.SealAttemptState sealAttemptState);

  default List<ContractRevisionSignatureDto> mapConsumerSignatures(ContractRevisionEntity entity) {
    List<ContractRevisionSignatureDto> result = new ArrayList<>();

    if (entity.getConsumerSignatureUserId1() != null) {
      result.add(new ContractRevisionSignatureDto(
          SignatureSlotCodeEnum.DATA_CONSUMER_01,
          entity.getConsumerSignatureUserId1(),
          entity.getConsumerSignatureName1(),
          entity.getConsumerSignatureTimestamp1()
      ));
    }

    if (entity.getConsumerSignatureUserId2() != null) {
      result.add(new ContractRevisionSignatureDto(
          SignatureSlotCodeEnum.DATA_CONSUMER_02,
          entity.getConsumerSignatureUserId2(),
          entity.getConsumerSignatureName2(),
          entity.getConsumerSignatureTimestamp2()
      ));
    }

    return result;
  }

  default List<ContractRevisionSignatureDto> mapProviderSignatures(ContractRevisionEntity entity) {
    List<ContractRevisionSignatureDto> result = new ArrayList<>();

    if (entity.getProviderSignatureUserId1() != null) {
      result.add(new ContractRevisionSignatureDto(
          SignatureSlotCodeEnum.DATA_PROVIDER_01,
          entity.getProviderSignatureUserId1(),
          entity.getProviderSignatureName1(),
          entity.getProviderSignatureTimestamp1()
      ));
    }

    if (entity.getProviderSignatureUserId2() != null) {
      result.add(new ContractRevisionSignatureDto(
          SignatureSlotCodeEnum.DATA_PROVIDER_02,
          entity.getProviderSignatureUserId2(),
          entity.getProviderSignatureName2(),
          entity.getProviderSignatureTimestamp2()
      ));
    }

    return result;
  }
}
