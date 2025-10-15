package ch.agridata.agreement.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ConsentRequestEntityTest {

  static Stream<Arguments> provideShowStateAsMigratedCases() {
    LocalDateTime now = LocalDateTime.now();

    return Stream.of(
        Arguments.of("No migration date set → false",
            null, null, false),
        Arguments.of("Migration date set and no last state change date → true",
            now, null, true),
        Arguments.of("Migration date set and last state change date was earlier → true",
            now, now.minusDays(1), true),
        Arguments.of("Migration date set and last state change date was later → false",
            now, now.plusDays(1), false)
    );
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("provideShowStateAsMigratedCases")
  @DisplayName("isShowStateAsMigrated should behave correctly")
  void testShowStateAsMigrated(String title,
                               LocalDateTime migratedFromMafDate,
                               LocalDateTime lastStateChangeDate,
                               boolean expected) {

    ConsentRequestEntity entity = new ConsentRequestEntity();
    entity.setMigratedFromMafDate(migratedFromMafDate);
    entity.setLastStateChangeDate(lastStateChangeDate);

    assertThat(entity.isShowStateAsMigrated()).isEqualTo(expected);
  }
}
