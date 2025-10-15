package integration.testutils;

import static integration.testutils.TestDataIdentifiers.Uid.CHE435;
import static integration.testutils.TestDataIdentifiers.Uid.CHE860;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TestUserEnum {
  PRODUCER_032("producer-032", List.of(CHE860, CHE435), "***032"),
  CONSUMER_BIO_SUISSE("consumer", List.of(), null),
  CONSUMER_IP_SUISSE("consumer-ip-suisse", List.of(), null),
  PROVIDER("provider", List.of(), null),
  SUPPORT("support", List.of(), null),
  GUEST("guest", List.of(), null),
  ADMIN("admin", List.of(), null);

  private final String username;
  private final List<String> companyUids;
  private final String ktIdP;

}
