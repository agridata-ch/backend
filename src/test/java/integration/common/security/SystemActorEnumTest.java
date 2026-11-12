package integration.common.security;

import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.common.security.SystemActorEnum;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.persistence.EntityManager;
import java.util.Arrays;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class SystemActorEnumTest {

  private final Flyway flyway;
  private final EntityManager entityManager;

  @BeforeEach
  void setUp() {
    flyway.migrate();
  }

  @Test
  void whenActorHasUserId_thenUserExistsWithActorIdAsGivenName() {
    var actorsWithUser = Arrays.stream(SystemActorEnum.values()).filter(actor -> actor.getUserId() != null).toList();

    assertThat(actorsWithUser).isNotEmpty().allSatisfy(actor -> {
      var givenNames = entityManager.createNativeQuery("select given_name from users where id = :id")
          .setParameter("id", actor.getUserId())
          .getResultList();
      assertThat(givenNames).as("users row of %s", actor).containsExactly(actor.getLoggingActorId());
    });
  }

  @Test
  void whenAllActorsChecked_thenUserIdsAreUnique() {
    var userIds = Arrays.stream(SystemActorEnum.values()).map(SystemActorEnum::getUserId).filter(Objects::nonNull).toList();

    assertThat(userIds).doesNotHaveDuplicates();
  }
}
