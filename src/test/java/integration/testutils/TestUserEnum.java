package integration.testutils;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TestUserEnum {
  PRODUCER_032("producer-b", List.of("CHE102000001", "CHE102000002"), "FLXXB0001"),
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
