package integration.testutils;

import ch.agridata.agreement.persistence.ConsentRequestEntity;
import ch.agridata.agreement.persistence.DataRequestEntity;
import ch.agridata.product.persistence.DataProductEntity;
import java.util.UUID;
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
    public static final Identifier<DataRequestEntity> IP_SUISSE_01 = id("98a35e61-0162-4986-9e9e-ee5c65f86316");
    public static final Identifier<DataRequestEntity> IP_SUISSE_02 = id("341f558a-781c-4eb5-bab7-c2f39216b9f2");
  }

  public static class ConsentRequest {
    public static final Identifier<ConsentRequestEntity> BIO_SUISSE_01_CHE101000001 = id("07813a3a-7b8d-4b68-847b-f34ce7037397");
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
  }

  public enum Uid {
    CHE101000001,
    CHE102000001,
    CHE102000002,
    CHE103000001,
    CHE103000002,
    CHE104000002
  }

  public enum Bur {
    A99910002,
    A99910003,
    A99910004,
    A99910005,
    A99920004,
    A99920005,
    A99920006,
    A99930004,
    A99930005,
    A99940003,
    A99940004
  }
}
