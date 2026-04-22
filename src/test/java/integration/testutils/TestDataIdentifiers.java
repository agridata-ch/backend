package integration.testutils;

import static ch.agridata.user.dto.LegalFormEnum.AKTIENGESELLSCHAFT;
import static ch.agridata.user.dto.LegalFormEnum.EINFACHE_GESELLSCHAFT;
import static ch.agridata.user.dto.LegalFormEnum.EQUIDENEIGENTUEMER;
import static ch.agridata.user.dto.LegalFormEnum.NATUERLICHE_PERSON;

import ch.agridata.agreement.persistence.ConsentRequestEntity;
import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.product.persistence.DataProductEntity;
import ch.agridata.product.persistence.DataProviderEntity;
import ch.agridata.user.dto.LegalFormEnum;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

public class TestDataIdentifiers {

  private static <E> Identifier<E> id(String uuid) {
    return new Identifier<>(UUID.fromString(uuid));
  }

  public record Identifier<E>(UUID uuid) {
    @Override
    public @NotNull String toString() {
      return uuid.toString();
    }
  }

  public static class DataRequest {
    public static final Identifier<DataRequestEntity> BIO_SUISSE_01 = id("3da3a459-d3c2-48af-b8d0-02bc95146468");
    public static final Identifier<DataRequestEntity> BIO_SUISSE_02 = id("81ae8571-9497-413a-99c5-237e72621ca7");
    public static final Identifier<DataRequestEntity> BIO_SUISSE_DRAFT = id("dbc991ef-b717-48d7-94da-e475e607e8bc");
    public static final Identifier<DataRequestEntity> IP_SUISSE_01 = id("98a35e61-0162-4986-9e9e-ee5c65f86316");
    public static final Identifier<DataRequestEntity> IP_SUISSE_02 = id("341f558a-781c-4eb5-bab7-c2f39216b9f2");
    public static final Identifier<DataRequestEntity> BLV_1 = id("218bca06-e792-4855-bcd9-e3559cea3d18");
  }

  public static class ConsentRequest {
    public static final Identifier<ConsentRequestEntity> BIO_SUISSE_01_CHE101000001 = id("07813a3a-7b8d-4b68-847b-f34ce7037397");
    public static final Identifier<ConsentRequestEntity> BIO_SUISSE_01_CHE101000001_99910003 = id("1d2025b5-424b-489d-a3c1-30464661f723");
    public static final Identifier<ConsentRequestEntity> BIO_SUISSE_01_CHE102000001 = id("94e4f8e3-70b1-43ae-bdfa-78b27f86958e");
    public static final Identifier<ConsentRequestEntity> BIO_SUISSE_01_CHE102000002 = id("f789e5ca-3b26-4ced-bcce-77df72ac06ac");

    public static final Identifier<ConsentRequestEntity> BIO_SUISSE_02_CHE102000001 = id("2643cbd7-8077-4378-8c47-27d2b31dd554");
    public static final Identifier<ConsentRequestEntity> BIO_SUISSE_02_CHE102000002 = id("1272346f-7983-4845-b038-329116d67e08");
    public static final Identifier<ConsentRequestEntity> BIO_SUISSE_02_CHE103000001 = id("235aed61-7da2-41ac-94c4-bcca91328ad6");
    public static final Identifier<ConsentRequestEntity> BIO_SUISSE_02_CHE103000002 = id("584c3587-517b-49ef-aaec-cb6e0179f78c");

    public static final Identifier<ConsentRequestEntity> IP_SUISSE_01_CHE101000001 = id("ef35df35-2051-416a-98ad-47ab35c8a77c");
    public static final Identifier<ConsentRequestEntity> IP_SUISSE_01_CHE102000002 = id("2f8ec662-9fce-417e-9b82-3ed042adb482");
    public static final Identifier<ConsentRequestEntity> IP_SUISSE_01_CHE103000001 = id("5e439777-8564-4954-9d01-7ebeabf4fc39");
    public static final Identifier<ConsentRequestEntity> IP_SUISSE_01_CHE103000002 = id("adbc6d5a-331d-4dd9-b80c-ee1945716293");

    public static final Identifier<ConsentRequestEntity> IP_SUISSE_02_CHE101000001 = id("5542ff84-ab93-417a-925a-9c7711a20fff");
    public static final Identifier<ConsentRequestEntity> IP_SUISSE_02_CHE102000001 = id("68c0e430-00b8-44ca-a2f7-be6197ff64a9");
    public static final Identifier<ConsentRequestEntity> IP_SUISSE_02_CHE102000002 = id("fea87d49-857a-45e4-8274-fec6885697c4");
    public static final Identifier<ConsentRequestEntity> IP_SUISSE_02_CHE103000001 = id("0dd93ea9-5a14-40a4-a36e-eafb2585ece3");
    public static final Identifier<ConsentRequestEntity> IP_SUISSE_02_CHE103000002 = id("629322ea-19ea-42a2-8bb9-1d1d2e47dda0");
  }

