package ch.agridata.datatransferv2.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.Assertions.within;

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
  void givenRequestArrivedBeforeFlow_whenSummarize_thenTimeOutsideProviderCallCountsAsAgridata() {
    long now = System.nanoTime();
    var timing = new FlowTiming(now - 80_000_000L);
    timing.addTask("Provider Request", FlowTiming.Responsibility.PROVIDER, now - 20_000_000L);

    var summary = timing.summarize();

    assertThat(summary.totalTimeInMs()).isGreaterThanOrEqualTo(80L);
    assertThat(summary.usedTimeInMsByProvider()).isBetween(20L, summary.totalTimeInMs());
    assertThat(summary.usedTimeInMsByAgridata()).isGreaterThanOrEqualTo(55L);
    assertThat(summary.usedTimeInMsByAgridata() + summary.usedTimeInMsByProvider())
        .isCloseTo(summary.totalTimeInMs(), within(1L));
  }

  @Test
  void givenSubMillisecondTasks_whenSummarize_thenTheyAddUpInsteadOfRoundingToZero() {
    var timing = new FlowTiming();
    for (int i = 0; i < 5; i++) {
      timing.addTask("Provider Request", FlowTiming.Responsibility.PROVIDER, System.nanoTime() - 900_000L);
    }

    assertThat(timing.getTasks()).allSatisfy(task -> assertThat(task.durationMs()).isGreaterThanOrEqualTo(0.9));
    assertThat(timing.summarize().usedTimeInMsByProvider()).isGreaterThanOrEqualTo(4L);
  }

  @Test
  void givenFreshTiming_thenNoFailedTask_andSetFailedTaskStoresIt() {
    var timing = new FlowTiming();
    assertThat(timing.getFailedTask()).isNull();

    timing.setFailedTask("BoomTask");

    assertThat(timing.getFailedTask()).isEqualTo("BoomTask");
  }
}
