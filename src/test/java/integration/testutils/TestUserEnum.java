package integration.testutils;

import static integration.testutils.TestDataIdentifiers.Uid.CHE101000001;
import static integration.testutils.TestDataIdentifiers.Uid.CHE102000001;
import static integration.testutils.TestDataIdentifiers.Uid.CHE102000002;
import static integration.testutils.TestDataIdentifiers.Uid.CHE103000001;
import static integration.testutils.TestDataIdentifiers.Uid.CHE103000002;
import static integration.testutils.TestDataIdentifiers.Uid.CHE104000002;
import static integration.testutils.TestDataIdentifiers.Uid.ZZZ199978837;
import static integration.testutils.TestDataIdentifiers.Uid.ZZZ199981609;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TestUserEnum {
  PRODUCER_A("producer-a",
      List.of(CHE101000001),
      "3477580",
      "FLXXA0001"),
  PRODUCER_B("producer-b",
      List.of(CHE102000001, CHE102000002, ZZZ199978837, ZZZ199981609),
      "3477581",
      "FLXXB0001"),
  PRODUCER_B_3("producer-b-3",
      List.of(CHE102000002),
      "3477582",
      "FLXXB0003"),
  PRODUCER_C("producer-c",
      List.of(CHE103000001, CHE103000002),
      "3477583",
      "FLXXC0001"),
  PRODUCER_D("producer-d",
      List.of(CHE104000002),
      "3477584",
      "FLXXD0001"),
  CONSUMER_BIO_SUISSE("consumer",
      List.of(),
      "20154600",
      null),
  CONSUMER_IP_SUISSE("consumer-ip-suisse",
      List.of(),
      "900000",
      null),
  PROVIDER("provider",
      List.of(),
      "3477553",
      null),
  SUPPORT("support",
      List.of(),
      "3477555",
      null),
  GUEST("guest",
      List.of(),
      null,
      null),
  ADMIN("admin",
      List.of(),
      "3477554",
      null);

  private final String username;
  private final List<TestDataIdentifiers.Uid> companyUids;
  private final String agateLoginId;
  private final String ktIdP;

}
