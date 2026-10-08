package ch.agridata.datatransferv2.service;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Per-invocation collector for the timings of a single data transfer flow. Wall-clock time is measured from the moment
 * the HTTP request arrived (not from the first flow task), so authentication, product lookup and response streaming are
 * included. Time spent waiting for the data provider is tracked separately; everything else is attributed to
 * agridata.ch. Durations are kept in nanoseconds and only converted to milliseconds when read, so sub-millisecond tasks
 * do not round down to zero.
 *
 * @CommentLastReviewed 2026-10-04
 */
final class FlowTiming {

  private static final long NANOS_PER_MILLI = 1_000_000L;

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
   * Measured duration of a single pipeline step within a flow, in milliseconds with microsecond precision.
   *
   * @CommentLastReviewed 2026-10-04
   */
  record TaskTiming(String name, Responsibility responsibility, double durationMs) {
  }

  private final long requestStartNanos;
  @Getter
  private final List<TaskTiming> tasks = new ArrayList<>();
  private long providerNanos;
  @Getter
  @Setter
  private String failedTask;

  FlowTiming() {
    this(System.nanoTime());
  }

  FlowTiming(long requestStartNanos) {
    this.requestStartNanos = requestStartNanos;
  }

  void addTask(String name, Responsibility responsibility, long taskStartNanos) {
    long durationNanos = System.nanoTime() - taskStartNanos;
    if (responsibility == Responsibility.PROVIDER) {
      providerNanos += durationNanos;
    }
    tasks.add(new TaskTiming(name, responsibility, Math.round(durationNanos / 1_000.0) / 1_000.0));
  }

  /**
   * Freezes the timings at the current instant. Agridata time is the wall-clock time since the request arrived minus
   * the time spent waiting for the provider.
   */
  Summary summarize() {
    long totalNanos = System.nanoTime() - requestStartNanos;
    return new Summary(toMillis(totalNanos), toMillis(totalNanos - providerNanos), toMillis(providerNanos));
  }

  private static long toMillis(long nanos) {
    return Math.round((double) nanos / NANOS_PER_MILLI);
  }

  /**
   * Snapshot of the aggregated flow timings in milliseconds.
   *
   * @CommentLastReviewed 2026-10-04
   */
  record Summary(long totalTimeInMs, long usedTimeInMsByAgridata, long usedTimeInMsByProvider) {
  }
}
