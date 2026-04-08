package integration.testutils;

import static integration.testutils.TestDataIdentifiers.Uid.CHE101000001;
import static integration.testutils.TestDataIdentifiers.Uid.CHE102000001;
import static integration.testutils.TestDataIdentifiers.Uid.CHE102000002;
import static integration.testutils.TestDataIdentifiers.Uid.CHE103000001;
import static integration.testutils.TestDataIdentifiers.Uid.CHE103000002;
import static integration.testutils.TestDataIdentifiers.Uid.CHE104000002;
import static integration.testutils.TestDataIdentifiers.Uid.ZZZ199984051;
import static integration.testutils.TestDataIdentifiers.Uid.ZZZ199984068;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TestUserEnum {
  PRODUCER_A("producer-a",
      "Erika",
      "Musterfrau",
      List.of(CHE101000001),
      "3477580",
      "FLXXA0001"),
  PRODUCER_B("producer-b",
      "Jonas",
      "Testmann",
      List.of(CHE102000001, CHE102000002),
      "3477581",
      "FLXXB0001"),
  PRODUCER_B_3("producer-b-3",
      "Sofia",
      "Mustermann",
      List.of(CHE102000002),
      "3477582",
      "FLXXB0003"),
  PRODUCER_C("producer-c",
      "Max",
      "Mustermann",
      List.of(CHE103000001, CHE103000002),
      "3477583",
      "FLXXC0001"),
  PRODUCER_D("producer-d",
      "Lara",
      "Beispiel",
      List.of(CHE104000002, ZZZ199984051),
      "3477584",
      "FLXXD0001"),
  PRODUCER_E("producer-e",
      "Nico",
      "Demomann",
      List.of(ZZZ199984068),
      "3477585",
      null),
  CONSUMER_BIO_SUISSE("consumer",
      "Lea",
      "Consumer",
      List.of(),
      "20154600",
      null),
  CONSUMER_IP_SUISSE("consumer-ip-suisse",
      "Tim",
      "Consumer",
      List.of(),
      "900000",
      null),
  CONSUMER_BLV_1("consumer-blv-1",
      "Maria",
      "BLV",
      List.of(),
      "3477588",
      null),
  CONSUMER_BLV_2("consumer-blv-2",
      "Thomas",
      "BLV",
      List.of(),
      "3477589",
      null),
  CONSUMER_BLV_WITHOUT_UID("consumer-blv-without-uid",
      "Sandra",
      "BLV",
      List.of(),
      "3477590",
      null),
  PROVIDER_1("provider-1",
      "Teo",
      "Provider",
      List.of(),
      "3477553",
      null),
  PROVIDER_2(
      "provider-2",
      "Nina",
      "Provider",
      List.of(),
      "3477586",
      null),
  SUPPORT("support",
      "Sep",
      "Support",
      List.of(),
      "3477555",
      null),
  GUEST("guest",
      "Leo",
      "Guest",
      List.of(),
      null,
      null),
  ADMIN("admin",
      "Tom",
      "Admin",
      List.of(),
      "3477554",
      null);

  private final String username;
  private final String givenName;
  private final String familyName;
  private final List<TestDataIdentifiers.Uid> companyUids;
  private final String agateLoginId;
  private final String ktIdP;

}
