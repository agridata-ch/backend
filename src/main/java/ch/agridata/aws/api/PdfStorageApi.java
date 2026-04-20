package ch.agridata.aws.api;

import java.util.UUID;

/**
 * Internal interface for storing and retrieving PDF files in object storage.
 *
 * @CommentLastReviewed 2026-04-17
 */
public interface PdfStorageApi {

  /**
   * Uploads a PDF to a storage bucket.
   *
   * @param bucket the name of the bucket
   * @param id     the ID used as the storage key
   * @param pdf    raw bytes of the PDF to store
   */
  void upload(String bucket, UUID id, byte[] pdf);
}