  public static class DataProduct {
    // AGIS
    public static final Identifier<DataProductEntity> UUID_C661EA48 = id("c661ea48-106d-4d7a-a5d1-a9a6db48dd8c");
    public static final Identifier<DataProductEntity> UUID_147E8C40 = id("147e8c40-78cc-4db3-a909-65504aa62a64");
    public static final Identifier<DataProductEntity> UUID_085E4B72 = id("085e4b72-964d-4bd5-a3c9-224d8c5585af");
    public static final Identifier<DataProductEntity> UUID_A795D0B0 = id("a795d0b0-f177-4bb4-8e41-1ed12d358c79");
    public static final Identifier<DataProductEntity> UUID_0A808700 = id("0a808700-d89e-4fa0-a2b8-8edb15f3addd");
    public static final Identifier<DataProductEntity> UUID_EF4F42DD = id("ef4f42dd-eaa9-4af1-988c-86b47bd963fe");
    public static final Identifier<DataProductEntity> UUID_2375219C = id("2375219c-5fe3-458f-bd07-d3c2c87e2539");
    public static final Identifier<DataProductEntity> UUID_64E39DF0 = id("64e39df0-2e56-4204-9c44-a43e1e26a2e8");
    public static final Identifier<DataProductEntity> UUID_1DAD9F91 = id("1dad9f91-30d8-45c9-8c82-ad72f4cb22e7");
    public static final Identifier<DataProductEntity> UUID_46F8A883 = id("46f8a883-da7c-49b3-b986-10a24b1e09ef");
    public static final Identifier<DataProductEntity> UUID_7911D98D = id("7911d98d-59eb-4cf4-be61-bfe77fe9117e");
    // Animal Tracing
    public static final Identifier<DataProductEntity> UUID_6319423C = id("6319423c-e4fc-4a47-be6e-43888f58f94f");
    public static final Identifier<DataProductEntity> UUID_C4D3B0A3 = id("c4d3b0a3-b486-40b8-a324-5029034433cc");
    public static final Identifier<DataProductEntity> UUID_3E0BFD53 = id("3e0bfd53-94c7-4a73-8d71-0f2c64313c3f");
    public static final Identifier<DataProductEntity> UUID_593913AC = id("593913ac-0294-431b-adf3-5227ff8fddff");
    public static final Identifier<DataProductEntity> UUID_298B653C = id("298b653c-b326-40d3-a3d1-97e2e9d9ca22");
    public static final Identifier<DataProductEntity> UUID_E08AF9D2 = id("e08af9d2-99ec-41b3-a77c-d4457415944f");
    public static final Identifier<DataProductEntity> UUID_B17AE68A = id("b17ae68a-42a8-47fe-ba7b-ec7105c5c5c7");
    public static final Identifier<DataProductEntity> UUID_5DA9E6E0 = id("5da9e6e0-4c17-4683-af89-b49206472ae7");
    public static final Identifier<DataProductEntity> UUID_5AA2EE15 = id("5aa2ee15-4f66-46ed-aaf2-55e6db55f960");
    public static final Identifier<DataProductEntity> UUID_7E4B1B3E = id("7e4b1b3e-bcfb-4d94-923d-1277828de70b");
    public static final Identifier<DataProductEntity> UUID_C6F18E45 = id("c6f18e45-95f8-47b3-9b11-9cbae351a9b2");
    // ZO API
    public static final Identifier<DataProductEntity> UUID_2F28D2EC = id("2f28d2ec-8797-46fd-8149-9c70ac5f2ebe");
    public static final Identifier<DataProductEntity> UUID_0B42AFB7 = id("0b42afb7-3683-4065-9cb3-396995a5be97");
    public static final Identifier<DataProductEntity> UUID_D10B898F = id("d10b898f-9796-4993-9654-8f092b97989d");
    public static final Identifier<DataProductEntity> UUID_59115547 = id("59115547-cfd5-428a-9863-6cd1b2200013");
    public static final Identifier<DataProductEntity> UUID_EC5B9F05 = id("ec5b9f05-0b8d-4833-9447-d7338fe1dc78");
    public static final Identifier<DataProductEntity> UUID_88DCF0F9 = id("88dcf0f9-8502-4596-b035-45c5d548d262");
    public static final Identifier<DataProductEntity> UUID_54DECB00 = id("54decb00-cecd-4c64-b368-8ab999130ac4");
    public static final Identifier<DataProductEntity> UUID_D156F252 = id("d156f252-c8c0-49de-8541-149c35aada6c");
    public static final Identifier<DataProductEntity> UUID_720AA209 = id("720aa209-7aa8-4faf-8b1a-013a041084f2");
    public static final Identifier<DataProductEntity> UUID_B0A4FF29 = id("b0a4ff29-cac4-4413-b8fb-7ff61f4ff2ac");
    public static final Identifier<DataProductEntity> UUID_E6128E10 = id("e6128e10-5d2b-4096-8356-b6523cb30e92");
  }

  public static class DataProvider {
    public static final Identifier<DataProviderEntity> UUID_E37B148B = id("e37b148b-9a0f-4c2e-80c5-fe9c9416b640");
  }

  @Getter
  @RequiredArgsConstructor
  public enum Uid {
    CHE101000001("Erika Musterfrau", NATUERLICHE_PERSON),
    CHE102000001("Jonas Testmann", NATUERLICHE_PERSON),
    CHE102000002("Testpartner GmbH", EINFACHE_GESELLSCHAFT),
    CHE103000001("Max Mustermann", NATUERLICHE_PERSON),
    CHE103000002("Testfirma GmbH", EINFACHE_GESELLSCHAFT),
    CHE104000002("Testbetrieb AG", AKTIENGESELLSCHAFT),
    ZZZ199984051("Lara Beispiel", EQUIDENEIGENTUEMER),
    ZZZ199984068("Nico Demomann", EQUIDENEIGENTUEMER);

    private final String uidName;
    private final LegalFormEnum legalForm;
  }

  public enum Bur {
    CODE_99910002("99910002"),
    CODE_99910003("99910003"),
    CODE_99910004("99910004"),
    CODE_99910005("99910005"),
    CODE_99920004("99920004"),
    CODE_99920005("99920005"),
    CODE_99920006("99920006"),
    CODE_99930004("99930004"),
    CODE_99930005("99930005"),
    CODE_99940003("99940003"),
    CODE_99940004("99940004");

    private final String code;

    Bur(String code) {
      this.code = code;
    }

    public String getCode() {
      return code;
    }
  }
}
