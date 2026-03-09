package ch.agridata.datatransferv2.service.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.agridata.datatransferv2.service.AgridataContext;
import ch.agridata.datatransferv2.service.FlowEnum;
import com.google.common.collect.Range;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ResolveRequestedDateTaskTest {

  private ResolveRequestedDateTask task;

  @BeforeEach
  void setUp() {
    task = new ResolveRequestedDateTask();
  }

  @ParameterizedTest(name = "[{0}]")
  @MethodSource("requestParamCases")
  void givenRequestParams_whenApply_thenRequestedDateRangeIsResolved(
      @SuppressWarnings("unused") String description,
      Map<String, String> params,
      Range<@NotNull LocalDate> expectedRange) {
    var context = createContextWithParams(params);

    assertThat(task.apply(context).getRequestedDateRange()).isEqualTo(expectedRange);
  }

  static Stream<Arguments> requestParamCases() {
    return Stream.of(
        Arguments.of("date only → single-day range",
            Map.of("date", "2026-01-15"),
            Range.closed(LocalDate.of(2026, 1, 15), LocalDate.of(2026, 1, 15))),
        Arguments.of("dateFrom only → from to today",
            Map.of("dateFrom", "2025-06-01"),
            Range.closed(LocalDate.of(2025, 6, 1), LocalDate.now())),
        Arguments.of("dateFrom and dateTo → explicit range",
            Map.of("dateFrom", "2025-12-01", "dateTo", "2026-01-15"),
            Range.closed(LocalDate.of(2025, 12, 1), LocalDate.of(2026, 1, 15))),
        Arguments.of("date overrides dateFrom and dateTo",
            Map.of("date", "2026-03-01", "dateFrom", "2025-12-01", "dateTo", "2026-06-01"),
            Range.closed(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 1))),
        Arguments.of("no params → today to today",
            Map.of(),
            Range.closed(LocalDate.now(), LocalDate.now())));
  }

  @Test
  void givenDateFromAfterDateTo_whenApply_thenIllegalArgumentExceptionIsThrown() {
    var context = createContextWithParams(Map.of("dateFrom", "2026-03-09", "dateTo", "2026-03-01"));

    assertThatThrownBy(() -> task.apply(context))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invalid range");
  }

  private AgridataContext createContextWithParams(Map<String, String> params) {
    return AgridataContext.builder()
        .productId(UUID.randomUUID())
        .flowEnum(FlowEnum.BUR_BASED_POST_VALIDATION)
        .requestParameters(params)
        .build();
  }
}
