package ch.agridata.datatransferv2.service.task;

import static org.assertj.core.api.Assertions.assertThat;

import ch.agridata.datatransferv2.service.AgridataContext;
import ch.agridata.datatransferv2.service.FlowEnum;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ResolveRequestedDateTaskTest {

  private ResolveRequestedDateTask task;

  @BeforeEach
  void setUp() {
    task = new ResolveRequestedDateTask();
  }

  @Test
  void givenDateInRequestParameters_whenApply_thenRequestedDateIsSet() {
    var context = createContextWithParams(Map.of("date", "2026-01-15"));

    var result = task.apply(context);

    assertThat(result.getRequestedDate()).isEqualTo(LocalDate.of(2026, 1, 15));
  }

  @Test
  void givenDateFromInRequestParameters_whenApply_thenRequestedDateIsSet() {
    var context = createContextWithParams(Map.of("dateFrom", "2025-06-01"));

    var result = task.apply(context);

    assertThat(result.getRequestedDate()).isEqualTo(LocalDate.of(2025, 6, 1));
  }

  @Test
  void givenBothDateAndDateFrom_whenApply_thenDateTakesPrecedence() {
    var context = createContextWithParams(Map.of("date", "2026-03-01", "dateFrom", "2025-12-01"));

    var result = task.apply(context);

    assertThat(result.getRequestedDate()).isEqualTo(LocalDate.of(2026, 3, 1));
  }

  @Test
  void givenNoDateParameters_whenApply_thenRequestedDateIsToday() {
    var context = createContextWithParams(Map.of());

    var result = task.apply(context);

    assertThat(result.getRequestedDate()).isEqualTo(LocalDate.now());
  }

  private AgridataContext createContextWithParams(Map<String, String> params) {
    return AgridataContext.builder()
        .productId(UUID.randomUUID())
        .flowEnum(FlowEnum.BUR_BASED_POST_VALIDATION)
        .requestParameters(params)
        .build();
  }
}
