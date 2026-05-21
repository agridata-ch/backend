package ch.agridata.agreement.utils;

import ch.agridata.agreement.dto.DataRequestDescriptionDto;
import ch.agridata.agreement.dto.DataRequestDto;
import ch.agridata.agreement.dto.DataRequestPurposeDto;
import ch.agridata.agreement.dto.DataRequestTitleDto;
import ch.agridata.agreement.dto.DataRequestUpdateDto;
import ch.agridata.agreement.persistence.ContractRevisionEntity;
import ch.agridata.agreement.persistence.DataRequestDataProductEntity;
import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.agreement.persistence.SignatureTypeEnum;
import ch.agridata.common.dto.TranslationDto;
import ch.agridata.product.dto.DataProductDto;
import ch.agridata.uidregister.dto.UidRegisterOrganisationDto;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DataRequestTestUtils {

  public static final UUID PRODUCT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  public static final UUID DATA_SOURCE_SYSTEM_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
  public static final UUID CONTRACT_REVISION_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
  public static final String USER_UID = "CHE101708094";

  public static DataRequestEntity buildEntity() {
    var entity = DataRequestEntity.builder()
        .id(UUID.randomUUID())
        .stateCode(DataRequestEntity.DataRequestStateEnum.DRAFT)
        .dataConsumerUid(USER_UID)
        .dataSourceSystemId(DATA_SOURCE_SYSTEM_ID)
        .build();
    entity.setDataProducts(new ArrayList<>(List.of(
        new DataRequestDataProductEntity(entity, PRODUCT_ID))));
    return entity;
  }

  public static DataRequestUpdateDto.DataRequestUpdateDtoBuilder updateDtoBuilder() {
    return DataRequestUpdateDto.builder()
        .title(new DataRequestTitleDto("Title DE", "Title FR", "Title IT"))
        .description(new DataRequestDescriptionDto("Desc DE", "Desc FR", "Desc IT"))
        .purpose(new DataRequestPurposeDto("Purpose DE", "Purpose FR", "Purpose IT"))
        .products(List.of(PRODUCT_ID));
  }

  public static DataRequestDto.DataRequestDtoBuilder dataRequestDtoBuilder() {
    return DataRequestDto.builder()
        .title(new DataRequestTitleDto("Title DE", "Title FR", "Title IT"))
        .description(new DataRequestDescriptionDto("Desc DE", "Desc FR", "Desc IT"))
        .purpose(new DataRequestPurposeDto("Purpose DE", "Purpose FR", "Purpose IT"))
        .products(List.of(PRODUCT_ID));
  }

  public static DataProductDto.DataProductDtoBuilder dataProductDtoBuilder(UUID id) {
    return DataProductDto.builder()
        .id(id)
        .name(buildTranslationDto("Product DE", "Product FR", "Product IT"))
        .description(buildTranslationDto("Description DE", "Description FR", "Description IT"));
  }

  public static TranslationDto buildTranslationDto(String de, String fr, String it) {
    return TranslationDto.builder()
        .de(de)
        .fr(fr)
        .it(it)
        .build();
  }

  public static UidRegisterOrganisationDto buildUidSearchResult() {
    return UidRegisterOrganisationDto.builder()
        .uid(USER_UID)
        .legalName("Test Organisation")
        .build();
  }

  public static ContractRevisionEntity buildContractRevision() {
    DataRequestEntity dataRequest = new DataRequestEntity();
    dataRequest.setCurrentContractRevisionId(CONTRACT_REVISION_ID);
    dataRequest.setConsumerSignatureType(SignatureTypeEnum.COLLECTIVE_SIGNATURE);

    return ContractRevisionEntity.builder()
        .id(CONTRACT_REVISION_ID)
        .dataRequest(dataRequest)
        .build();
  }
}
