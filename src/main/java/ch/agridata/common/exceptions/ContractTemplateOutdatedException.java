package ch.agridata.common.exceptions;

/**
 * Indicates that a contract revision is based on an outdated contract template version and can therefore not be signed.
 *
 * @CommentLastReviewed 2026-11-13
 */
public class ContractTemplateOutdatedException extends RuntimeException {

  public ContractTemplateOutdatedException(String message) {
    super(message);
  }
}
