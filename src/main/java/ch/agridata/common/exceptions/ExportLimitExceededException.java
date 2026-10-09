package ch.agridata.common.exceptions;

/**
 * Indicates that an export was rejected because it contains more rows than the export allows.
 *
 * @CommentLastReviewed 2026-10-12
 */
public class ExportLimitExceededException extends RuntimeException {

  public ExportLimitExceededException(long rowCount, int maxRows) {
    super("Export contains " + rowCount + " rows, which exceeds the maximum of " + maxRows + " rows");
  }
}
