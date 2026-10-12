package ch.agridata.burregister.service;

/**
 * Raised when the BUR responds with a SOAP 1.2 fault envelope. Carries the fault reason text so callers can attach request-specific
 * context before surfacing the failure.
 *
 * @CommentLastReviewed 2026-10-09
 */
public class SoapFaultException extends RuntimeException {

  public SoapFaultException(String reason) {
    super(reason);
  }
}
