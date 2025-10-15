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
    public static final Identifier<ConsentRequestEntity> BIO_SUISSE_01_CHE_278 = id("66363629-9bf3-439c-8c87-c3dab54e63bb");
    public static final Identifier<ConsentRequestEntity> BIO_SUISSE_01_CHE_948 = id("6243cb1d-fcd6-41cf-94d7-41041bfb19e0");
    public static final Identifier<ConsentRequestEntity> BIO_SUISSE_01_CHE_744 = id("5ed45bdf-8e7d-4982-91f4-97246b013beb");
    public static final Identifier<ConsentRequestEntity> BIO_SUISSE_01_CHE_505 = id("f2f2cf9b-c09d-4d20-86f3-8095f47c5216");
    public static final Identifier<ConsentRequestEntity> BIO_SUISSE_01_CHE_435 = id("8cb5ecef-43bf-4469-9b51-fa8f49a6105c");
    public static final Identifier<ConsentRequestEntity> BIO_SUISSE_01_CHE_860 = id("d4f9a2a2-be5b-42df-9ec0-c5d922e90ecc");
    public static final Identifier<ConsentRequestEntity> BIO_SUISSE_01_CHE_280 = id("e3f9b0c1-2d4e-0f5a-4b8c-7d9e1f3a5b7c");
    public static final Identifier<ConsentRequestEntity> BIO_SUISSE_01_CHE_286 = id("c9d5f6a7-8b0c-6d1e-0f4a-3b5c7d9e1f3a");
    public static final Identifier<ConsentRequestEntity> BIO_SUISSE_01_CHE_299 = id("f2a8c9d0-1e3f-9a4b-3c7d-6e8f0a2b4c6d");
    public static final Identifier<ConsentRequestEntity> BIO_SUISSE_01_CHE_801 = id("f4a0c1d2-3e5f-1a6b-5c9d-8e0f2a4b6c8d");


    public static final Identifier<ConsentRequestEntity> BIO_SUISSE_02_CHE_435 = id("638ec4e3-796a-4505-bc3e-9c8d5895df75");
    public static final Identifier<ConsentRequestEntity> BIO_SUISSE_02_CHE_860 = id("4077affa-c024-4555-99e4-2b30fa997180");
    public static final Identifier<ConsentRequestEntity> BIO_SUISSE_02_CHE_553 = id("07b11d34-e33d-4a80-8e61-66ace084408c");
    public static final Identifier<ConsentRequestEntity> BIO_SUISSE_02_CHE_142 = id("99544917-1d65-4d28-90f0-f1269242fed7");
    public static final Identifier<ConsentRequestEntity> BIO_SUISSE_02_CHE_280 = id("ca5ad276-4805-4bf9-a34e-e448cc6bd7b1");
    public static final Identifier<ConsentRequestEntity> BIO_SUISSE_02_CHE_801 = id("9165a46d-c48d-4141-b03f-40a0f8a3b932");
    public static final Identifier<ConsentRequestEntity> BIO_SUISSE_02_CHE_278 = id("07b11d34-e33d-4a80-8e61-66ace084408c");
    public static final Identifier<ConsentRequestEntity> BIO_SUISSE_02_CHE_632 = id("d0e6a7b8-9c1d-7e2f-1a5b-4c6d8e0f2a4b");
    public static final Identifier<ConsentRequestEntity> BIO_SUISSE_02_CHE_505 = id("d6e2a3b4-5c7d-3e8f-7a1b-0c2d4e6f8a0b");
    public static final Identifier<ConsentRequestEntity> BIO_SUISSE_02_CHE_917 = id("b8c4e5f6-7a9b-5c0d-9e3f-2a4b6c8d0e2f");
    public static final Identifier<ConsentRequestEntity> BIO_SUISSE_02_CHE_948 = id("b0c6e7f8-9a1b-7c2d-1e5f-4a6b8c0d2e4f");

    public static final Identifier<ConsentRequestEntity> IP_SUISSE_01_CHE_280 = id("3f18e5ac-f49a-4f20-b04d-669b3f08d8b8");
    public static final Identifier<ConsentRequestEntity> IP_SUISSE_01_CHE_801 = id("3c74ef78-3343-475f-bc91-040c27e8b2a3");
    public static final Identifier<ConsentRequestEntity> IP_SUISSE_01_CHE_299 = id("c77d4675-a8ba-4b69-bbfa-4a446e7a2045");
    public static final Identifier<ConsentRequestEntity> IP_SUISSE_01_CHE_632 = id("323e24e8-a296-4e15-a793-ad97dd46dd55");
    public static final Identifier<ConsentRequestEntity> IP_SUISSE_01_CHE_917 = id("39806f21-0dfd-4aeb-9c00-4898f21b65d5");
    public static final Identifier<ConsentRequestEntity> IP_SUISSE_01_CHE_286 = id("e28377b3-372a-4c94-9bd1-fbce932bdc2e");
    public static final Identifier<ConsentRequestEntity> IP_SUISSE_01_CHE_860 = id("a3b9d0e1-2f4a-0b5c-4d8e-7f9a1b3c5d7e");
    public static final Identifier<ConsentRequestEntity> IP_SUISSE_01_CHE_553 = id("c5d1f2a3-4b6c-2d7e-6f0a-9b1c3d5e7f9a");
    public static final Identifier<ConsentRequestEntity> IP_SUISSE_01_CHE_505 = id("e7f3b4c5-6d8e-4f9a-8b2c-1d3e5f7a9b1c");
    public static final Identifier<ConsentRequestEntity> IP_SUISSE_01_CHE_948 = id("a9b5d6e7-8f0a-6b1c-0d4e-3f5a7b9c1d3e");
    public static final Identifier<ConsentRequestEntity> IP_SUISSE_01_CHE_744 = id("d2e8a9b0-1c3d-9e4f-3a7b-6c8d0e2f4a6b");
    public static final Identifier<ConsentRequestEntity> IP_SUISSE_01_CHE_142 = id("a5b1d2e3-4f6a-2b7c-6d0e-9f1a3b5c7d9e");

    public static final Identifier<ConsentRequestEntity> IP_SUISSE_02_CHE_553 = id("5d0e1a01-bebb-4936-be41-afc910d05ea7");
    public static final Identifier<ConsentRequestEntity> IP_SUISSE_02_CHE_280 = id("0f602be3-52c5-4e18-ac82-1ee9d9b615c8");
    public static final Identifier<ConsentRequestEntity> IP_SUISSE_02_CHE_299 = id("93d551e1-054f-4263-9862-20567a191f66");
    public static final Identifier<ConsentRequestEntity> IP_SUISSE_02_CHE_286 = id("9d65735b-78bb-4c6a-9b58-8eacbb485084");
    public static final Identifier<ConsentRequestEntity> IP_SUISSE_02_CHE_435 = id("ff6c18e2-5219-415b-b12a-0328c0a26121");
    public static final Identifier<ConsentRequestEntity> IP_SUISSE_02_CHE_860 = id("2cbcaa08-74f6-4356-9e52-bfa247b3e56e");
    public static final Identifier<ConsentRequestEntity> IP_SUISSE_02_CHE_801 = id("183f2c04-ceea-45d6-853d-c0df5defd467");
    public static final Identifier<ConsentRequestEntity> IP_SUISSE_02_CHE_278 = id("f8a4c5d6-7e9f-5a0b-9c3d-2e4f6a8b0c2d");
    public static final Identifier<ConsentRequestEntity> IP_SUISSE_02_CHE_744 = id("c1d7f8a9-0b2c-8d3e-2f6a-5b7c9d1e3f5a");
    public static final Identifier<ConsentRequestEntity> IP_SUISSE_02_CHE_632 = id("e1f7b8c9-0d2e-8f3a-2b6c-5d7e9f1a3b5c");
    public static final Identifier<ConsentRequestEntity> IP_SUISSE_02_CHE_917 = id("a7b3d4e5-6f8a-4b9c-8d2e-1f3a5b7c9d1e");
    public static final Identifier<ConsentRequestEntity> IP_SUISSE_02_CHE_142 = id("b6c2e3f4-5a7b-3c8d-7e1f-0a2b4c6d8e0f");
  }

  public static class DataProduct {
    public static final Identifier<DataProductEntity> UUID_C661EA48 = id("c661ea48-106d-4d7a-a5d1-a9a6db48dd8c");
    public static final Identifier<DataProductEntity> UUID_147E8C40 = id("147e8c40-78cc-4db3-a909-65504aa62a64");
    public static final Identifier<DataProductEntity> UUID_254BB1F1 = id("254bb1f1-05d3-4c4e-80ea-2fa075771ed7");
    public static final Identifier<DataProductEntity> UUID_085E4B72 = id("085e4b72-964d-4bd5-a3c9-224d8c5585af");
    public static final Identifier<DataProductEntity> UUID_A795D0B0 = id("a795d0b0-f177-4bb4-8e41-1ed12d358c79");
    public static final Identifier<DataProductEntity> UUID_0A808700 = id("0a808700-d89e-4fa0-a2b8-8edb15f3addd");
    public static final Identifier<DataProductEntity> UUID_EF4F42DD = id("ef4f42dd-eaa9-4af1-988c-86b47bd963fe");
    public static final Identifier<DataProductEntity> UUID_2375219C = id("2375219c-5fe3-458f-bd07-d3c2c87e2539");
    public static final Identifier<DataProductEntity> UUID_64E39DF0 = id("64e39df0-2e56-4204-9c44-a43e1e26a2e8");
    public static final Identifier<DataProductEntity> UUID_1DAD9F91 = id("1dad9f91-30d8-45c9-8c82-ad72f4cb22e7");
    public static final Identifier<DataProductEntity> UUID_46F8A883 = id("46f8a883-da7c-49b3-b986-10a24b1e09ef");
  }

  public static class Uid {
    public static final String CHE860 = "CHE***860";
    public static final String CHE435 = "CHE***435";
    public static final String CHE278 = "CHE***278";
    public static final String CHE142 = "CHE***142";
    public static final String CHE299 = "CHE***299";
    public static final String CHE948 = "CHE***948";
  }
}
