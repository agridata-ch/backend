package ch.agridata.datatransferv2.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import org.junit.jupiter.api.Test;

class FlowTimingTest {

  @Test
  void givenTasksAdded_whenGetTasks_thenReturnedInInsertionOrderWithResponsibility() {
    var timing = new FlowTiming();
    timing.addTask("A", FlowTiming.Responsibility.AGRIDATA, System.nanoTime());
    timing.addTask("Provider Request", FlowTiming.Responsibility.PROVIDER, System.nanoTime());
    timing.addTask("B", FlowTiming.Responsibility.AGRIDATA, System.nanoTime());

    assertThat(timing.getTasks())
        .extracting(FlowTiming.TaskTiming::name, FlowTiming.TaskTiming::responsibility)
        .containsExactly(
            tuple("A", FlowTiming.Responsibility.AGRIDATA),
            tuple("Provider Request", FlowTiming.Responsibility.PROVIDER),
            tuple("B", FlowTiming.Responsibility.AGRIDATA));
  }

  @Test
  void givenTasksOfBothResponsibilities_whenAggregating_thenTimeIsSplitAndTotalIsTheirSum() {
    var timing = new FlowTiming();
    long fiftyMsAgo = System.nanoTime() - 50_000_000L;
    timing.addTask("agridata", FlowTiming.Responsibility.AGRIDATA, fiftyMsAgo);
    timing.addTask("provider", FlowTiming.Responsibility.PROVIDER, System.nanoTime());

    long agridata = timing.getUsedTimeInMsByResponsibility(FlowTiming.Responsibility.AGRIDATA);
    long provider = timing.getUsedTimeInMsByResponsibility(FlowTiming.Responsibility.PROVIDER);

    assertThat(agridata).isGreaterThanOrEqualTo(40L);
    assertThat(provider).isGreaterThanOrEqualTo(0L);
    assertThat(timing.getTotalTimeInMsSinceInitialization()).isEqualTo(agridata + provider);
  }

  @Test
  void givenNoTasks_whenAggregating_thenAllTimesAreZero() {
    var timing = new FlowTiming();

    assertThat(timing.getTasks()).isEmpty();
    assertThat(timing.getUsedTimeInMsByResponsibility(FlowTiming.Responsibility.AGRIDATA)).isZero();
    assertThat(timing.getUsedTimeInMsByResponsibility(FlowTiming.Responsibility.PROVIDER)).isZero();
    assertThat(timing.getTotalTimeInMsSinceInitialization()).isZero();
  }

  @Test
  void givenFreshTiming_thenNoFailedTask_andSetFailedTaskStoresIt() {
    var timing = new FlowTiming();
    assertThat(timing.getFailedTask()).isNull();

    timing.setFailedTask("BoomTask");

    assertThat(timing.getFailedTask()).isEqualTo("BoomTask");
  }
}
