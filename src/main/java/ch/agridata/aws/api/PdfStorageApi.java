package ch.agridata.aws.api;

/**
 * Internal interface for storing, retrieving, and deleting PDF files in object storage. Additionally, it provides operations for reading
 * the virus scan results.
 *
 * @CommentLastReviewed 2026-07-10
 */
public interface PdfStorageApi {

  String GUARDDUTY_TAG_KEY = "GuardDutyMalwareScanStatus";

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

  /**
   * Reads the scan result from a storage bucket.
   *
   * @param bucket   the name of the bucket
   * @param fileName the fileName used as the storage key
   * @return the scan result as a GuardDutyScanResultEnum
   */
  GuardDutyScanResultEnum readScanResult(String bucket, String fileName);

  /**
   * Deletes a PDF from a storage bucket.
   *
   * @param bucket   the name of the bucket
   * @param fileName the fileName used as the storage key
   */
  void delete(String bucket, String fileName);
}
