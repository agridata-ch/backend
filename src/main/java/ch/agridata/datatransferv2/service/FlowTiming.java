package ch.agridata.datatransferv2.service;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * Per-invocation collector for the timings of a single data transfer flow. Records one entry per
 * executed task, attributes each duration to a {@link Responsibility} (agridata.ch vs. the data
 * provider) and remembers the task that failed, if any. A fresh instance is created for every
 * {@link AgridataFlow#run} call and carries no shared state.
 *
 * @CommentLastReviewed 2026-08-04
 */
@Getter
@Setter
final class FlowTiming {

  /**
   * Responsible for task
   *
   * @CommentLastReviewed 2026-08-03
   */
  public enum Responsibility {
    AGRIDATA,
    PROVIDER
  }

  /**
   * Measured duration of a single pipeline task within a flow phase.
   *
   * @CommentLastReviewed 2026-08-03
   */
  record TaskTiming(String name, Responsibility responsibility, long durationMs) {
  }

  private final List<TaskTiming> tasks = new ArrayList<>();
  private String failedTask;

  void addTask(String name, Responsibility responsibility, long taskStartTime) {
    tasks.add(new TaskTiming(name, responsibility, (System.nanoTime() - taskStartTime) / 1_000_000));
  }

  long getUsedTimeInMsByResponsibility(@NonNull Responsibility responsibility) {
    return tasks.stream()
        .filter(task -> responsibility.equals(task.responsibility))
        .mapToLong(TaskTiming::durationMs)
        .sum();
  }

  long getTotalTimeInMsSinceInitialization() {
    return tasks.stream()
        .mapToLong(TaskTiming::durationMs)
        .sum();
  }
}
