package ch.agridata.agreement.mapper;

import ch.agridata.agreement.dto.ContractRevisionDto;
import ch.agridata.agreement.persistence.ContractRevisionEntity;
import ch.agridata.agreement.persistence.DataRequestEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Maps contract revision entities and DTOs and creates initial contract revisions from data requests.
 *
 * @CommentLastReviewed: 2026-03-16
 */

@Mapper(componentModel = "jakarta")
public interface ContractRevisionMapper {
  @Mapping(target = "dataRequestId", source = "dataRequest.id")
  ContractRevisionDto toDto(ContractRevisionEntity entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "dataRequest", source = "dataRequest")
  @Mapping(target = "dataConsumerName", source = "dataRequest.dataConsumerLegalName")
  @Mapping(target = "dataProviderName", source = "dataProviderName")
  ContractRevisionEntity toInitialEntity(DataRequestEntity dataRequest, String dataProviderName);

}
