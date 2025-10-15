package ch.agridata.agreement.utils;

import ch.agridata.agreement.dto.DataRequestDescriptionDto;
import ch.agridata.agreement.dto.DataRequestPurposeDto;
import ch.agridata.agreement.dto.DataRequestTitleDto;
import ch.agridata.agreement.dto.DataRequestUpdateDto;
import ch.agridata.agreement.persistence.DataRequestDataProductEntity;
import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.common.dto.TranslationDto;
import ch.agridata.product.dto.DataProductDto;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DataRequestTestUtils {

  public static final UUID PRODUCT_ID = UUID.randomUUID();
  public static final String USER_UID = "CHE101708094";

  public static DataRequestEntity buildEntity() {
    var entity = DataRequestEntity.builder()
        .id(UUID.randomUUID())
        .stateCode(DataRequestEntity.DataRequestStateEnum.DRAFT)
        .dataConsumerUid(USER_UID)
        .build();
    entity.setDataProducts(new ArrayList<>(List.of(
        new DataRequestDataProductEntity(entity, PRODUCT_ID))));
    return entity;
  }

  public static DataRequestUpdateDto buildUpdateDto() {
    return DataRequestUpdateDto.builder()
        .title(new DataRequestTitleDto("Title DE", "Title FR", "Title IT"))
        .description(new DataRequestDescriptionDto("Desc DE", "Desc FR", "Desc IT"))
        .purpose(new DataRequestPurposeDto("Purpose DE", "Purpose FR", "Purpose IT"))
        .products(List.of(UUID.randomUUID()))
        .build();
  }

  public static DataProductDto buildDataProductDto(UUID id) {
    return DataProductDto.builder()
        .id(id)
        .name(buildTranslationDto("Product DE", "Product FR", "Product IT"))
        .description(buildTranslationDto("Description DE", "Description FR", "Description IT"))
        .build();
  }

  public static TranslationDto buildTranslationDto(String de, String fr, String it) {
    return TranslationDto.builder()
        .de(de)
        .fr(fr)
        .it(it)
        .build();
  }

}
