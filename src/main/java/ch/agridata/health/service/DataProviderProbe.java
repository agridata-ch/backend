package ch.agridata.health.service;

/**
 * Extension point that checks the reachability of the data source system identified by a given
 * {@code data_source_system.code}. Implementations may throw; the caller treats any failure or timeout as down.
 *
 * @CommentLastReviewed 2026-08-11
 */
public interface DataProviderProbe {

  String dataSourceSystemCode();

  boolean isUp();
}
