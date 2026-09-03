package ch.agridata.bit.api;

import lombok.NonNull;

/**
 * Internal interface for sealing a PDF document via the BIT evidence Signing service.
 * Callers provide raw PDF bytes and the authUserId of the user who confirms the seal via Mobile ID.
 * The full flow — PDF hash extraction, BIT API communication, and CMS signature embedding — is
 * handled by the implementation.
 *
 * @CommentLastReviewed 2026-08-20
 */
public interface BitSignatureApi {

  /**
   * Seals the given PDF document via the BIT evidence Signing API using Mobile ID for declaration
   * of intent. Blocks until the user confirms on their device or the process fails.
   *
   * @param documentBytes raw bytes of a valid, non-encrypted PDF document
   * @param authUserId    auth user ID of the user who will receive the Mobile ID push notification
   * @return the sealed PDF as a byte array
   */
  byte[] sign(byte @NonNull [] documentBytes, @NonNull String authUserId);
}
