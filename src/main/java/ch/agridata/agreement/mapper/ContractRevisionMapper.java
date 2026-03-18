package ch.agridata.agreement.mapper;

import ch.agridata.agreement.dto.ContractRevisionDto;
import ch.agridata.agreement.dto.ContractRevisionSignatureDto;
import ch.agridata.agreement.dto.DataRequestContextDto;
import ch.agridata.agreement.dto.SignatureSlotCodeEnum;
import ch.agridata.agreement.persistence.ContractRevisionEntity;
import ch.agridata.agreement.persistence.DataRequestEntity;
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
  ContractRevisionDto toDto(ContractRevisionEntity entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "dataRequest", source = "dataRequest")
  @Mapping(target = "dataConsumerName", source = "dataRequest.dataConsumerLegalName")
  @Mapping(target = "dataProviderName", source = "dataProviderName")
  @Mapping(target = "consumerSignatureUserId1", ignore = true)
  @Mapping(target = "consumerSignatureName1", ignore = true)
  @Mapping(target = "consumerSignatureTimestamp1", ignore = true)
  @Mapping(target = "consumerSignatureUserId2", ignore = true)
  @Mapping(target = "consumerSignatureName2", ignore = true)
  @Mapping(target = "consumerSignatureTimestamp2", ignore = true)
  ContractRevisionEntity toInitialEntity(DataRequestEntity dataRequest, String dataProviderName);

  @Mapping(target = "dataConsumerLogoBase64", source = "entity", qualifiedByName = "convertLogoToBase64")
  DataRequestContextDto toDataRequestContextDto(DataRequestEntity entity);

  @Mapping(target = "id", ignore = true)
  ContractRevisionEntity toNextRevisionEntity(ContractRevisionEntity previousRevision);

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
}
