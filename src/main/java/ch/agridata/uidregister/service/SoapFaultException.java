package ch.agridata.uidregister.service;

/**
 * Raised when the UID-Register responds with a SOAP fault envelope. Carries the parsed {@code faultstring} so callers can attach
 * request-specific context before surfacing the failure.
 *
 * @CommentLastReviewed 2026-06-16
 */
public class SoapFaultException extends RuntimeException {

  public SoapFaultException(String faultString) {
    super(faultString);
  }
}
