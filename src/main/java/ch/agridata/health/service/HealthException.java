package ch.agridata.health.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Signals a non-2xx response from a data provider's health endpoint, carrying the returned HTTP status.
 *
 * @CommentLastReviewed 2026-08-11
 */
@Getter
@RequiredArgsConstructor
public class HealthException extends RuntimeException {
  private final Integer httpStatus;
}
