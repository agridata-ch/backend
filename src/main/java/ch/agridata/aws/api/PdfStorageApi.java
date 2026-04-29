package ch.agridata.aws.api;

/**
 * Internal interface for storing and retrieving PDF files in object storage.
 *
 * @CommentLastReviewed 2026-04-17
 */
public interface PdfStorageApi {

  /**
   * Uploads a PDF to a storage bucket.
   *
   * @param bucket   the name of the bucket
   * @param fileName the fileName used as the storage key
   * @param pdf      raw bytes of the PDF to store
   */
  void upload(String bucket, String fileName, byte[] pdf);

  /**
   * Downloads a PDF from a storage bucket.
   *
   * @param bucket   the name of the bucket
   * @param fileName the fileName used as the storage key
   * @return raw bytes of the downloaded PDF
   */
  byte[] download(String bucket, String fileName);
}
